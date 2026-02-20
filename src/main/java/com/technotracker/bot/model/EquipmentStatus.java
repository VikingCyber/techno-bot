package com.technotracker.bot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Модель заявки согласно контракту сервиса Requests (domain.Request).
 * Используется как при создании заявки, так и при получении статуса.
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class EquipmentStatus {

    @JsonProperty("id")
    private String id;

    @JsonProperty("request_text")
    private String requestText;

    /** pending | assigned | in_progress | completed | canceled | rejected */
    @JsonProperty("status")
    private String status;

    @JsonProperty("schedule_time")
    private String scheduleTime;

    @JsonProperty("end_time")
    private String endTime;

    @JsonProperty("address")
    private String address;

    @JsonProperty("responsible_user_id")
    private String responsibleUserId;

    @JsonProperty("equipment")
    private List<RequestEquipmentItem> equipment;

    @JsonProperty("audit")
    private Audit audit;

    // ---- Вспомогательное отображение статуса в текст ----
    public String statusLabel() {
        if (status == null) return "—";
        return switch (status) {
            case "pending"     -> "⏳ Ожидает";
            case "assigned"    -> "👤 Назначена";
            case "in_progress" -> "🔄 В работе";
            case "completed"   -> "✅ Выполнена";
            case "canceled"    -> "❌ Отменена";
            case "rejected"    -> "🚫 Отклонена";
            default            -> status;
        };
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RequestEquipmentItem {
        @JsonProperty("request_id")
        private String requestId;

        @JsonProperty("equipment_id")
        private Integer equipmentId;

        @JsonProperty("quantity")
        private Integer quantity;

        @JsonProperty("created_at")
        private String createdAt;

        @JsonProperty("updated_at")
        private String updatedAt;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Audit {
        @JsonProperty("created_at")
        private String createdAt;

        @JsonProperty("updated_at")
        private String updatedAt;

        @JsonProperty("created_by")
        private String createdBy;

        @JsonProperty("updated_by")
        private String updatedBy;

        @JsonProperty("deleted_at")
        private String deletedAt;
    }
}
