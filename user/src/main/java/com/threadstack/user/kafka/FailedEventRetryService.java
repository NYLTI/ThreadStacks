package com.threadstack.user.kafka;

import java.util.concurrent.TimeUnit;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.threadstack.user.repository.FailedEventRepository;
import com.threadstack.user.model.FailedEvent;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class FailedEventRetryService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final FailedEventRepository failedEventRepository;

    public FailedEventRetryService(KafkaTemplate<String, String> kafkaTemplate,
	    FailedEventRepository failedEventRepository) {
	this.kafkaTemplate = kafkaTemplate;
	this.failedEventRepository = failedEventRepository;
    }

    @Scheduled(fixedDelay = 10000)
    public void retryFailedEvents() {
	if (!RetryStatus.shouldRetry.get())
	    return;
	System.out.println("Checking failed events");
	if (testKafka()) {
	    failedEventRepository.findAll().flatMap(this::sendAndDeleteEvent).doOnComplete(this::checkAndStopRetry)
		    .subscribeOn(Schedulers.boundedElastic()).subscribe();
	} else {
	    System.err.println("Kafka is still unavailable");
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

    private Mono<Void> sendAndDeleteEvent(FailedEvent event) {
	return Mono.fromFuture(() -> kafkaTemplate.send(event.getTopic(), event.getEventData())).flatMap(sendResult -> {
	    if (sendResult.getRecordMetadata() != null) {
		return failedEventRepository.delete(event);
	    } else {
		return Mono.empty();
	    }
	}).onErrorResume(ex -> Mono.empty());
    }

    private void checkAndStopRetry() {
	failedEventRepository.count().doOnSuccess(count -> {
	    if (count == 0) {
		System.err.println("Completed all qs");
		RetryStatus.shouldRetry.set(false);
	    }
	}).subscribe();
    }
}
