package com.technotracker.bot.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Запрос от пользователя в БД: id, issuer_id, raw_text, created_at, status, responsible_info.
 */
@Entity
@Table(name = "user_requests")
public class UserRequest {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "issuer_id", nullable = false)
    private Long issuerId;

    @Column(name = "raw_text", nullable = false, length = 4096)
    private String rawText;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "status", length = 64)
    private String status;

    @Column(name = "responsible_info", length = 1024)
    private String responsibleInfo;

    @PrePersist
    public void onPrePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (status == null) {
            status = "CREATED";
        }
    }

    public UserRequest() {
    }

    public UserRequest(Long issuerId, String rawText, Instant createdAt, String status, String responsibleInfo) {
        this.issuerId = issuerId;
        this.rawText = rawText;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.status = status != null ? status : "CREATED";
        this.responsibleInfo = responsibleInfo;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Long getIssuerId() { return issuerId; }
    public void setIssuerId(Long issuerId) { this.issuerId = issuerId; }
    public String getRawText() { return rawText; }
    public void setRawText(String rawText) { this.rawText = rawText; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getResponsibleInfo() { return responsibleInfo; }
    public void setResponsibleInfo(String responsibleInfo) { this.responsibleInfo = responsibleInfo; }
}
