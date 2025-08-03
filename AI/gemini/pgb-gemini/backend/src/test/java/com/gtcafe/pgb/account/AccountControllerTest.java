package com.gtcafe.pgb.account;

import com.gtcafe.pgb.config.SecurityConfig;
import com.gtcafe.pgb.message.Message;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@Import(SecurityConfig.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @MockBean
    private CustomOAuth2UserService customOAuth2UserService;

    @MockBean
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @MockBean
    private TokenAuthenticationFilter tokenAuthenticationFilter;

    @Test
    void testGetCurrentUserMessages() throws Exception {
        Page<Message> page = new PageImpl<>(Collections.emptyList());
        when(accountService.getUserMessages(any(String.class), any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/accounts/me/posts").with(oauth2Login()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void testGetUserMessages() throws Exception {
        Page<Message> page = new PageImpl<>(Collections.emptyList());
        when(accountService.getUserPosts(any(Long.class), any(PageRequest.class))).thenReturn(page);

        mockMvc.perform(get("/api/accounts/1/messages"))
                .andExpect(status().isOk());
    }
}