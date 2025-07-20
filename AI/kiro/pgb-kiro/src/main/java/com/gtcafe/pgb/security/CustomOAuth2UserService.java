package com.gtcafe.pgb.security;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.gtcafe.pgb.entity.User;
import com.gtcafe.pgb.service.SSOService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Custom OAuth2 user service that integrates with our SSO service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final SSOService ssoService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.debug("Loading OAuth2 user from provider: {}",
                userRequest.getClientRegistration().getRegistrationId());

        // Load the OAuth2 user from the provider
        OAuth2User oauth2User = super.loadUser(userRequest);

        try {
            // Process the OAuth2 user through our SSO service
            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            User localUser = ssoService.processOAuth2User(oauth2User, registrationId);

            // Create a custom OAuth2 user that includes our local user data
            return new CustomOAuth2User(oauth2User, localUser);

        } catch (Exception ex) {
            log.error("Error processing OAuth2 user", ex);
            throw new OAuth2AuthenticationException("Failed to process OAuth2 user: " + ex.getMessage());
        }
    }
}