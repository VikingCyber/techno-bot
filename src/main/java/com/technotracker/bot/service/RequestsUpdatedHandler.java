package com.technotracker.bot.service;

import com.technotracker.bot.model.RequestUpdatedEvent;
import com.technotracker.bot.model.UserRequest;
import com.technotracker.bot.repository.UserRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Обрабатывает события requests.updated: обновляет status и responsible_info в БД по request_id.
 */
@Service
public class RequestsUpdatedHandler {

    private static final Logger logger = LoggerFactory.getLogger(RequestsUpdatedHandler.class);

    private final UserRequestRepository userRequestRepository;

    public RequestsUpdatedHandler(UserRequestRepository userRequestRepository) {
        this.userRequestRepository = userRequestRepository;
    }

    @Transactional
    public void onRequestUpdated(RequestUpdatedEvent event) {
        if (event == null || event.getRequestId() == null) {
            logger.warn("RequestUpdatedEvent missing request_id, skipping");
            return;
        }
        UUID requestId = event.getRequestId();
        Optional<UserRequest> opt = userRequestRepository.findById(requestId);
        if (opt.isEmpty()) {
            logger.debug("UserRequest not found for request_id={}, skipping", requestId);
            return;
        }
        UserRequest entity = opt.get();
        boolean changed = false;
        if (event.getStatus() != null && !event.getStatus().equals(entity.getStatus())) {
            entity.setStatus(event.getStatus());
            changed = true;
        }
        if (event.getResponsibleInfo() != null && !event.getResponsibleInfo().equals(entity.getResponsibleInfo())) {
            entity.setResponsibleInfo(event.getResponsibleInfo());
            changed = true;
        }
        if (changed) {
            userRequestRepository.save(entity);
            logger.info("Updated UserRequest {}: status={}, responsible_info={}",
                    requestId, entity.getStatus(), entity.getResponsibleInfo());
        }
    }
}
