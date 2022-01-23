package com.example.spring.restclient.resttemplateresilience4j.hello;

import com.example.spring.restclient.resttemplateresilience4j.dto.MyError;
import com.example.spring.restclient.resttemplateresilience4j.exceptions.MyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.UnknownContentTypeException;

@RestController
public class HelloController
{
    // ============================== [Fields] ==============================

    // -------------------- [Private Fields] --------------------

    // ============================== [Construction / Destruction] ==============================

    // -------------------- [Public Construction / Destruction] --------------------

    // ============================== [Getter/Setter] ==============================

    // -------------------- [Private Getter/Setter] --------------------

    // -------------------- [Public Getter/Setter] --------------------

    // ============================== [Methods] ==============================

    // -------------------- [Private Methods] --------------------

    @ExceptionHandler()
    public ResponseEntity<MyError> handleException(MyException ex)
    {
        MyError myError = new MyError(ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).contentType(MediaType.APPLICATION_JSON).body(myError);
    }

    // -------------------- [Public Methods] --------------------

    @GetMapping(path = "/hello",
                produces = MediaType.APPLICATION_JSON_VALUE)
    public Hello get()
    {
        return new Hello("Hello, GET!");
    }

    @PostMapping(path = "/hello",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public Hello post(@RequestBody Hello hello)
    {
        return new Hello("Hello, POST!");
    }

    @GetMapping(path = "/hello/error/{status}",
                produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Hello> get(@PathVariable("status") Integer status) throws Exception
    {
        // Simulate an error.
        throw new MyException(status, String.format("An error with status code '%d' occurred.", status));
    }
}
