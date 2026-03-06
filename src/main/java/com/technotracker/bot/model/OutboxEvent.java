package com.technotracker.bot.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Transactional Outbox: событие для гарантированной отправки в NATS.
 */
@Entity
@Table(name = "outbox_events", indexes = @Index(name = "idx_outbox_sent_at", columnList = "sent_at"))
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "topic", nullable = false, length = 256)
    private String topic;

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "sent_at")
    private Instant sentAt;

    @PrePersist
    public void onPrePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public OutboxEvent() {
    }

    public OutboxEvent(String topic, String payload, Instant createdAt, Instant sentAt) {
        this.topic = topic;
        this.payload = payload;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.sentAt = sentAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getSentAt() { return sentAt; }
    public void setSentAt(Instant sentAt) { this.sentAt = sentAt; }
}
