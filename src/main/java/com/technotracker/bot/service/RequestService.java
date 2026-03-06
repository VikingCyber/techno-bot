package com.technotracker.bot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.technotracker.bot.config.NatsConfig;
import com.technotracker.bot.model.EquipmentRequest;
import com.technotracker.bot.model.OutboxEvent;
import com.technotracker.bot.model.UserRequest;
import com.technotracker.bot.repository.OutboxEventRepository;
import com.technotracker.bot.repository.UserRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Сохраняет запрос в БД и кладёт событие в Outbox для отправки в NATS (Transactional Outbox).
 */
@Service
public class RequestService {

    private static final Logger logger = LoggerFactory.getLogger(RequestService.class);

    private final UserRequestRepository userRequestRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final NatsConfig natsConfig;
    private final ObjectMapper objectMapper;

    @Value("${app.outbox.enabled:true}")
    private boolean outboxEnabled;

    public RequestService(UserRequestRepository userRequestRepository,
                          OutboxEventRepository outboxEventRepository,
                          NatsConfig natsConfig,
                          ObjectMapper objectMapper) {
        this.userRequestRepository = userRequestRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.natsConfig = natsConfig;
        this.objectMapper = objectMapper;
    }

    /**
     * Сохраняет запрос в БД и либо пишет в Outbox (если включён), либо возвращает payload для прямой отправки.
     * При outbox=true в одной транзакции сохраняются UserRequest и OutboxEvent.
     */
    @Transactional(rollbackFor = Exception.class)
    public UserRequest saveRequestAndEnqueueForNats(Long issuerId, String rawText, EquipmentRequest equipmentRequest)
            throws JsonProcessingException {
        UserRequest entity = new UserRequest(issuerId, rawText, Instant.now(), "CREATED", null);
        userRequestRepository.save(entity);

        equipmentRequest.setRequestId(entity.getId());
        String payload = objectMapper.writeValueAsString(equipmentRequest);

        if (outboxEnabled) {
            OutboxEvent outbox = new OutboxEvent(
                    natsConfig.getRequestCreatedSubject(),
                    payload,
                    Instant.now(),
                    null
            );
            outboxEventRepository.save(outbox);
            logger.debug("Saved UserRequest {} and OutboxEvent for topic {}", entity.getId(), outbox.getTopic());
        }
        return entity;
    }

    public boolean isOutboxEnabled() {
        return outboxEnabled;
    }
}
