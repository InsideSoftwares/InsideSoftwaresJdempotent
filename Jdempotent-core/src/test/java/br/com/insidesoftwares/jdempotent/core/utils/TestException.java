package br.com.insidesoftwares.jdempotent.core.utils;

public class TestException extends RuntimeException {
    public TestException(){
        super();
    }

    public TestException(String message){
        super(message);
    }
}
