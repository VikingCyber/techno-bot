package com.technotracker.bot.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/**
 * Контракт события requests.updated: request_id, status (опционально), responsible_info (опционально).
 */
public class RequestUpdatedEvent {

    @JsonProperty("request_id")
    private UUID requestId;

    @JsonProperty("status")
    private String status;

    @JsonProperty("responsible_info")
    private String responsibleInfo;

    public UUID getRequestId() { return requestId; }
    public void setRequestId(UUID requestId) { this.requestId = requestId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getResponsibleInfo() { return responsibleInfo; }
    public void setResponsibleInfo(String responsibleInfo) { this.responsibleInfo = responsibleInfo; }
}
