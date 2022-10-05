package com.example.bot.config;

import com.example.bot.service.MainBotService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
public class BotConfig {

    private final MainBotService bot;

    public BotConfig(MainBotService bot) {
        this.bot = bot;
    }

    @PostConstruct
    protected void init() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);
            System.out.println("Bot ishladi");
        } catch (TelegramApiException e) {
            /// it's your exception
        }
    }

    @PreDestroy
    protected void closeSession() {
    }


}
