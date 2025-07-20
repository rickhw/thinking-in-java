package com.gtcafe.pgb.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.gtcafe.pgb.account.Account;
import com.gtcafe.pgb.account.AccountRepository;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository repos;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account user = repos.findByGoogleId(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with googleId : " + username));

        return new org.springframework.security.core.userdetails.User(user.getGoogleId(), "", new ArrayList<>());
    }
}
