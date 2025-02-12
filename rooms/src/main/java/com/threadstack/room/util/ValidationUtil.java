package com.threadstack.room.util;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Set;

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
