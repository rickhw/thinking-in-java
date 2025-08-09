package com.example.messageboard.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gtcafe.messageboard.controller.request.UpdateMessageRequest;

/**
 * Integration tests for error handling across the entire application
 */
@SpringBootTest(classes = com.gtcafe.messageboard.Main.class)
@AutoConfigureWebMvc
@ActiveProfiles("test")
class ErrorHandlingIntegrationTest {

    private MockMvc mockMvc;
    
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void getMessageById_WithInvalidIdFormat_ShouldReturnStandardErrorResponse() throws Exception {
        // Given
        String invalidId = "invalid-id-format";

        // When & Then
        mockMvc.perform(get("/api/v1/messages/{messageId}", invalidId))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode").value("INVALID_MESSAGE_ID"))
                .andExpect(jsonPath("$.message").value("Invalid message ID format: " + invalidId));
    }

    @Test
    void getMessageById_WithTooShortId_ShouldReturnStandardErrorResponse() throws Exception {
        // Given
        String shortId = "ABC123";

        // When & Then
        mockMvc.perform(get("/api/v1/messages/{messageId}", shortId))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode").value("INVALID_MESSAGE_ID"))
                .andExpect(jsonPath("$.message").value("Invalid message ID format: " + shortId));
    }

    @Test
    void getMessageById_WithTooLongId_ShouldReturnStandardErrorResponse() throws Exception {
        // Given
        String longId = "ABCD1234-EFGH-5678-IJKL-MNOPQRSTUVWXYZ-EXTRA";

        // When & Then
        mockMvc.perform(get("/api/v1/messages/{messageId}", longId))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode").value("INVALID_MESSAGE_ID"))
                .andExpect(jsonPath("$.message").value("Invalid message ID format: " + longId));
    }

    @Test
    void getMessageById_WithLowercaseCharacters_ShouldReturnStandardErrorResponse() throws Exception {
        // Given
        String lowercaseId = "abcd1234-efgh-5678-ijkl-mnopqrstuvwx";

        // When & Then
        mockMvc.perform(get("/api/v1/messages/{messageId}", lowercaseId))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode").value("INVALID_MESSAGE_ID"))
                .andExpect(jsonPath("$.message").value("Invalid message ID format: " + lowercaseId));
    }

    @Test
    void getMessageById_WithSpecialCharacters_ShouldReturnStandardErrorResponse() throws Exception {
        // Given
        String specialCharId = "ABCD@#$%-EFGH-5678-IJKL-MNOPQRSTUVWX";

        // When & Then
        mockMvc.perform(get("/api/v1/messages/{messageId}", specialCharId))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode").value("INVALID_MESSAGE_ID"))
                .andExpect(jsonPath("$.message").value("Invalid message ID format: " + specialCharId));
    }

    @Test
    void getMessageById_WithWrongDashPositions_ShouldReturnStandardErrorResponse() throws Exception {
        // Given
        String wrongDashId = "ABCD12-34EFGH-5678-IJKL-MNOPQRSTUVWX";

        // When & Then
        mockMvc.perform(get("/api/v1/messages/{messageId}", wrongDashId))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode").value("INVALID_MESSAGE_ID"))
                .andExpect(jsonPath("$.message").value("Invalid message ID format: " + wrongDashId));
    }

    @Test
    void updateMessage_WithInvalidIdFormat_ShouldReturnStandardErrorResponse() throws Exception {
        // Given
        String invalidId = "invalid-id";
        UpdateMessageRequest request = new UpdateMessageRequest();
        request.setContent("Updated content");

        // When & Then
        mockMvc.perform(put("/api/v1/messages/{messageId}", invalidId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode").value("INVALID_MESSAGE_ID"))
                .andExpect(jsonPath("$.message").value("Invalid message ID format: " + invalidId));
    }

    @Test
    void deleteMessage_WithInvalidIdFormat_ShouldReturnStandardErrorResponse() throws Exception {
        // Given
        String invalidId = "invalid-id";

        // When & Then
        mockMvc.perform(delete("/api/v1/messages/{messageId}", invalidId))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errorCode").value("INVALID_MESSAGE_ID"))
                .andExpect(jsonPath("$.message").value("Invalid message ID format: " + invalidId));
    }

    @Test
    void getMessageById_WithValidFormatButNonExistentId_ShouldReturnNotFound() throws Exception {
        // Given - Valid format but non-existent ID
        String validButNonExistentId = "ABCD1234-EFGH-5678-IJKL-MNOPQRSTUVWX";

        // When & Then
        mockMvc.perform(get("/api/v1/messages/{messageId}", validButNonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void errorResponse_ShouldHaveConsistentStructure() throws Exception {
        // Given
        String invalidId1 = "invalid1";
        String invalidId2 = "invalid2";

        // When & Then - Test multiple invalid IDs return same error structure
        mockMvc.perform(get("/api/v1/messages/{messageId}", invalidId1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.errorCode").value("INVALID_MESSAGE_ID"))
                .andExpect(jsonPath("$.message").value("Invalid message ID format: " + invalidId1));

        mockMvc.perform(get("/api/v1/messages/{messageId}", invalidId2))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.errorCode").value("INVALID_MESSAGE_ID"))
                .andExpect(jsonPath("$.message").value("Invalid message ID format: " + invalidId2));
    }

    @Test
    void errorResponse_ShouldNotExposeInternalDetails() throws Exception {
        // Given
        String invalidId = "invalid-id";

        // When & Then
        mockMvc.perform(get("/api/v1/messages/{messageId}", invalidId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_MESSAGE_ID"))
                .andExpect(jsonPath("$.message").value("Invalid message ID format: " + invalidId))
                // Ensure no internal details are exposed
                .andExpect(jsonPath("$.stackTrace").doesNotExist())
                .andExpect(jsonPath("$.cause").doesNotExist())
                .andExpect(jsonPath("$.exception").doesNotExist());
    }

    @Test
    void errorResponse_ShouldBeValidJson() throws Exception {
        // Given
        String invalidId = "invalid-id";

        // When & Then
        String responseContent = mockMvc.perform(get("/api/v1/messages/{messageId}", invalidId))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Verify it's valid JSON by parsing it
        objectMapper.readTree(responseContent);
    }

    @Test
    void multipleInvalidRequests_ShouldAllReturnConsistentErrorFormat() throws Exception {
        // Given
        String[] invalidIds = {
            "invalid",
            "too-short",
            "ABCD1234-EFGH-5678-IJKL-MNOPQRSTUVWXYZ-TOO-LONG",
            "abcd1234-efgh-5678-ijkl-mnopqrstuvwx",
            "ABCD@#$%-EFGH-5678-IJKL-MNOPQRSTUVWX"
        };

        // When & Then
        for (String invalidId : invalidIds) {
            mockMvc.perform(get("/api/v1/messages/{messageId}", invalidId))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.errorCode").value("INVALID_MESSAGE_ID"))
                    .andExpect(jsonPath("$.message").value("Invalid message ID format: " + invalidId));
        }
    }
}