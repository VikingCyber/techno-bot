package com.technotracker.bot;

import com.technotracker.bot.config.BotConfig;
import com.technotracker.bot.handlers.CommandHandler;
import com.technotracker.bot.handlers.MessageHandler;
import com.technotracker.bot.handlers.CallbackHandler;
import com.technotracker.bot.nats.NatsClient;
import com.technotracker.bot.model.EquipmentStatus;
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

    public TechnoTrackerBot(BotConfig botConfig, NatsClient natsClient) {
        super(botConfig.getToken());
        this.botConfig = botConfig;
        this.natsClient = natsClient;
        this.commandHandler = new CommandHandler(this, natsClient);
        this.messageHandler = new MessageHandler(this, natsClient);
        this.callbackHandler = new CallbackHandler(this, natsClient);
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
     * Отправляет уведомление об изменении статуса заявки.
     * Вызывается внешним компонентом (например, при входящем Pub/Sub событии).
     *
     * @param status  объект заявки из контракта
     * @param chatId  Telegram chat id получателя
     */
    public void sendStatusUpdate(EquipmentStatus status, Long chatId) {
        String message = String.format(
            "📦 <b>Обновление статуса заявки</b>\n\n" +
            "🆔 ID: <code>%s</code>\n" +
            "📝 Описание: %s\n" +
            "📍 Адрес: %s\n" +
            "📅 Время: %s\n" +
            "Статус: %s",
            status.getId(),
            status.getRequestText() != null ? status.getRequestText() : "—",
            status.getAddress() != null ? status.getAddress() : "—",
            status.getScheduleTime() != null ? status.getScheduleTime() : "—",
            status.statusLabel()
        );

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId.toString());
        sendMessage.setText(message);
        sendMessage.setParseMode("HTML");

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            logger.error("Failed to send status update to chat {}", chatId, e);
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

}






