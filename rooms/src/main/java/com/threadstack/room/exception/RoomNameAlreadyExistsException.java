package com.threadstack.room.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class RoomNameAlreadyExistsException extends RuntimeException {
    /**
     * 
     */
    private static final long serialVersionUID = -5639443194387196389L;

    public RoomNameAlreadyExistsException(String message) {
	super(message);
    }
}
