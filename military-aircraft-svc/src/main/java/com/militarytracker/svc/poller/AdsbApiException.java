package com.militarytracker.svc.poller;

public class AdsbApiException extends RuntimeException {

    public AdsbApiException(String message) {
        super(message);
    }

    public AdsbApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
