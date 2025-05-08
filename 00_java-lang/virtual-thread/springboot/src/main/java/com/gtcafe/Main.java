package com.gtcafe;

import org.apache.catalina.Executor;
import org.apache.catalina.core.StandardThreadExecutor;
import org.apache.coyote.ProtocolHandler;
import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Bean
    public TomcatProtocolHandlerCustomizer<?> protocolHandlerVirtualThreadCustomizer() {
        return protocolHandler -> {
            if (protocolHandler instanceof AbstractHttp11Protocol<?> http11) {
                ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
                Executor executorAdapter = new Executor() {
                    @Override public void execute(Runnable command) {
                        executor.submit(command);
                    }
                    @Override public String getName() {
                        return "virtualThreadExecutor";
                    }
                };
                http11.setExecutor(executorAdapter);
            }
        };
    }
}
