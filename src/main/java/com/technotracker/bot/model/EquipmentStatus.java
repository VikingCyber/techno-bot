package com.technotracker.bot.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class EquipmentStatus {
    @JsonProperty("request_id")
    private String requestId;
    
    @JsonProperty("equipment_id")
    private Long equipmentId;
    
    @JsonProperty("equipment_name")
    private String equipmentName;
    
    @JsonProperty("status")
    private String status; // PENDING, APPROVED, REJECTED, IN_DELIVERY, DELIVERED
    
    @JsonProperty("user_id")
    private Long userId;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
    
    public EquipmentStatus() {
    }
    
    public EquipmentStatus(String requestId, Long equipmentId, String equipmentName, String status, Long userId, String message, LocalDateTime updatedAt) {
        this.requestId = requestId;
        this.equipmentId = equipmentId;
        this.equipmentName = equipmentName;
        this.status = status;
        this.userId = userId;
        this.message = message;
        this.updatedAt = updatedAt;
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    public Long getEquipmentId() {
        return equipmentId;
    }
    
    public void setEquipmentId(Long equipmentId) {
        this.equipmentId = equipmentId;
    }
    
    public String getEquipmentName() {
        return equipmentName;
    }
    
    public void setEquipmentName(String equipmentName) {
        this.equipmentName = equipmentName;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}






