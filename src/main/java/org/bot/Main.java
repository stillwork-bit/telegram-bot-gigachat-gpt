package org.bot;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    public static void main(String[] args) {

        String botToken = "7787625957:AAFQM3Akmm18jU2DHBS_qocjaH9jCAJ2P54";
        String gigachatAuthKey = "MGFkNTdkZDktYTc3ZS00ZDU5LWFkZTYtZDFhM2ZkNWY0N2FjOmViNDU1MmEyLWJmM2YtNDc2ZS04Y2EwLWFjNDNmZGM0NzRkYw==";

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new GigaChatBot(botToken, gigachatAuthKey));
            System.out.println("Bot started!");
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}