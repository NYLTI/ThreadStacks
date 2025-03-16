package com.threadstack.user.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import com.threadstack.user.model.FailedEvent;

public interface FailedEventRepository extends R2dbcRepository<FailedEvent, Long>{

}
