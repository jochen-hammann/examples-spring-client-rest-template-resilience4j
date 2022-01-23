package com.example.spring.restclient.resttemplateresilience4j.config;

import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Configuration
public class Resilience4jConfig
{
    // ============================== [Fields] ==============================

    // -------------------- [Private Fields] --------------------

    // -------------------- [Public Fields] --------------------

    // ============================== [Construction / Destruction] ==============================

    // -------------------- [Private Construction / Destruction] --------------------

    // -------------------- [Public Construction / Destruction] --------------------

    // ============================== [Spring Beans] ==============================

    // -------------------- [Public Spring Beans] --------------------

    @Bean
    RetryRegistry retryRegistry()
    {
        RetryConfig config = RetryConfig.<ResponseEntity>custom()
                                        .maxAttempts(3)
                                        .waitDuration(Duration.ofMillis(1000))
                                        .retryOnResult(response -> response.getStatusCode().value() == 500)
                                        //.retryOnException(e -> e instanceof WebServiceException)
                                        .retryExceptions(ResourceAccessException.class)
                                        //.ignoreExceptions(BusinessException.class, OtherBusinessException.class)
                                        .failAfterMaxAttempts(true)
                                        .build();

        return RetryRegistry.of(config);
    }

    // ============================== [Getter/Setter] ==============================

    // -------------------- [Private Getter/Setter] --------------------

    // -------------------- [Public Getter/Setter] --------------------

    // ============================== [Methods] ==============================

    // -------------------- [Private Methods] --------------------

    // -------------------- [Public Methods] --------------------

}
