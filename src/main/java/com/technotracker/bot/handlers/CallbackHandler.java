package com.technotracker.bot.handlers;

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
        String data = callbackQuery.getData();
        Long chatId = callbackQuery.getMessage().getChatId();

        try {
            // Ответ на callback query (чтобы убрать "загрузку" у кнопки)
            AnswerCallbackQuery answer = new AnswerCallbackQuery();
            answer.setCallbackQueryId(callbackQuery.getId());
            bot.execute(answer);

            // Обработка различных типов callback данных
            if (data.startsWith("action_")) {
                handleActionCallback(data, chatId);
            } else if (data.startsWith("equipment_")) {
                handleEquipmentCallback(data, chatId);
            } else if (data.startsWith("status_")) {
                handleStatusCallback(data, chatId);
            } else {
                sendMessage(chatId, "Неизвестная команда.");
            }
        } catch (TelegramApiException e) {
            logger.error("Error handling callback", e);
        }
    }

    private void handleActionCallback(String data, Long chatId) {
        // Обработка действий из inline кнопок
        String action = data.replace("action_", "");
        
        switch (action) {
            case "start":
                handleStart(chatId);
                break;
            case "help":
                handleHelp(chatId);
                break;
            case "request":
                handleRequest(chatId);
                break;
            case "status":
                handleStatus(chatId);
                break;
            case "catalog":
                handleCatalog(chatId);
                break;
            default:
                sendMessage(chatId, "Неизвестное действие.");
        }
    }
    
    private void handleStart(Long chatId) {
        String welcome = """
            👋 Добро пожаловать в Techno Tracker!
            
            Я помогу вам:
            • Запросить оборудование
            • Проверить статус заявок
            • Просмотреть каталог оборудования
            
            Используйте /help для списка команд.
            """;
        
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
        
        sendMessage(chatId, welcome, keyboard);
    }
    
    private void handleHelp(Long chatId) {
        String help = """
            📋 Доступные команды:
            
            /start - Начать работу с ботом
            /help - Показать это сообщение
            /request - Оформить запрос на оборудование
            /status - Проверить статус ваших заявок
            /catalog - Просмотреть каталог оборудования
            
            💡 Просто отправьте команду, и я помогу вам!
            """;
        
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
        
        rows.add(row1);
        rows.add(row2);
        keyboard.setKeyboard(rows);
        
        sendMessage(chatId, help, keyboard);
    }
    
    private void handleRequest(Long chatId) {
        String message = """
            📝 Оформление запроса на оборудование
            
            Чтобы оформить запрос, отправьте сообщение в следующем формате:
            
            <code>Запрос: [название оборудования]
            Локация: [ваша локация]
            Комментарий: [дополнительная информация]</code>
            
            Пример:
            <code>Запрос: Проектор
            Локация: Аудитория 1
            Комментарий: Для презентации в 15:00</code>
            """;
        sendMessage(chatId, message);
    }
    
    private void handleStatus(Long chatId) {
        String message = """
            📊 Статус ваших заявок
            
            Запрашиваю информацию о ваших заявках...
            
            (Эта функция требует интеграции с системой для получения актуальных данных)
            """;
        
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton refreshBtn = new InlineKeyboardButton();
        refreshBtn.setText("🔄 Обновить");
        refreshBtn.setCallbackData("action_status");
        row.add(refreshBtn);
        
        InlineKeyboardButton backBtn = new InlineKeyboardButton();
        backBtn.setText("◀️ Назад");
        backBtn.setCallbackData("action_start");
        row.add(backBtn);
        
        rows.add(row);
        keyboard.setKeyboard(rows);
        
        sendMessage(chatId, message, keyboard);
    }
    
    private void handleCatalog(Long chatId) {
        String message = """
            📚 Каталог оборудования
            
            Запрашиваю каталог оборудования...
            
            (Эта функция требует интеграции с системой для получения актуального каталога)
            """;
        
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton refreshBtn = new InlineKeyboardButton();
        refreshBtn.setText("🔄 Обновить");
        refreshBtn.setCallbackData("action_catalog");
        row.add(refreshBtn);
        
        InlineKeyboardButton backBtn = new InlineKeyboardButton();
        backBtn.setText("◀️ Назад");
        backBtn.setCallbackData("action_start");
        row.add(backBtn);
        
        rows.add(row);
        keyboard.setKeyboard(rows);
        
        sendMessage(chatId, message, keyboard);
    }
    
    private void handleEquipmentCallback(String data, Long chatId) {
        // Обработка callback для выбора оборудования из каталога
        String equipmentId = data.replace("equipment_", "");
        sendMessage(chatId, "Обработка выбора оборудования: " + equipmentId);
        // Здесь можно реализовать логику создания запроса на выбранное оборудование
    }

    private void handleStatusCallback(String data, Long chatId) {
        // Обработка callback для проверки статуса
        String requestId = data.replace("status_", "");
        sendMessage(chatId, "Проверка статуса заявки: " + requestId);
        // Здесь можно реализовать запрос статуса через NATS
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






