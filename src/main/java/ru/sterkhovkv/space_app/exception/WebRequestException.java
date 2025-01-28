package ru.sterkhovkv.space_app.exception;

public class WebRequestException extends RuntimeException {
    public WebRequestException(String message) {
        super(message);
    }
}
