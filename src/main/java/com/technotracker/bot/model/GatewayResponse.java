package com.technotracker.bot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;

/**
 * Стандартная обёртка всех ответов сервиса Requests через NATS Request-Reply.
 *
 * <pre>
 * {
 *   "success": true,
 *   "message": "success",
 *   "data": { ... }
 * }
 * </pre>
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GatewayResponse {

    @JsonProperty("success")
    private boolean success;

    /** "success" | описание ошибки */
    @JsonProperty("message")
    private String message;

    /** Сырые данные ответа — десериализуются в нужный тип вызывающей стороной */
    @JsonProperty("data")
    private JsonNode data;
}

