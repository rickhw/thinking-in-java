package com.gtcafe.pgb;

import com.gtcafe.pgb.Main;
import com.gtcafe.pgb.config.SecurityConfig;
import com.gtcafe.pgb.security.CustomOAuth2UserService;
import com.gtcafe.pgb.security.OAuth2AuthenticationSuccessHandler;
import com.gtcafe.pgb.security.TokenAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(classes = {Main.class, SecurityConfig.class})
class MainTests {

	@MockBean
	private CustomOAuth2UserService customOAuth2UserService;

	@MockBean
	private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

	@MockBean
	private TokenAuthenticationFilter tokenAuthenticationFilter;

	@Test
	void contextLoads() {
	}

}
