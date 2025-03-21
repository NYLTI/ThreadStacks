package com.threadstack.room.kafka;

import java.util.concurrent.TimeUnit;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.threadstack.room.repository.RoomCreatedEventRepository;
import com.threadstack.room.util.RetryUtility;
import com.threadstack.room.model.RoomCreatedEvent;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class KafkaRetryService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final RoomCreatedEventRepository roomCreatedEventRepository;

    public KafkaRetryService(KafkaTemplate<String, String> kafkaTemplate,
                             RoomCreatedEventRepository roomCreatedEventRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.roomCreatedEventRepository = roomCreatedEventRepository;
    }

    @Scheduled(fixedDelay = 10000)
    public void retryFailedEvents() {
        if (!RetryUtility.SHOULDRETRYKAFKA.get()) return;

        if (testKafka()) {
            roomCreatedEventRepository.findAll()
                .flatMap(this::sendAndDeleteEvent)
                .doOnComplete(this::checkAndStopRetry)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
        }
    }

    private boolean testKafka() {
        try {
            kafkaTemplate.send("test-health-topic", "health-check").get(5, TimeUnit.SECONDS);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Mono<Void> sendAndDeleteEvent(RoomCreatedEvent event) {
        return Mono.fromFuture(() -> kafkaTemplate.send("room-created-topic", event.getRoomName()))
                .flatMap(sendResult -> {
                    if (sendResult.getRecordMetadata() != null) {
                        return roomCreatedEventRepository.delete(event);
                    } else {
                        return Mono.empty();
                    }
                })
                .onErrorResume(ex -> Mono.empty());
    }

    private void checkAndStopRetry() {
        roomCreatedEventRepository.count()
            .doOnSuccess(count -> {
                if (count == 0) {
                    RetryUtility.SHOULDRETRYKAFKA.set(false);
                }
            }).subscribe();
    }
}
