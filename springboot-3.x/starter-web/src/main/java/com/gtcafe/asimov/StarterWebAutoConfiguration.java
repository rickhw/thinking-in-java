package com.gtcafe.asimov;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "starter.web", name = "enabled", havingValue = "true", matchIfMissing = true)
public class StarterWebAutoConfiguration {

    @Bean
    public TaskService taskService() {
        return new TaskService();
    }
}