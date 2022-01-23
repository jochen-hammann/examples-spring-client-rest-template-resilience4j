package com.example.spring.restclient.resttemplateresilience4j.hello;

import com.example.spring.restclient.resttemplateresilience4j.dto.MyError;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.CheckedFunction0;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
class HelloControllerErrorHandlingIT
{
    // ============================== [Fields] ==============================

    // -------------------- [Private Fields] --------------------

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RestTemplate restTemplate;

    // ============================== [Unit Tests] ==============================

    // -------------------- [Test Helper Classes] --------------------

    // -------------------- [Test Helper Methods] --------------------

    private void dump(Object obj) throws JsonProcessingException
    {
        String jsonStr = this.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        System.out.println(jsonStr);
    }

    public void handleException(Throwable throwable) throws JsonProcessingException
    {
        try
        {
            throw throwable;
        }
        catch (ResourceAccessException ex)
        {
            log.error("An access exception occurred.");
        }
        catch (RestClientResponseException ex)
        {
            log.error("A response exception occurred.");

            if (ex.getResponseHeaders().getContentType().isCompatibleWith(MediaType.APPLICATION_JSON))
            {
                MyError myError = this.objectMapper.readValue(ex.getResponseBodyAsString(), MyError.class);
                this.dump(myError);
            }
            else
                System.out.println(ex.getResponseBodyAsString());
        }
        catch (UnknownContentTypeException ex)
        {
            log.error("An unknown content type exception occurred.");
        }
        catch (Throwable t)
        {
            log.error("An unknown exception occurred.");
        }
    }

    // -------------------- [Test Initialization] --------------------

    // -------------------- [Tests] --------------------

    @Test
    void getErrorHandling() throws JsonProcessingException
    {
        CheckedFunction0 checkedFunction = () -> this.restTemplate.getForEntity("http://localhost:{port}/hello/error/400", Hello.class, port);

        Try<ResponseEntity<Hello>> result = Try.of(checkedFunction);

        if (result.isSuccess())
        {
            ResponseEntity<Hello> responseEntity = result.get();
            this.dump(responseEntity.getBody());
        }
        else
        {
            Throwable throwable = result.getCause();
            this.handleException(throwable);
        }
    }
}
