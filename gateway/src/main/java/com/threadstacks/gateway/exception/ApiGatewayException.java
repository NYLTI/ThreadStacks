package com.threadstacks.gateway.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public class ApiGatewayException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = -6734477561963030151L;
	private final HttpStatus status;

    public ApiGatewayException(String message, HttpStatusCode httpStatusCode) {
        super(message);
        this.status = (HttpStatus) httpStatusCode;
    }
    
    public HttpStatusCode getStatus() {
        return status;
    }
}