package com.gtcafe.pgb.security;

import java.util.Optional;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.gtcafe.pgb.account.Account;
import com.gtcafe.pgb.account.AccountRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final AccountRepository accountRepos;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String googleId = oauth2User.getName();
        String profileImageUrl = oauth2User.getAttribute("picture");

        Optional<Account> userOptional = accountRepos.findByGoogleId(googleId);
        if (userOptional.isEmpty()) {
            Account user = new Account();
            user.setGoogleId(googleId);
            user.setEmail(email);
            user.setName(name);
            user.setProfileImageUrl(profileImageUrl);
            accountRepos.save(user);
        }

        return oauth2User;
    }
}
