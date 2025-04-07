package org.tan;

import lombok.extern.slf4j.Slf4j;
import org.tan.bot.GigaChatBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import static org.tan.Constants.BOT_TOKEN;
import static org.tan.Constants.GIGA_CHAT_AUTH_KEY;

@Slf4j
public class Main {
    static GigaChatBot gigaChatBot = new GigaChatBot(BOT_TOKEN, GIGA_CHAT_AUTH_KEY);

    public static void main(String[] args) {

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(gigaChatBot);
            log.info("Bot started!");
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}