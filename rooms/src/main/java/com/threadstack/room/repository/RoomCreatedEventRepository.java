package com.threadstack.room.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.threadstack.room.model.RoomCreatedEvent;

public interface RoomCreatedEventRepository extends ReactiveMongoRepository<RoomCreatedEvent, ObjectId> {

}
