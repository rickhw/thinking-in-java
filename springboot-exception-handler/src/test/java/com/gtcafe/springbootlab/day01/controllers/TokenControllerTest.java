package com.gtcafe.springbootlab.day01.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gtcafe.springbootlab.day01.payload.request.CreateAppTokenRequest;

@WebMvcTest(TokenController.class)
public class TokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testValidationSuccess() throws Exception {
        CreateAppTokenRequest request = new CreateAppTokenRequest();
        request.setTokenName("ValidToken");
        request.setAppName("ValidApp");
        request.setTenantId(0);

        mockMvc.perform(post("/api/tokens/")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Auth-Token", "123")
                .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
            ;
    }

    @Test
    public void testValidationJsonBody() throws Exception {
        CreateAppTokenRequest request = new CreateAppTokenRequest();
        // 不符合驗證條件的數據

        mockMvc.perform(post("/api/tokens/")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Auth-Token", "123")
                .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists()) // 驗證錯誤消息
            ;
                // .andExpect(jsonPath("$.tenantId").exists());
    }



    // @Test
    // public void testValidationFailure() throws Exception {
    //     CreateAppTokenRequest request = new CreateAppTokenRequest();
    //     // 不符合驗證條件的數據

    //     mockMvc.perform(post("/api/tokens/")
    //             .contentType(MediaType.APPLICATION_JSON)
    //             .header("X-Auth-Token", "123")
    //             .content(new ObjectMapper().writeValueAsString(request)))
    //             .andExpect(status().isBadRequest())
    //             .andExpect(jsonPath("$.id").exists()) // 驗證錯誤消息
    //             .andExpect(jsonPath("$.token").exists())
    //             ;
    //             // .andExpect(jsonPath("$.tenantId").exists());
    // }
}
