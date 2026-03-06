package com.technotracker.bot;

import com.technotracker.bot.config.BotConfig;
import com.technotracker.bot.handlers.CommandHandler;
import com.technotracker.bot.handlers.MessageHandler;
import com.technotracker.bot.handlers.CallbackHandler;
import com.technotracker.bot.nats.NatsClient;
import com.technotracker.bot.model.EquipmentStatus;
import com.technotracker.bot.service.RequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class TechnoTrackerBot extends TelegramLongPollingBot {
    private static final Logger logger = LoggerFactory.getLogger(TechnoTrackerBot.class);
    
    private final BotConfig botConfig;
    private final NatsClient natsClient;
    private final CommandHandler commandHandler;
    private final MessageHandler messageHandler;
    private final CallbackHandler callbackHandler;

    public TechnoTrackerBot(BotConfig botConfig, NatsClient natsClient, RequestService requestService) {
        super(botConfig.getToken());
        this.botConfig = botConfig;
        this.natsClient = natsClient;
        this.commandHandler = new CommandHandler(this, natsClient);
        this.messageHandler = new MessageHandler(this, natsClient, requestService);
        this.callbackHandler = new CallbackHandler(this, natsClient);
        
        // Подписываемся на обновления статуса оборудования
        setupStatusUpdates();
    }

    @Override
    public String getBotUsername() {
        return botConfig.getUsername();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                String text = update.getMessage().getText();
                
                if (text.startsWith("/")) {
                    commandHandler.handle(update);
                } else {
                    messageHandler.handle(update);
                }
            } else if (update.hasCallbackQuery()) {
                callbackHandler.handle(update);
            }
        } catch (Exception e) {
            logger.error("Error processing update", e);
            sendError(update.getMessage().getChatId(), "Произошла ошибка при обработке запроса");
        }
    }

    /**
     * Настраивает обработку обновлений статуса оборудования из NATS
     */
    private void setupStatusUpdates() {
        // Подписываемся на обновления только если NATS подключен
        if (natsClient.isConnected()) {
            natsClient.setStatusUpdateHandler(status -> {
                try {
                    sendStatusUpdate(status);
                } catch (Exception e) {
                    logger.error("Error sending status update", e);
                }
            });
        } else {
            logger.info("NATS not connected - status updates disabled (demo mode)");
        }
    }

    /**
     * Отправляет уведомление пользователю об изменении статуса оборудования
     */
    private void sendStatusUpdate(EquipmentStatus status) {
        String message = String.format(
            "📦 Обновление статуса оборудования\n\n" +
            "Оборудование: %s\n" +
            "Статус: %s\n" +
            "Время: %s\n\n" +
            "%s",
            status.getEquipmentName(),
            formatStatus(status.getStatus()),
            status.getUpdatedAt(),
            status.getMessage() != null ? status.getMessage() : ""
        );

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(status.getUserId().toString());
        sendMessage.setText(message);
        sendMessage.setParseMode("HTML");

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            logger.error("Failed to send status update to user {}", status.getUserId(), e);
        }
    }

    /**
     * Отправляет сообщение об ошибке
     */
    public void sendError(Long chatId, String errorMessage) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("❌ " + errorMessage);
        
        try {
            execute(message);
        } catch (TelegramApiException e) {
            logger.error("Failed to send error message", e);
        }
    }

    /**
     * Форматирует статус для отображения
     */
    private String formatStatus(String status) {
        return switch (status.toUpperCase()) {
            case "PENDING" -> "⏳ Ожидает рассмотрения";
            case "APPROVED" -> "✅ Одобрено";
            case "REJECTED" -> "❌ Отклонено";
            case "IN_DELIVERY" -> "🚚 В доставке";
            case "DELIVERED" -> "✓ Доставлено";
            default -> status;
        };
    }
}






