package com.technotracker.bot.handlers;

import com.technotracker.bot.model.EquipmentRequest;
import com.technotracker.bot.nats.NatsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);
    
    private final TelegramLongPollingBot bot;
    private final NatsClient natsClient;
    
    // Паттерны для парсинга запроса
    private static final Pattern REQUEST_PATTERN = Pattern.compile(
        "(?i)запрос[:\\.]?\\s*(.+)", Pattern.MULTILINE);
    private static final Pattern LOCATION_PATTERN = Pattern.compile(
        "(?i)локация[:\\.]?\\s*(.+)", Pattern.MULTILINE);
    private static final Pattern COMMENT_PATTERN = Pattern.compile(
        "(?i)комментарий[:\\.]?\\s*(.+)", Pattern.MULTILINE);

    public MessageHandler(TelegramLongPollingBot bot, NatsClient natsClient) {
        this.bot = bot;
        this.natsClient = natsClient;
    }

    public void handle(Update update) {
        Message message = update.getMessage();
        String text = message.getText();
        Long chatId = message.getChatId();
        
        // Пытаемся распарсить запрос на оборудование
        EquipmentRequest request = parseEquipmentRequest(text, message);
        
        if (request != null) {
            processEquipmentRequest(request, chatId);
        } else {
            sendMessage(chatId, """
                ❓ Не удалось распознать запрос.
                
                Используйте формат:
                <code>Запрос: [название оборудования]
                Локация: [ваша локация]
                Комментарий: [дополнительная информация]</code>
                
                Или используйте команду /help для справки.
                """);
        }
    }

    /**
     * Парсит текст сообщения и создает объект запроса на оборудование
     */
    private EquipmentRequest parseEquipmentRequest(String text, Message message) {
        try {
            Matcher requestMatcher = REQUEST_PATTERN.matcher(text);
            Matcher locationMatcher = LOCATION_PATTERN.matcher(text);
            Matcher commentMatcher = COMMENT_PATTERN.matcher(text);

            String equipmentName = null;
            String location = null;
            String comment = null;

            if (requestMatcher.find()) {
                equipmentName = requestMatcher.group(1).trim();
            }
            if (locationMatcher.find()) {
                location = locationMatcher.group(1).trim();
            }
            if (commentMatcher.find()) {
                comment = commentMatcher.group(1).trim();
            }

            // Минимальные требования: название оборудования
            if (equipmentName == null || equipmentName.isEmpty()) {
                return null;
            }

            return new EquipmentRequest(
                (long) message.getFrom().getId(),
                message.getFrom().getUserName() != null 
                    ? message.getFrom().getUserName() 
                    : message.getFrom().getFirstName(),
                null, // equipmentId будет установлен системой
                equipmentName,
                location,
                comment,
                LocalDateTime.now(),
                UUID.randomUUID().toString()
            );
        } catch (Exception e) {
            logger.error("Error parsing equipment request", e);
            return null;
        }
    }

    /**
     * Обрабатывает запрос на оборудование: отправляет через NATS и подтверждает пользователю
     */
    private void processEquipmentRequest(EquipmentRequest request, Long chatId) {
        try {
            // Отправляем запрос в систему через NATS (если подключен)
            boolean natsConnected = natsClient.isConnected();
            if (natsConnected) {
                natsClient.publishEquipmentRequest(request);
            } else {
                logger.info("NATS not connected - running in demo mode. Request: {}", request);
            }
            
            // Подтверждаем пользователю
            String confirmation = String.format("""
                ✅ Запрос успешно создан!
                
                ID заявки: <code>%s</code>
                Оборудование: %s
                Локация: %s
                %s
                
                %s
                """,
                request.getRequestId(),
                request.getEquipmentName(),
                request.getLocation() != null ? request.getLocation() : "Не указана",
                request.getComment() != null 
                    ? "Комментарий: " + request.getComment() 
                    : "",
                natsConnected 
                    ? "Ваш запрос отправлен администратору. Вы получите уведомление при изменении статуса."
                    : "⚠️ Режим демонстрации: NATS не подключен. Запрос сохранен локально."
            );
            
            sendMessage(chatId, confirmation);
            logger.info("Equipment request processed: {}", request);
        } catch (Exception e) {
            logger.error("Error processing equipment request", e);
            sendMessage(chatId, "❌ Произошла ошибка при обработке запроса. Попробуйте позже.");
        }
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.setParseMode("HTML");

        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            logger.error("Failed to send message", e);
        }
    }
}






