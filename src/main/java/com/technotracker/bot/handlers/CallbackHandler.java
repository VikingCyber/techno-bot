package com.technotracker.bot.handlers;

import com.technotracker.bot.model.EquipmentStatus;
import com.technotracker.bot.nats.NatsClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

public class CallbackHandler {
    private static final Logger logger = LoggerFactory.getLogger(CallbackHandler.class);

    private final TelegramLongPollingBot bot;
    private final NatsClient natsClient;

    public CallbackHandler(TelegramLongPollingBot bot, NatsClient natsClient) {
        this.bot = bot;
        this.natsClient = natsClient;
    }

    public void handle(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        String data   = callbackQuery.getData();
        Long chatId   = callbackQuery.getMessage().getChatId();
        Long userId   = callbackQuery.getFrom().getId();

        try {
            AnswerCallbackQuery answer = new AnswerCallbackQuery();
            answer.setCallbackQueryId(callbackQuery.getId());
            bot.execute(answer);

            if (data.startsWith("action_")) {
                handleActionCallback(data, chatId, userId);
            } else if (data.startsWith("cancel_")) {
                handleCancelCallback(data, chatId);
            } else if (data.startsWith("equipment_")) {
                handleEquipmentCallback(data, chatId);
            } else if (data.startsWith("status_")) {
                handleStatusCallback(data, chatId);
            } else {
                sendMessage(chatId, "Неизвестная команда.", buildMainMenuKeyboard());
            }
        } catch (TelegramApiException e) {
            logger.error("Error handling callback", e);
        }
    }

    // ------------------------------------------------------------------
    // Action handlers
    // ------------------------------------------------------------------

    private void handleActionCallback(String data, Long chatId, Long userId) {
        String action = data.replace("action_", "");
        switch (action) {
            case "start"   -> handleStart(chatId);
            case "help"    -> handleHelp(chatId);
            case "request" -> handleRequest(chatId);
            case "status"  -> handleStatus(chatId, userId);
            case "catalog" -> handleCatalog(chatId);
            default        -> sendMessage(chatId, "Неизвестное действие.", buildMainMenuKeyboard());
        }
    }

    private void handleStart(Long chatId) {
        String welcome = """
            👋 Добро пожаловать в Techno Tracker!
            
            Я помогу вам:
            • Запросить оборудование
            • Проверить статус заявок
            • Просмотреть каталог оборудования
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

    private void handleCancelCallback(String data, Long chatId) {
        String requestId = data.replace("cancel_", "");
        if (!natsClient.isConnected()) {
            sendMessage(chatId, "⚠️ Нет подключения к серверу. Отмена недоступна.", buildMainMenuKeyboard());
            return;
        }
        boolean ok = natsClient.cancelRequest(requestId);
        sendMessage(chatId,
                ok ? "✅ Заявка <code>" + requestId + "</code> отменена."
                   : "❌ Не удалось отменить заявку. Попробуйте позже.",
                buildMainMenuKeyboard());
    }

    private void handleEquipmentCallback(String data, Long chatId) {
        String equipmentId = data.replace("equipment_", "");
        sendMessage(chatId, "Обработка выбора оборудования: " + equipmentId, buildMainMenuKeyboard());
    }

    private void handleStatusCallback(String data, Long chatId) {
        String requestId = data.replace("status_", "");
        sendMessage(chatId, "Проверка статуса заявки: " + requestId, buildMainMenuKeyboard());
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
