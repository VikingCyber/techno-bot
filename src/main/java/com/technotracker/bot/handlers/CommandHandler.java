package com.technotracker.bot.handlers;

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

        switch (command) {
            case "/start":
                handleStart(chatId);
                break;
            case "/help":
                handleHelp(chatId);
                break;
            case "/request":
                handleRequest(chatId);
                break;
            case "/status":
                handleStatus(chatId);
                break;
            case "/catalog":
                handleCatalog(chatId);
                break;
            default:
                sendMessage(chatId, "Неизвестная команда. Используйте /help для списка доступных команд.");
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
        
        // Первая строка кнопок
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton requestBtn = new InlineKeyboardButton();
        requestBtn.setText("📝 Запросить оборудование");
        requestBtn.setCallbackData("action_request");
        row1.add(requestBtn);
        
        // Вторая строка кнопок
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        InlineKeyboardButton statusBtn = new InlineKeyboardButton();
        statusBtn.setText("📊 Статус заявок");
        statusBtn.setCallbackData("action_status");
        row2.add(statusBtn);
        
        InlineKeyboardButton catalogBtn = new InlineKeyboardButton();
        catalogBtn.setText("📚 Каталог");
        catalogBtn.setCallbackData("action_catalog");
        row2.add(catalogBtn);
        
        // Третья строка кнопок
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
        
        // Первая строка кнопок
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton requestBtn = new InlineKeyboardButton();
        requestBtn.setText("📝 Запросить оборудование");
        requestBtn.setCallbackData("action_request");
        row1.add(requestBtn);
        
        // Вторая строка кнопок
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
        // Здесь должен быть запрос к системе через NATS для получения статусов
        // Пока отправляем заглушку
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
        // Здесь должен быть запрос к системе через NATS для получения каталога
        // Пока отправляем заглушку
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






