package com.threadstack.thread.util;

import java.util.Set;

import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import reactor.core.publisher.Mono;

@Component
public class ValidationUtil {
    
    private final Validator validator;

    public ValidationUtil(Validator validator) {
	this.validator = validator;
    }

    public <T> Mono<T> validate(T obj) {
	Set<ConstraintViolation<T>> violations = validator.validate(obj);
	if (!violations.isEmpty()) {
	    return Mono.error(new ConstraintViolationException(violations));
	}
	return Mono.just(obj);
    }

}
