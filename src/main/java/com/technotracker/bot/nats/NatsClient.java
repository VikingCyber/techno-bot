package com.technotracker.bot.nats;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.technotracker.bot.config.NatsConfig;
import com.technotracker.bot.model.EquipmentRequest;
import com.technotracker.bot.model.EquipmentStatus;
import com.technotracker.bot.model.RequestUpdatedEvent;
import io.nats.client.Connection;
import io.nats.client.JetStream;
import io.nats.client.JetStreamApiException;
import io.nats.client.JetStreamSubscription;
import io.nats.client.Message;
import io.nats.client.Nats;
import io.nats.client.Subscription;
import io.nats.client.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Component
public class NatsClient {
    private static final Logger logger = LoggerFactory.getLogger(NatsClient.class);

    private final NatsConfig natsConfig;
    private final ObjectMapper objectMapper;
    private Connection connection;
    private JetStream jetStream;
    private JetStreamSubscription subscription;
    private Subscription subscriptionRequestsUpdated;
    private volatile Consumer<RequestUpdatedEvent> requestsUpdatedHandler;

    public NatsClient(NatsConfig natsConfig) {
        this.natsConfig = natsConfig;
        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    public void connect() {
        // Allow running the application without NATS for local UI/debug runs.
        if (!natsConfig.isEnabled()) {
            logger.info("NATS connection is disabled via configuration (nats.enabled=false). Skipping connect.");
            return;
        }

        try {
            Options options = new Options.Builder()
                    .server(natsConfig.getUrl())
                    .maxReconnects(-1)
                    .reconnectWait(Duration.ofSeconds(2))
                    .connectionListener((conn, type) -> {
                        logger.info("NATS connection event: {}", type);
                    })
                    .build();

            connection = Nats.connect(options);
            jetStream = connection.jetStream();

            logger.info("Connected to NATS at {}", natsConfig.getUrl());

            // Подписка на уведомления о статусе оборудования
            subscribeToStatusUpdates();
            // Подписка на обновления запросов (requests.updated)
            subscriptionRequestsUpdated = connection.subscribe(natsConfig.getRequestsUpdatedSubject());
            logger.info("Subscribed to request updates on subject: {}", natsConfig.getRequestsUpdatedSubject());
        } catch (Exception e) {
            // Do not fail application startup if NATS is unavailable; only log the error.
            logger.warn("Failed to connect to NATS (continuing without NATS): {}", e.toString());
            logger.debug("NATS connection failure details", e);
        }
    }

    /**
     * Публикует запрос на получение оборудования
     */
    public void publishEquipmentRequest(EquipmentRequest request) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(request);
        if (connection == null) {
            logger.warn("Nats connection is not available - skipping publish of equipment request: {}", request);
            return;
        }

        connection.request(natsConfig.getRequestCreatedSubject(), json.getBytes(StandardCharsets.UTF_8)).
                thenAccept(response -> {
                    logger.info("Received response for equipment request: {}", new String(response.getData(), StandardCharsets.UTF_8));
                });
        logger.info("Published equipment request: {}", request);
    }

    /**
     * Публикует сообщение в топик без request-reply (для Outbox worker).
     */
    public boolean publishToSubject(String subject, String jsonPayload) {
        if (connection == null) {
            logger.warn("NATS connection not available - skip publish to {}", subject);
            return false;
        }
        try {
            connection.publish(subject, jsonPayload.getBytes(StandardCharsets.UTF_8));
            logger.debug("Published to {}: {} bytes", subject, jsonPayload.length());
            return true;
        } catch (Exception e) {
            logger.error("Failed to publish to {}", subject, e);
            return false;
        }
    }

    /**
     * Подписывается на обновления статуса оборудования
     */
    private void subscribeToStatusUpdates() {
        try {
            io.nats.client.PushSubscribeOptions subscribeOptions = io.nats.client.PushSubscribeOptions.builder()
                    .durable("telegram-bot-subscriber")
                    .build();

            subscription = jetStream.subscribe(natsConfig.getSubject() + ".status", subscribeOptions);
            logger.info("Subscribed to status updates on subject: {}", natsConfig.getSubject() + ".status");
        } catch (IOException | JetStreamApiException e) {
            logger.error("Failed to subscribe to status updates", e);
        }
    }

    /**
     * Обрабатывает сообщения о статусе оборудования
     */
    public void setStatusUpdateHandler(Consumer<EquipmentStatus> handler) {
        new Thread(() -> {
            try {
                while (connection != null && connection.getStatus() == Connection.Status.CONNECTED) {
                    if (subscription == null) {
                        continue;
                    }

                    Message msg = subscription.nextMessage(Duration.ofSeconds(1));
                    if (msg == null) {
                        continue;
                    }

                    try {
                        String json = new String(msg.getData(), StandardCharsets.UTF_8);
                        EquipmentStatus status = objectMapper.readValue(json, EquipmentStatus.class);
                        handler.accept(status);
                        msg.ack();
                    } catch (Exception e) {
                        logger.error("Error processing status update", e);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Status update handler interrupted", e);
            }
        }).start();
    }

    /**
     * Подписывается на события requests.updated и вызывает handler при получении сообщения.
     */
    public void setRequestsUpdatedHandler(Consumer<RequestUpdatedEvent> handler) {
        this.requestsUpdatedHandler = handler;
        new Thread(() -> {
            try {
                while (connection != null && connection.getStatus() == Connection.Status.CONNECTED
                        && subscriptionRequestsUpdated != null) {
                    Message msg = subscriptionRequestsUpdated.nextMessage(Duration.ofSeconds(1));
                    if (msg == null || requestsUpdatedHandler == null) {
                        continue;
                    }
                    try {
                        String json = new String(msg.getData(), StandardCharsets.UTF_8);
                        RequestUpdatedEvent event = objectMapper.readValue(json, RequestUpdatedEvent.class);
                        requestsUpdatedHandler.accept(event);
                    } catch (Exception e) {
                        logger.error("Error processing request updated event", e);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("Requests updated handler interrupted", e);
            }
        }, "nats-requests-updated").start();
    }

    /**
     * Проверяет, подключен ли NATS
     */
    public boolean isConnected() {
        return connection != null
                && connection.getStatus() == Connection.Status.CONNECTED
                && jetStream != null;
    }

    @PreDestroy
    public void disconnect() {
        try {
            if (subscription != null) {
                subscription.unsubscribe();
            }
            if (subscriptionRequestsUpdated != null) {
                subscriptionRequestsUpdated.unsubscribe();
            }
            if (connection != null) {
                connection.close();
            }
            logger.info("Disconnected from NATS");
        } catch (Exception e) {
            logger.error("Error disconnecting from NATS", e);
        }
    }
}

