# Techno Tracker Telegram Bot

Telegram-бот для системы управления техническими ресурсами "Techno Tracker". Бот обеспечивает пользователям возможность оформлять запросы на оборудование и получать уведомления о статусе заявок через Telegram.

## Возможности

- 📝 Оформление запросов на получение оборудования
- 📊 Просмотр статуса заявок
- 🔔 Получение уведомлений об изменении статуса оборудования
- 📚 Доступ к каталогу оборудования (требует интеграции с основной системой)
- ⚡ Интеграция с NATS JetStream для обмена сообщениями с основной системой

## Требования

- Java 21 (LTS)
- Maven 3.6+
- Доступ к NATS серверу с JetStream
- Telegram Bot Token (получить у [@BotFather](https://t.me/botfather))

## Установка и настройка

### 1. Клонирование и сборка проекта

```bash
mvn clean install
```

### 2. Настройка переменных окружения

Создайте файл `.env` или установите переменные окружения:

```bash
export TELEGRAM_BOT_TOKEN=your_bot_token_here
export TELEGRAM_BOT_USERNAME=your_bot_username
export NATS_URL=nats://localhost:4222
export NATS_SUBJECT=technotracker.events
export NATS_REQUEST_SUBJECT=technotracker.requests
```

Или отредактируйте `src/main/resources/application.properties`:

```properties
bot.token=your_bot_token_here
bot.username=your_bot_username
nats.url=nats://localhost:4222
nats.subject=technotracker.events
nats.requestSubject=technotracker.requests
```

### 3. Запуск бота

#### Вариант 1: Запуск через Maven

```bash
mvn spring-boot:run
```

#### Вариант 2: Сборка JAR и запуск

```bash
mvn package
java -jar target/telegram-bot-1.0.0.jar
```

#### Вариант 3: Запуск через Docker Compose (рекомендуется)

1. Создайте файл `.env` с переменными окружения:

```bash
TELEGRAM_BOT_TOKEN=your_bot_token_here
TELEGRAM_BOT_USERNAME=your_bot_username
NATS_URL=nats://nats:4222
NATS_SUBJECT=technotracker.events
NATS_REQUEST_SUBJECT=technotracker.requests
```

2. Запустите через Docker Compose:

```bash
docker-compose up -d
```

Это запустит бота вместе с NATS сервером в контейнерах.

#### Вариант 4: Только Docker образ бота

```bash
docker build -t technotracker-bot .
docker run -e TELEGRAM_BOT_TOKEN=your_token \
           -e TELEGRAM_BOT_USERNAME=your_username \
           -e NATS_URL=nats://host.docker.internal:4222 \
           technotracker-bot
```

## Архитектура

### Структура проекта

```
src/main/java/com/technotracker/bot/
├── Application.java              # Точка входа приложения
├── TechnoTrackerBot.java        # Основной класс бота
├── config/
│   ├── BotConfig.java           # Конфигурация бота
│   └── NatsConfig.java          # Конфигурация NATS
├── handlers/
│   ├── CommandHandler.java      # Обработчик команд (/start, /help, etc.)
│   ├── MessageHandler.java      # Обработчик текстовых сообщений
│   └── CallbackHandler.java     # Обработчик callback запросов (кнопки)
├── nats/
│   └── NatsClient.java          # Клиент для работы с NATS JetStream
└── model/
    ├── EquipmentRequest.java    # Модель запроса на оборудование
    └── EquipmentStatus.java     # Модель статуса оборудования
```

### Интеграция с NATS JetStream

Бот использует NATS JetStream для обмена сообщениями с основной системой:

1. **Публикация запросов**: При получении запроса на оборудование от пользователя, бот публикует сообщение в топик `technotracker.requests` (по умолчанию).

2. **Подписка на обновления**: Бот подписывается на топик `technotracker.events.status` (по умолчанию) для получения уведомлений об изменении статуса заявок.

### Формат сообщений

#### EquipmentRequest (запрос на оборудование)

```json
{
  "user_id": 123456789,
  "user_name": "username",
  "equipment_id": null,
  "equipment_name": "Проектор",
  "location": "Аудитория 1",
  "comment": "Для презентации в 15:00",
  "request_date": "2024-01-15T14:30:00",
  "request_id": "uuid-string"
}
```

#### EquipmentStatus (статус оборудования)

```json
{
  "request_id": "uuid-string",
  "equipment_id": 1,
  "equipment_name": "Проектор",
  "status": "APPROVED",
  "user_id": 123456789,
  "message": "Ваш запрос одобрен",
  "updated_at": "2024-01-15T14:35:00"
}
```

Возможные статусы:
- `PENDING` - Ожидает рассмотрения
- `APPROVED` - Одобрено
- `REJECTED` - Отклонено
- `IN_DELIVERY` - В доставке
- `DELIVERED` - Доставлено

## Команды бота

- `/start` - Начать работу с ботом
- `/help` - Показать справку по командам
- `/request` - Инструкция по оформлению запроса на оборудование
- `/status` - Проверить статус ваших заявок
- `/catalog` - Просмотреть каталог оборудования

## Оформление запроса

Для оформления запроса на оборудование отправьте сообщение в следующем формате:

```
Запрос: [название оборудования]
Локация: [ваша локация]
Комментарий: [дополнительная информация]
```

Пример:
```
Запрос: Проектор
Локация: Аудитория 1
Комментарий: Для презентации в 15:00
```

## Разработка

### Зависимости

Основные зависимости проекта:
- `telegrambots` - Библиотека для работы с Telegram Bot API
- `jnats` - NATS Java клиент
- `jackson-databind` - JSON сериализация/десериализация
- `spring-boot-starter` - Spring Boot для управления зависимостями
- `lombok` - Генерация boilerplate кода
- `logback` - Логирование

### Расширение функциональности

Для добавления новых команд:

1. Добавьте обработку команды в `CommandHandler.java`
2. При необходимости добавьте новую модель данных в `model/`
3. Если требуется интеграция с системой через NATS, используйте `NatsClient`

## Логирование

Логи сохраняются в:
- Консоль (все уровни)
- Файл `logs/telegram-bot.log` (INFO и выше)
- Ротация логов: ежедневно, хранение 30 дней

## Troubleshooting

### Бот не отвечает

1. Проверьте, что `TELEGRAM_BOT_TOKEN` установлен корректно
2. Убедитесь, что бот запущен и подключен к интернету
3. Проверьте логи на наличие ошибок

### Не работает интеграция с NATS

1. Убедитесь, что NATS сервер запущен и доступен
2. Проверьте настройки `NATS_URL` в конфигурации
3. Убедитесь, что используется NATS с поддержкой JetStream
4. Проверьте логи подключения к NATS

### Ошибки при парсинге запросов

Бот использует регулярные выражения для парсинга. Убедитесь, что сообщения пользователя следуют указанному формату.

## Контакты и поддержка

Для вопросов и предложений обращайтесь к команде разработки Techno Tracker.

## Лицензия

Внутренний проект Красноярской Летней Школы.

