package com.feiwin.imserver.exception;

public class Assert {

    public static void isFalse(boolean expression, String message) {
        if(expression) {
            throw new ServiceException(message);
        }
    }

    public static void isTrue(boolean expression, String message) {
        if(!expression) {
            throw new ServiceException(message);
        }
    }

}
