package com.fuxi.core.common.exception;

import org.apache.log4j.Logger;


public class BusinessException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(BusinessException.class);

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(Throwable cause) {
        super(cause);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }



}
