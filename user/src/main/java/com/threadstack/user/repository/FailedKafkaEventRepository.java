package com.threadstack.user.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import com.threadstack.user.model.FailedKafkaEvent;

public interface FailedKafkaEventRepository extends R2dbcRepository<FailedKafkaEvent, Long>{

}
