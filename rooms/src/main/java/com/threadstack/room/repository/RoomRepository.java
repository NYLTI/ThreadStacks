package com.threadstack.room.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import com.threadstack.room.model.Room;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface RoomRepository extends ReactiveMongoRepository<Room, String> {
    Mono<Room> findByNameIgnoreCase(String name);

    Flux<Room> findByCreatedBy(Long createdBy);

    @Query(value = "{ '_id': ?0 }", fields = "{ 'members': 1 }")
    Mono<List<Long>> findMembersById(String roomId);

    @Query("{ '_id': ?0, 'members': { $ne: ?1 } }")
    @Update("{ '$addToSet': { 'members': ?1 } }")
    Mono<Long> addMember(String roomId, Long memberId);

    @Query("{ '_id': ?0 }")
    @Update("{ '$pull': { 'members': ?1 } }")
    Mono<Long> removeMember(String roomId, Long memberId);
}
