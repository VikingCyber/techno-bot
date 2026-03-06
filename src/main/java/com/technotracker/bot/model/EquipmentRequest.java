package com.technotracker.bot.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class EquipmentRequest {
    @JsonProperty("request_id")
    private UUID requestId;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("user_name")
    private String userName;

    @JsonProperty("equipment_string")
    private String equipmentName;

    @JsonProperty("location")
    private String location;

    @JsonProperty("text")
    private String comment;

    @JsonProperty("address")
    private String address;

    @JsonProperty("schedule_time")
    private String requestDate;

    public EquipmentRequest() {
    }

    public EquipmentRequest(UUID requestId, Long userId, String userName, String equipmentName, String location, String comment, String requestDate, String address) {
        this.requestId = requestId;
        this.userId = userId;
        this.userName = userName;
        this.equipmentName = equipmentName;
        this.location = location;
        this.comment = comment;
        this.requestDate = requestDate;
        this.address = address;
    }

    public UUID getRequestId() {
        return requestId;
    }

    public void setRequestId(UUID requestId) {
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

    public String getEquipmentName() { return equipmentName; }
    public void setEquipmentName(String equipmentName) {
        this.equipmentName = equipmentName;
    }

    public String getLocation() { return location; }
    public void setLocation(String location) {
        this.location = location;
    }

    public String getComment() { return comment; }
    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(String requestDate) {
        this.requestDate = requestDate;
    }
}






