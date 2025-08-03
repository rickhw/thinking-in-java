
package com.gtcafe.pgb.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gtcafe.pgb.config.SecurityConfig;
import com.gtcafe.pgb.security.CustomOAuth2UserService;
import com.gtcafe.pgb.security.OAuth2AuthenticationSuccessHandler;
import com.gtcafe.pgb.security.TokenAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import com.gtcafe.pgb.TaskResponse;

@WebMvcTest(MessageController.class)
@Import(SecurityConfig.class)
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MessageService messageService;

    @MockBean
    private CustomOAuth2UserService customOAuth2UserService;

    @MockBean
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @MockBean
    private TokenAuthenticationFilter tokenAuthenticationFilter;

    @Test
    @WithMockUser
    void testGetPosts() throws Exception {
        Page<Message> page = new PageImpl<>(Collections.emptyList());
        when(messageService.getPosts(any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/messages"))
                .andExpect(status().isOk());
    }

    @Test
    void testTaskResponseSerialization() throws Exception {
        TaskResponse taskResponse = new TaskResponse("test-id", "PENDING", "Test message");
        String json = objectMapper.writeValueAsString(taskResponse);
        System.out.println("Serialized TaskResponse: " + json);
        // Assert that the serialized JSON contains the expected fields
        // This is a basic check, more robust assertions can be added
        assert(json.contains("\"taskId\":\"test-id\""));
        assert(json.contains("\"status\":\"PENDING\""));
        assert(json.contains("\"message\":\"Test message\""));
    }

    @Test
    @WithMockUser
    void testCreatePost() throws Exception {
        Message message = new Message();
        message.setContent("New Post");

        when(messageService.createPost(any(Message.class)))
                .thenReturn(CompletableFuture.completedFuture(message));

        mockMvc.perform(post("/api/messages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(message)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(messageService, times(1)).createPost(any(Message.class));
    }

    @Test
    @WithMockUser
    void testUpdatePost() throws Exception {
        Message message = new Message();
        message.setContent("Updated Post");

        when(messageService.updatePost(any(Long.class), any(Message.class)))
                .thenReturn(CompletableFuture.completedFuture(message));

        mockMvc.perform(put("/api/messages/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(message)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(messageService, times(1)).updatePost(any(Long.class), any(Message.class));
    }

    @Test
    @WithMockUser
    void testDeletePost() throws Exception {
        when(messageService.deletePost(any(Long.class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        mockMvc.perform(delete("/api/messages/1"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(messageService, times(1)).deletePost(any(Long.class));
    }
}
