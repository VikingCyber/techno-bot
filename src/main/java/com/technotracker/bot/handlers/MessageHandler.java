package com.technotracker.bot.handlers;

import com.technotracker.bot.model.EquipmentRequest;
import com.technotracker.bot.model.EquipmentStatus;
import com.technotracker.bot.nats.NatsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);

    private final TelegramLongPollingBot bot;
    private final NatsClient natsClient;

    // Паттерны для парсинга запроса (регистронезависимые, многострочные)
    private static final Pattern REQUEST_PATTERN = Pattern.compile(
            "^запрос[:\\.]?\\s*(.+)$",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.UNICODE_CASE);
    private static final Pattern LOCATION_PATTERN = Pattern.compile(
            "^локация[:\\.]?\\s*(.+)$",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.UNICODE_CASE);
    private static final Pattern COMMENT_PATTERN = Pattern.compile(
            "^комментарий[:\\.]?\\s*(.+)$",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.UNICODE_CASE);
    private static final Pattern TIME_PATTERN = Pattern.compile(
            "^время[:\\.]?\\s*(.+)$",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.UNICODE_CASE);

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
                    Время: [время мероприятия]
                    Комментарий: [дополнительная информация]</code>
                    
                    Или используйте команду /help для справки.
                    """, buildMainMenuKeyboard());
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
            Matcher timeMatcher = TIME_PATTERN.matcher(text);

            String equipmentName = null;
            String location = null;
            String comment = null;
            String scheduleTime = LocalDateTime.now().toString();

            if (requestMatcher.find()) equipmentName = requestMatcher.group(1).trim();
            if (locationMatcher.find()) location = locationMatcher.group(1).trim();
            if (commentMatcher.find()) comment = commentMatcher.group(1).trim();
            if (timeMatcher.find()) scheduleTime = timeMatcher.group(1).trim();

            // Минимальные требования: название оборудования
            if (equipmentName == null || equipmentName.isEmpty()) {
                return null;
            }

            return new EquipmentRequest(
                    message.getFrom().getId(),
                    message.getFrom().getUserName() != null
                            ? message.getFrom().getUserName()
                            : message.getFrom().getFirstName(),
                    equipmentName,
                    location,
                    comment,
                    scheduleTime
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
            EquipmentStatus created = null;

            if (natsConnected) {
                created = natsClient.publishEquipmentRequest(request);
            } else {
                logger.info("NATS not connected — demo mode. Request: {}", request);
            }

            String confirmation;
            if (created != null) {
                confirmation = String.format("""
                        ✅ Заявка успешно создана!
                        
                        🆔 ID: <code>%s</code>
                        🔧 Оборудование: %s
                        📍 Адрес: %s
                        📅 Время: %s
                        %s
                        
                        Статус: %s
                        Вы получите уведомление при изменении статуса.
                        """,
                        created.getId(),
                        request.getEquipmentName(),
                        created.getAddress() != null ? created.getAddress() : "Не указан",
                        created.getScheduleTime() != null ? created.getScheduleTime() : "—",
                        created.getRequestText() != null ? "💬 Комментарий: " + created.getRequestText() : "",
                        created.statusLabel()
                );
            } else {
                confirmation = String.format("""
                        %s Запрос принят!
                        
                        🔧 Оборудование: %s
                        📍 Адрес: %s
                        %s
                        """,
                        natsConnected ? "⚠️" : "📋",
                        request.getEquipmentName(),
                        request.getLocation() != null ? request.getLocation() : "Не указан",
                        natsConnected
                                ? "Ответ от сервера не получен, попробуйте позже."
                                : "⚠️ Режим демонстрации: NATS не подключён."
                );
            }

            sendMessage(chatId, confirmation, buildMainMenuKeyboard());
            logger.info("Equipment request processed: {}", request);
        } catch (Exception e) {
            logger.error("Error processing equipment request", e);
            sendMessage(chatId, "❌ Произошла ошибка при обработке запроса. Попробуйте позже.", buildMainMenuKeyboard());
        }
    }

    private InlineKeyboardMarkup buildMainMenuKeyboard() {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton requestBtn = new InlineKeyboardButton();
        requestBtn.setText("📝 Запросить оборудование");
        requestBtn.setCallbackData("action_request");
        row1.add(requestBtn);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton statusBtn = new InlineKeyboardButton();
        statusBtn.setText("📊 Статус заявок");
        statusBtn.setCallbackData("action_status");
        row2.add(statusBtn);

        InlineKeyboardButton catalogBtn = new InlineKeyboardButton();
        catalogBtn.setText("📚 Каталог");
        catalogBtn.setCallbackData("action_catalog");
        row2.add(catalogBtn);

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        InlineKeyboardButton helpBtn = new InlineKeyboardButton();
        helpBtn.setText("❓ Помощь");
        helpBtn.setCallbackData("action_help");
        row3.add(helpBtn);

        rows.add(row1);
        rows.add(row2);
        rows.add(row3);
        keyboard.setKeyboard(rows);
        return keyboard;
    }

    private void sendMessage(Long chatId, String text) {
        sendMessage(chatId, text, null);
    }

    private void sendMessage(Long chatId, String text, InlineKeyboardMarkup keyboard) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.setParseMode("HTML");

        if (keyboard != null) {
            message.setReplyMarkup(keyboard);
        }

        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            logger.error("Failed to send message", e);
        }
    }
}
