package com.technotracker.bot.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "nats")
public class NatsConfig {
    private String url = "nats://localhost:4222";
    private String subject = "technotracker.events";
    private String requestSubject = "technotracker.requests";

    @Setter
    @Getter
    private String requestCreatedSubject = "bot.requests.created";
    @Setter
    @Getter
    private String requestGetSubject = "bot.requests.get";
    @Setter
    @Getter
    private String requestListSubject = "bot.requests.list";
    @Setter
    @Getter
    private String requestDeleteSubject = "bot.requests.cancel";
    /**
     * Enable or disable connecting to NATS. When false the application will start
     * without attempting to connect to a NATS server (useful for local UI/debug runs).
     */
    private boolean enabled = false;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getRequestSubject() {
        return requestSubject;
    }

    public void setRequestSubject(String requestSubject) {
        this.requestSubject = requestSubject;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}

