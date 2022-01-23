package com.example.spring.restclient.resttemplateresilience4j.hello;

import com.example.spring.restclient.resttemplateresilience4j.exceptions.MyException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.vavr.CheckedFunction0;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
class HelloControllerIT
{
    // ============================== [Fields] ==============================

    // -------------------- [Private Fields] --------------------

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RetryRegistry retryRegistry;

    // ============================== [Unit Tests] ==============================

    // -------------------- [Test Helper Classes] --------------------

    // -------------------- [Test Helper Methods] --------------------

    private void dump(Object hello) throws JsonProcessingException
    {
        String jsonStr = this.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(hello);
        System.out.println(jsonStr);
    }

    // -------------------- [Test Initialization] --------------------

    // -------------------- [Tests] --------------------

    @Test
    void getSuccess() throws JsonProcessingException
    {
        ResponseEntity<Hello> response = this.restTemplate.getForEntity("http://localhost:{port}/hello", Hello.class, this.port);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        this.dump(response.getBody());
    }

    @Test
    void getSuccessWithRetries() throws Throwable
    {
        Retry retry = this.retryRegistry.retry("Test");

        CheckedFunction0 checkedSupplier = Retry.decorateCheckedSupplier(retry,
                () -> restTemplate.getForEntity("http://localhost:{port}/hello", Hello.class, this.port));

        Try<ResponseEntity> result = Try.of(checkedSupplier);

        Assertions.assertTrue(result.isSuccess());
        Assertions.assertEquals(HttpStatus.OK, result.get().getStatusCode());

        this.dump(result.get().getBody());
    }

    @Test
    void getFailed() throws Throwable
    {
        Retry retry = this.retryRegistry.retry("Test");
        retry.getEventPublisher()
             .onError(
                     event -> log.error("[{}] The connection could not be established: '{}'", event.getName(), event.getLastThrowable().getMessage()))
             .onRetry(event -> log.info("[{}] Perform the {}. retry, because of the following error: '{}'.", event.getName(),
                     event.getNumberOfRetryAttempts(), event.getLastThrowable().getMessage()));

        CheckedFunction0 checkedSupplier = Retry.decorateCheckedSupplier(retry,
                () -> restTemplate.getForEntity("http://localhost:9090/hello", Hello.class, this.port));

        Try<ResponseEntity<Hello>> result = Try.of(checkedSupplier);

        Assertions.assertTrue(result.isFailure());
    }

    @Test
    void getFailedRecoverThrowable() throws Throwable
    {
        Retry retry = this.retryRegistry.retry("Test");
        retry.getEventPublisher()
             .onError(
                     event -> log.error("[{}] The connection could not be established: '{}'", event.getName(), event.getLastThrowable().getMessage()))
             .onRetry(event -> log.info("[{}] Perform the {}. retry, because of the following error: '{}'.", event.getName(),
                     event.getNumberOfRetryAttempts(), event.getLastThrowable().getMessage()));

        CheckedFunction0 checkedSupplier = Retry.decorateCheckedSupplier(retry,
                () -> restTemplate.getForEntity("http://localhost:9090/hello", Hello.class, this.port));

        // Recover the result - i.e. the Try succeeds with the specified response.
        Try<ResponseEntity<Hello>> result = Try.of(checkedSupplier).recover(t -> ResponseEntity.ok().body(new Hello("The request failed")));

        Assertions.assertTrue(result.isSuccess());  // recover() leads to a successful Try.
        Assertions.assertEquals(HttpStatus.OK, result.get().getStatusCode());
        this.dump(result.get().getBody());
    }

    @Test
    void getFailedThrows()
    {
        Retry retry = this.retryRegistry.retry("Test");
        retry.getEventPublisher()
             .onError(
                     event -> log.error("[{}] The connection could not be established: '{}'", event.getName(), event.getLastThrowable().getMessage()))
             .onRetry(event -> log.info("[{}] Perform the {}. retry, because of the following error: '{}'.", event.getName(),
                     event.getNumberOfRetryAttempts(), event.getLastThrowable().getMessage()));

        CheckedFunction0<ResponseEntity<Hello>> checkedSupplier = Retry.decorateCheckedSupplier(retry,
                () -> restTemplate.getForEntity("http://localhost:9090/hello", Hello.class, this.port));

        Assertions.assertThrows(Exception.class, () -> Try.of(checkedSupplier).get());
    }

    @Test
    void getFailedOrElseThrows()
    {
        Retry retry = this.retryRegistry.retry("Test");
        retry.getEventPublisher()
             .onError(
                     event -> log.error("[{}] The connection could not be established: '{}'", event.getName(), event.getLastThrowable().getMessage()))
             .onRetry(event -> log.info("[{}] Perform the {}. retry, because of the following error: '{}'.", event.getName(),
                     event.getNumberOfRetryAttempts(), event.getLastThrowable().getMessage()));

        CheckedFunction0<ResponseEntity<Hello>> checkedSupplier = Retry.decorateCheckedSupplier(retry,
                () -> restTemplate.getForEntity("http://localhost:9090/hello", Hello.class, this.port));

        Assertions.assertThrows(MyException.class,
                () -> Try.of(checkedSupplier).getOrElseThrow(t -> new MyException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Test")));
    }
}
