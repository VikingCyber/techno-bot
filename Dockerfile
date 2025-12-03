FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Копируем pom.xml и скачиваем зависимости
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Копируем исходный код и собираем проект
COPY src ./src
RUN mvn clean package -DskipTests

# Финальный образ
FROM eclipse-temurin:21-jre

WORKDIR /app

# Копируем собранный JAR
COPY --from=build /app/target/telegram-bot-*.jar app.jar

# Создаем директорию для логов
RUN mkdir -p logs

# Запуск приложения
ENTRYPOINT ["java", "-jar", "app.jar"]







