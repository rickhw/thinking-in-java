// package com.gtcafe.asimov.config;

// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.web.cors.CorsConfiguration;
// import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
// import org.springframework.web.filter.CorsFilter;

// import java.util.List;

// @Configuration
// public class CorsConfig {

//     // @Value("${app.cors.allowed-origins:http://localhost:3000}")
//     // private List<String> allowedOrigins;
   
//     @Bean
//     public CorsFilter corsFilter() {
//         // CorsConfiguration config = new CorsConfiguration();
//         // config.setAllowedOrigins(List.of("http://35.166.206.169")); // 設定允許的來源
//         // config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//         // config.setAllowedHeaders(List.of("*"));
//         // config.setAllowCredentials(true); // 允許帶上認證資訊 (e.g., Cookies, Authorization headers)

//         // UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//         // source.registerCorsConfiguration("/**", config); // 套用到所有端點

//         // return new CorsFilter(source);

//         UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//         CorsConfiguration config = new CorsConfiguration();
//         // config.setAllowedOrigins(allowedOrigins); // 允許的來源
//         config.addAllowedOrigin("*");
//         config.addAllowedMethod("*"); // 允許所有 HTTP 方法
//         config.addAllowedHeader("*"); // 允許所有請求 Header
//         config.setAllowCredentials(true); // 允許攜帶 Cookie

//         source.registerCorsConfiguration("/**", config);
//         return new CorsFilter(source);
//     }

// }