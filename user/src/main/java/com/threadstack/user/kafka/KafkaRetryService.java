package com.threadstack.user.kafka;

import java.util.concurrent.TimeUnit;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.threadstack.user.repository.FailedKafkaEventRepository;
import com.threadstack.user.util.RetryUtility;
import com.threadstack.user.model.FailedKafkaEvent;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class KafkaRetryService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final FailedKafkaEventRepository failedKafkaEventRepository;

    public KafkaRetryService(KafkaTemplate<String, String> kafkaTemplate,
	    FailedKafkaEventRepository failedKafkaEventRepository) {
	this.kafkaTemplate = kafkaTemplate;
	this.failedKafkaEventRepository = failedKafkaEventRepository;
    }

    @Scheduled(fixedDelay = 10000)
    public void retryFailedEvents() {
	if (!RetryUtility.SHOULDRETRYKAFKA.get())
	    return;
	if (testKafka()) {
	    failedKafkaEventRepository.findAll().flatMap(this::sendAndDeleteEvent).doOnComplete(this::checkAndStopRetry)
		    .subscribeOn(Schedulers.boundedElastic()).subscribe();
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

    private Mono<Void> sendAndDeleteEvent(FailedKafkaEvent event) {
	return Mono.fromFuture(() -> kafkaTemplate.send(event.getTopic(), event.getEventData())).flatMap(sendResult -> {
	    if (sendResult.getRecordMetadata() != null) {
		return failedKafkaEventRepository.delete(event);
	    } else {
		return Mono.empty();
	    }
	}).onErrorResume(ex -> Mono.empty());
    }

    private void checkAndStopRetry() {
	failedKafkaEventRepository.count().doOnSuccess(count -> {
	    if (count == 0) {
		RetryUtility.SHOULDRETRYKAFKA.set(false);
	    }
	}).subscribe();
    }
}
