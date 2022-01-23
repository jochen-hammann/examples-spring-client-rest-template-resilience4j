package com.example.spring.restclient.resttemplateresilience4j.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
public class MyException extends Exception
{
    // ============================== [Fields] ==============================

    // -------------------- [Private Fields] --------------------

    private int status;

    // ============================== [Construction / Destruction] ==============================

    // -------------------- [Public Construction / Destruction] --------------------

    public MyException(int status, String message)
    {
        super(message);
        this.status = status;
    }

    // ============================== [Spring Beans] ==============================

    // -------------------- [Public Spring Beans] --------------------

    // ============================== [Getter/Setter] ==============================

    // -------------------- [Private Getter/Setter] --------------------

    // -------------------- [Public Getter/Setter] --------------------

    // ============================== [Methods] ==============================

    // -------------------- [Private Methods] --------------------

    // -------------------- [Public Methods] --------------------

}
