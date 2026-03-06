package com.technotracker.bot.service;

import com.technotracker.bot.model.OutboxEvent;
import com.technotracker.bot.nats.NatsClient;
import com.technotracker.bot.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Периодически забирает неотправленные события из Outbox и публикует в NATS (Transactional Outbox).
 */
@Component
public class OutboxWorker {

    private static final Logger logger = LoggerFactory.getLogger(OutboxWorker.class);

    private final OutboxEventRepository outboxEventRepository;
    private final NatsClient natsClient;

    @Value("${app.outbox.enabled:true}")
    private boolean outboxEnabled;

    public OutboxWorker(OutboxEventRepository outboxEventRepository, NatsClient natsClient) {
        this.outboxEventRepository = outboxEventRepository;
        this.natsClient = natsClient;
    }

    @Scheduled(fixedDelayString = "${app.outbox.poll-ms:5000}")
    @Transactional
    public void processOutbox() {
        if (!outboxEnabled || !natsClient.isConnected()) {
            return;
        }
        List<OutboxEvent> unsent = outboxEventRepository.findUnsentOrderByCreatedAt();
        for (OutboxEvent event : unsent) {
            if (natsClient.publishToSubject(event.getTopic(), event.getPayload())) {
                event.setSentAt(Instant.now());
                outboxEventRepository.save(event);
                logger.debug("Outbox event {} sent to {}", event.getId(), event.getTopic());
            }
        }
    }
}
