package com.threadstack.user.config.keycloak;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.threadstack.user.repository.FailedKeycloakEventRepository;
import com.threadstack.user.util.RetryUtility;
import com.threadstack.user.model.FailedKeycloakEvent;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
public class KeycloakRetryService {

    private final KeycloakService keycloakService;
    private final FailedKeycloakEventRepository failedKeycloakEventRepository;

    public KeycloakRetryService(KeycloakService keycloakService,
                                FailedKeycloakEventRepository failedKeycloakEventRepository) {
        this.keycloakService = keycloakService;
        this.failedKeycloakEventRepository = failedKeycloakEventRepository;
    }

    @Scheduled(fixedDelay = 10000)
    public void retryFailedEvents() {
        if (!RetryUtility.SHOULDRETRYKEYCLOAKUSERCREATION.get()) return;
       
        if (keycloakService.isKeycloakAlive()) {
            failedKeycloakEventRepository.findAll()
                .flatMap(this::processAndDeleteEvent)
                .doOnComplete(this::checkAndStopRetry)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
        }
    }

    private Mono<Void> processAndDeleteEvent(FailedKeycloakEvent event) {
        return Mono.fromRunnable(() -> {
            switch (event.getEventType()) {
                case "CREATE_USER":
                    keycloakService.createUser(event.getUsername(), event.getEmail(), event.getPassword());
                    break;
                case "ASSIGN_ROLE":
                    keycloakService.assignRoleToUser(event.getUsername(), event.getRoleName());
                    break;
                case "CREATE_ROLE":
                    keycloakService.createRoleIfNotExists(event.getRoleName());
                    break;
                default:
                    System.err.println("Unknown Keycloak event type: " + event.getEventType());
                    return;
            }
        })
        .flatMap(result -> failedKeycloakEventRepository.delete(event))
        .onErrorResume(ex -> Mono.empty());
    }

    private void checkAndStopRetry() {
        failedKeycloakEventRepository.count()
            .doOnSuccess(count -> {
                if (count == 0) {
                    RetryUtility.SHOULDRETRYKEYCLOAKUSERCREATION.set(false);
                }
            })
            .subscribe();
    }
}
