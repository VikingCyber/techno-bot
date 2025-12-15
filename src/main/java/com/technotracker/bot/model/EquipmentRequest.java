package com.technotracker.bot.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

public class EquipmentRequest {
    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("user_name")
    private String userName;

    @Getter
    @JsonProperty("equipment_string")
    private String equipmentName;

    @Getter
    @JsonProperty("location")
    private String location;

    @Getter
    @JsonProperty("text")
    private String comment;

    @Getter
    @JsonProperty("address")
    private String address;

    @JsonProperty("schedule_time")
    private String requestDate;

    public EquipmentRequest() {
    }

    public EquipmentRequest(Long userId, String userName, String equipmentName, String location, String comment, String requestDate, String address) {
        this.userId = userId;
        this.userName = userName;
        this.equipmentName = equipmentName;
        this.location = location;
        this.comment = comment;
        this.requestDate = requestDate;
        this.address = address;
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

    public void setEquipmentName(String equipmentName) {
        this.equipmentName = equipmentName;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(String requestDate) {
        this.requestDate = requestDate;
    }
}






