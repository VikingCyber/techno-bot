package com.technotracker.bot.nats;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.technotracker.bot.config.NatsConfig;
import com.technotracker.bot.model.EquipmentRequest;
import com.technotracker.bot.model.EquipmentStatus;
import com.technotracker.bot.model.GatewayResponse;
import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.Nats;
import io.nats.client.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class NatsClient {
    private static final Logger logger = LoggerFactory.getLogger(NatsClient.class);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);

    private final NatsConfig natsConfig;
    private final ObjectMapper objectMapper;
    private Connection connection;

    public NatsClient(NatsConfig natsConfig) {
        this.natsConfig = natsConfig;
        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    public void connect() {
        if (!natsConfig.isEnabled()) {
            logger.info("NATS connection is disabled via configuration (nats.enabled=false). Skipping connect.");
            return;
        }

        try {
            Options options = new Options.Builder()
                    .server(natsConfig.getUrl())
                    .maxReconnects(-1)
                    .reconnectWait(Duration.ofSeconds(2))
                    .connectionListener((conn, type) -> logger.info("NATS connection event: {}", type))
                    .build();

            connection = Nats.connect(options);
            logger.info("Connected to NATS at {}", natsConfig.getUrl());
        } catch (Exception e) {
            logger.warn("Failed to connect to NATS (continuing without NATS): {}", e.toString());
            logger.debug("NATS connection failure details", e);
        }
    }

    // -------------------------------------------------------------------------
    // Request-Reply helpers
    // -------------------------------------------------------------------------

    /**
     * Создаёт заявку через NATS (bot.requests.created).
     * Возвращает созданную заявку или null при ошибке.
     */
    public EquipmentStatus publishEquipmentRequest(EquipmentRequest request) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(request);
        logger.info("Publishing equipment request to {}: {}", natsConfig.getRequestCreatedSubject(), json);

        GatewayResponse response = requestReply(natsConfig.getRequestCreatedSubject(), json);
        if (response == null || !response.isSuccess() || response.getData() == null) {
            logger.warn("Equipment request failed: {}", response != null ? response.getMessage() : "no response");
            return null;
        }
        return objectMapper.treeToValue(response.getData(), EquipmentStatus.class);
    }

    /**
     * Получает список заявок пользователя по Telegram ID (bot.requests.list).
     */
    public List<EquipmentStatus> getUserRequests(Long telegramId) {
        try {
            Map<String, Object> payload = Map.of(
                    "telegram_id", telegramId,
                    "limit", 20,
                    "offset", 0
            );
            String json = objectMapper.writeValueAsString(payload);
            GatewayResponse response = requestReply(natsConfig.getRequestListSubject(), json);
            if (response == null || !response.isSuccess() || response.getData() == null) {
                logger.warn("getUserRequests failed: {}", response != null ? response.getMessage() : "no response");
                return Collections.emptyList();
            }
            return objectMapper.readValue(
                    objectMapper.treeAsTokens(response.getData()),
                    new TypeReference<List<EquipmentStatus>>() {}
            );
        } catch (Exception e) {
            logger.error("Error fetching user requests", e);
            return Collections.emptyList();
        }
    }

    /**
     * Отменяет заявку (bot.requests.cancel).
     */
    public boolean cancelRequest(String requestId) {
        try {
            String json = objectMapper.writeValueAsString(Map.of("request_id", requestId));
            GatewayResponse response = requestReply(natsConfig.getRequestDeleteSubject(), json);
            if (response == null) return false;
            if (!response.isSuccess()) {
                logger.warn("cancelRequest failed: {}", response.getMessage());
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.error("Error canceling request {}", requestId, e);
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // Internal
    // -------------------------------------------------------------------------

    private GatewayResponse requestReply(String subject, String jsonPayload) {
        if (connection == null) {
            logger.warn("NATS connection is not available, skipping requestReply to {}", subject);
            return null;
        }
        try {
            Message reply = connection.request(
                    subject,
                    jsonPayload.getBytes(StandardCharsets.UTF_8),
                    REQUEST_TIMEOUT
            );
            if (reply == null) {
                logger.warn("No reply received from {} within timeout", subject);
                return null;
            }
            String replyJson = new String(reply.getData(), StandardCharsets.UTF_8);
            logger.debug("Reply from {}: {}", subject, replyJson);
            return objectMapper.readValue(replyJson, GatewayResponse.class);
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) Thread.currentThread().interrupt();
            logger.error("Request-Reply error on subject {}", subject, e);
            return null;
        }
    }

    /**
     * Проверяет, подключен ли NATS
     */
    public boolean isConnected() {
        return connection != null && connection.getStatus() == Connection.Status.CONNECTED;
    }

    @PreDestroy
    public void disconnect() {
        try {
            if (connection != null) {
                connection.close();
            }
            logger.info("Disconnected from NATS");
        } catch (Exception e) {
            logger.error("Error disconnecting from NATS", e);
        }
    }
}
