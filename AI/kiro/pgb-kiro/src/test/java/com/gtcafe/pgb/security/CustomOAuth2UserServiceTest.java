package com.gtcafe.pgb.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.gtcafe.pgb.entity.User;
import com.gtcafe.pgb.service.SSOService;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @Mock
    private SSOService ssoService;

    @Mock
    private OAuth2UserRequest userRequest;

    @Mock
    private ClientRegistration clientRegistration;

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    private OAuth2User oauth2User;
    private User localUser;

    @BeforeEach
    void setUp() {
        // Create OAuth2User
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", "google123");
        attributes.put("email", "test@gmail.com");
        attributes.put("name", "Test User");

        oauth2User = new DefaultOAuth2User(null, attributes, "sub");

        // Create local user
        localUser = new User();
        localUser.setId(1L);
        localUser.setSsoId("google:google123");
        localUser.setEmail("test@gmail.com");
        localUser.setDisplayName("Test User");
        localUser.setUsername("testuser");
        localUser.setIsActive(true);
        localUser.setCreatedAt(LocalDateTime.now());
        localUser.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void loadUser_ValidRequest_ShouldReturnCustomOAuth2User() {
        // Given
        when(userRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn("google");
        when(clientRegistration.getClientId()).thenReturn("client-id");
        when(clientRegistration.getClientSecret()).thenReturn("client-secret");
        when(clientRegistration.getAuthorizationGrantType()).thenReturn(AuthorizationGrantType.AUTHORIZATION_CODE);
        when(clientRegistration.getRedirectUri()).thenReturn("http://localhost:8080/login/oauth2/code/google");

        when(ssoService.processOAuth2User(any(OAuth2User.class), eq("google"))).thenReturn(localUser);

        // When
        OAuth2User result = customOAuth2UserService.loadUser(userRequest);

        // Then
        assertThat(result).isInstanceOf(CustomOAuth2User.class);

        CustomOAuth2User customUser = (CustomOAuth2User) result;
        assertThat(customUser.getName()).isEqualTo("testuser");
        assertThat(customUser.getLocalUser()).isEqualTo(localUser);
        assertThat(customUser.getAttributes()).containsKey("sub");

        verify(ssoService).processOAuth2User(any(OAuth2User.class), eq("google"));
    }

    @Test
    void loadUser_SSOServiceThrowsException_ShouldThrowOAuth2AuthenticationException() {
        // Given
        when(userRequest.getClientRegistration()).thenReturn(clientRegistration);
        when(clientRegistration.getRegistrationId()).thenReturn("google");
        when(clientRegistration.getClientId()).thenReturn("client-id");
        when(clientRegistration.getClientSecret()).thenReturn("client-secret");
        when(clientRegistration.getAuthorizationGrantType()).thenReturn(AuthorizationGrantType.AUTHORIZATION_CODE);
        when(clientRegistration.getRedirectUri()).thenReturn("http://localhost:8080/login/oauth2/code/google");

        when(ssoService.processOAuth2User(any(OAuth2User.class), eq("google")))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThatThrownBy(() -> customOAuth2UserService.loadUser(userRequest))
                .isInstanceOf(OAuth2AuthenticationException.class)
                .hasMessageContaining("Failed to process OAuth2 user");

        verify(ssoService).processOAuth2User(any(OAuth2User.class), eq("google"));
    }
}