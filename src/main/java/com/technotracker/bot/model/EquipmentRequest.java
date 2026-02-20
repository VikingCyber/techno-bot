package com.technotracker.bot.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class EquipmentRequest {

    /** Свободный текст описания заявки (=comment) */
    @JsonProperty("text")
    private String text;

    /** Строковое описание оборудования (если без справочника) */
    @JsonProperty("equipment_string")
    private String equipmentString;

    /** Время проведения (строка, не RFC3339) */
    @JsonProperty("schedule_time")
    private String scheduleTime;

    /** Адрес / локация */
    @JsonProperty("address")
    private String address;

    /** Telegram user id */
    @JsonProperty("user_id")
    private Long userId;

    /** Telegram username */
    @JsonProperty("user_name")
    private String userName;

    /** Список оборудования из справочника (может быть пустым) */
    @JsonProperty("equipments")
    private List<EquipmentItem> equipments;

    public EquipmentRequest() {}

    /** Короткий конструктор для создания заявки из текстового сообщения */
    public EquipmentRequest(Long userId, String userName, String equipmentString,
                            String address, String text, String scheduleTime) {
        this.userId = userId;
        this.userName = userName;
        this.equipmentString = equipmentString;
        this.address = address;
        this.text = text;
        this.scheduleTime = scheduleTime;
        this.equipments = List.of();
    }

    // Backward-compat helpers used in MessageHandler
    public String getEquipmentName()  { return equipmentString; }
    public String getLocation()       { return address; }
    public String getComment()        { return text; }

    @Override
    public String toString() {
        return "EquipmentRequest{userId=" + userId + ", equipmentString='" + equipmentString
                + "', address='" + address + "', scheduleTime='" + scheduleTime + "'}";
    }

    // ---- вложенный класс элемента оборудования ----
    @Getter
    @Setter
    public static class EquipmentItem {
        @JsonProperty("id")
        private Integer id;

        @JsonProperty("quantity")
        private Integer quantity;

        public EquipmentItem() {}

        public EquipmentItem(Integer id, Integer quantity) {
            this.id = id;
            this.quantity = quantity;
        }
    }
}
