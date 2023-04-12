package com.feiwin.imserver.exception;

import lombok.Data;

@Data
public class ServiceException extends RuntimeException {
    private final int code;

    public ServiceException(String message) {
        this(500, message);
    }

    public ServiceException(int code, String message) {
        super(message);
        this.code = code;
    }
}
