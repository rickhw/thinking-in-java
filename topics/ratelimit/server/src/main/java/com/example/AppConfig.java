package com.example;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.FilterRegistrationBean;

import com.github.bucket4j.Bandwidth;
import com.github.bucket4j.Bucket;
import com.github.bucket4j.Refill;

@Configuration
public class AppConfig {

    @Bean
    public Bucket bucket() {
        Bandwidth limit = Bandwidth.classic(10, Refill.greedy(10, Duration.ofSeconds(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilter(Bucket bucket) {
        FilterRegistrationBean<RateLimitFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new RateLimitFilter(bucket));
        registrationBean.addUrlPatterns("/api/*");
        return registrationBean;
    }
}
