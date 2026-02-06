package com.example.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({ObjectNotFound.class, UserAlreadyExist.class})
    public ResponseEntity<Map<String, String>> handleException(RuntimeException ex){
        Map<String,String> error = new HashMap<>();
        error.put("message",ex.getMessage()!=null?ex.getMessage():"hmm something went wrong..\nTry again later");
        error.put("error","Runtime Exception");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
