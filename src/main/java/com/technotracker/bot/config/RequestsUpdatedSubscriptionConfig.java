package com.technotracker.bot.config;

import com.technotracker.bot.nats.NatsClient;
import com.technotracker.bot.service.RequestsUpdatedHandler;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;

/**
 * Подписывает обработчик на топик requests.updated после полного старта приложения.
 */
@Configuration
public class RequestsUpdatedSubscriptionConfig implements ApplicationRunner {

    private final NatsClient natsClient;
    private final RequestsUpdatedHandler requestsUpdatedHandler;

    public RequestsUpdatedSubscriptionConfig(NatsClient natsClient, RequestsUpdatedHandler requestsUpdatedHandler) {
        this.natsClient = natsClient;
        this.requestsUpdatedHandler = requestsUpdatedHandler;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (natsClient.isConnected()) {
            natsClient.setRequestsUpdatedHandler(requestsUpdatedHandler::onRequestUpdated);
        }
    }
}
