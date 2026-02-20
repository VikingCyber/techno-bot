package com.technotracker.bot.handlers;

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

import java.util.ArrayList;
import java.util.List;

public class CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(CommandHandler.class);

    private final TelegramLongPollingBot bot;
    private final NatsClient natsClient;

    public CommandHandler(TelegramLongPollingBot bot, NatsClient natsClient) {
        this.bot = bot;
        this.natsClient = natsClient;
    }

    public void handle(Update update) {
        Message message = update.getMessage();
        String command = message.getText().split(" ")[0];
        Long chatId = message.getChatId();
        Long userId = message.getFrom().getId();

        switch (command) {
            case "/start"   -> handleStart(chatId);
            case "/help"    -> handleHelp(chatId);
            case "/request" -> handleRequest(chatId);
            case "/status"  -> handleStatus(chatId, userId);
            case "/catalog" -> handleCatalog(chatId);
            default         -> sendMessage(chatId,
                    "Неизвестная команда. Используйте /help для списка доступных команд.",
                    buildMainMenuKeyboard());
        }
    }

    // ------------------------------------------------------------------
    // Handlers
    // ------------------------------------------------------------------

    private void handleStart(Long chatId) {
        String welcome = """
            👋 Добро пожаловать в Techno Tracker!
            
            Я помогу вам:
            • Запросить оборудование
            • Проверить статус заявок
            • Просмотреть каталог оборудования
            
            Используйте кнопки ниже или /help для списка команд.
            """;
        sendMessage(chatId, welcome, buildMainMenuKeyboard());
    }

    private void handleHelp(Long chatId) {
        String help = """
            📋 Доступные команды:
            
            /start   — Начать работу с ботом
            /help    — Показать это сообщение
            /request — Оформить запрос на оборудование
            /status  — Проверить статус ваших заявок
            /catalog — Просмотреть каталог оборудования
            
            💡 Просто отправьте команду, и я помогу вам!
            """;
        sendMessage(chatId, help, buildMainMenuKeyboard());
    }

    private void handleRequest(Long chatId) {
        String message = """
            📝 Оформление запроса на оборудование

            Отправьте сообщение в следующем формате (регистр не важен):

            <code>Запрос: [название оборудования]
            Локация: [ваша локация]
            Время: [время мероприятия]
            Комментарий: [дополнительная информация]</code>

            Пример:
            <code>ЗАПРОС: Проектор
            локация: Аудитория 1
            ВРЕМЯ: 15:00
            Комментарий: Для презентации</code>
            """;
        sendMessage(chatId, message);
    }

    private void handleStatus(Long chatId, Long userId) {
        if (!natsClient.isConnected()) {
            sendMessage(chatId, """
                📊 Статус заявок
                
                ⚠️ NATS не подключён — работа в демо-режиме.
                Подключитесь к серверу, чтобы видеть реальные заявки.
                """, buildStatusKeyboard());
            return;
        }

        List<EquipmentStatus> requests = natsClient.getUserRequests(userId);

        if (requests.isEmpty()) {
            sendMessage(chatId, """
                📊 Статус заявок
                
                У вас пока нет активных заявок.
                Используйте кнопку ниже, чтобы создать новую.
                """, buildStatusKeyboard());
            return;
        }

        StringBuilder sb = new StringBuilder("📊 <b>Ваши заявки:</b>\n\n");
        for (EquipmentStatus r : requests) {
            sb.append(String.format(
                    "🆔 <code>%s</code>\n🔧 %s\n📍 %s\n📅 %s\n%s\n\n",
                    r.getId(),
                    r.getRequestText() != null ? r.getRequestText() : "—",
                    r.getAddress() != null ? r.getAddress() : "—",
                    r.getScheduleTime() != null ? r.getScheduleTime() : "—",
                    r.statusLabel()
            ));
        }

        sendMessage(chatId, sb.toString(), buildStatusKeyboard());
    }

    private void handleCatalog(Long chatId) {
        String message = """
            📚 Каталог оборудования
            
            Запрашиваю каталог оборудования...
            
            (Эта функция будет доступна после интеграции справочника)
            """;
        sendMessage(chatId, message, buildCatalogKeyboard());
    }

    // ------------------------------------------------------------------
    // Keyboards
    // ------------------------------------------------------------------

    private InlineKeyboardMarkup buildMainMenuKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(btn("📝 Запросить оборудование", "action_request"));

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(btn("📊 Статус заявок", "action_status"));
        row2.add(btn("📚 Каталог", "action_catalog"));

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(btn("❓ Помощь", "action_help"));

        rows.add(row1);
        rows.add(row2);
        rows.add(row3);

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(rows);
        return keyboard;
    }

    private InlineKeyboardMarkup buildStatusKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(btn("🔄 Обновить", "action_status"));
        row.add(btn("📝 Новый запрос", "action_request"));
        row.add(btn("◀️ Назад", "action_start"));
        rows.add(row);

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(rows);
        return keyboard;
    }

    private InlineKeyboardMarkup buildCatalogKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(btn("🔄 Обновить", "action_catalog"));
        row.add(btn("◀️ Назад", "action_start"));
        rows.add(row);

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(rows);
        return keyboard;
    }

    private static InlineKeyboardButton btn(String text, String callbackData) {
        InlineKeyboardButton b = new InlineKeyboardButton();
        b.setText(text);
        b.setCallbackData(callbackData);
        return b;
    }

    // ------------------------------------------------------------------
    // Send helpers
    // ------------------------------------------------------------------

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
