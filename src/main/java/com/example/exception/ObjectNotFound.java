package com.example.exception;

public class ObjectNotFound extends RuntimeException{
    public ObjectNotFound(String message){
        super(message);
    }
}
