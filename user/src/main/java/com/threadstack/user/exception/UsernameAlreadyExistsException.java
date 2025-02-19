package com.threadstack.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class UsernameAlreadyExistsException extends RuntimeException {
    /**
	 * 
	 */
	private static final long serialVersionUID = -2096698040718754569L;

	public UsernameAlreadyExistsException(String message) {
        super(message);
    }
}
