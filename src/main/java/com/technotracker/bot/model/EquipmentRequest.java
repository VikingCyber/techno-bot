package com.technotracker.bot.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class EquipmentRequest {
    @JsonProperty("user_id")
    private Long userId;
    
    @JsonProperty("user_name")
    private String userName;
    
    @JsonProperty("equipment_id")
    private Long equipmentId;
    
    @JsonProperty("equipment_name")
    private String equipmentName;
    
    @JsonProperty("location")
    private String location;
    
    @JsonProperty("comment")
    private String comment;
    
    @JsonProperty("request_date")
    private LocalDateTime requestDate;
    
    @JsonProperty("request_id")
    private String requestId;
    
    public EquipmentRequest() {
    }
    
    public EquipmentRequest(Long userId, String userName, Long equipmentId, String equipmentName, String location, String comment, LocalDateTime requestDate, String requestId) {
        this.userId = userId;
        this.userName = userName;
        this.equipmentId = equipmentId;
        this.equipmentName = equipmentName;
        this.location = location;
        this.comment = comment;
        this.requestDate = requestDate;
        this.requestId = requestId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
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
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getComment() {
        return comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    public LocalDateTime getRequestDate() {
        return requestDate;
    }
    
    public void setRequestDate(LocalDateTime requestDate) {
        this.requestDate = requestDate;
    }
    
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}






