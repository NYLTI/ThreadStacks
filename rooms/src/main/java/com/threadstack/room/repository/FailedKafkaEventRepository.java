package com.threadstack.room.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.threadstack.room.model.FailedKafkaEvent;

public interface FailedKafkaEventRepository extends ReactiveMongoRepository<FailedKafkaEvent, ObjectId> {

}
