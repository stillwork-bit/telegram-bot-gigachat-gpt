package org.bot;


import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

    public class GigaChatBot extends TelegramLongPollingBot {
        private final GigaChatClient gigaClient;

        public GigaChatBot(String botToken, String gigachatAuthKey) {
            super(botToken);
            this.gigaClient = new GigaChatClient(gigachatAuthKey);
        }

        @Override
        public void onUpdateReceived(Update update) {
            if (!update.hasMessage() || !update.getMessage().hasText()) return;

            String chatId = update.getMessage().getChatId().toString();
            String text = update.getMessage().getText();

            try {
                String response = gigaClient.getResponse(chatId, text);
                sendMessage(chatId, response);
            } catch (Exception e) {
                sendMessage(chatId, "❌ Ошибка обработки запроса");
                e.printStackTrace();
            }
        }

        private void sendMessage(String chatId, String text) {
            SendMessage message = new SendMessage(chatId, text);
            message.setParseMode("Markdown");

            try {
                execute(message);
            } catch (TelegramApiException e) {
                System.err.println("Failed to send message: " + e.getMessage());
            }
        }

        @Override
        public String getBotUsername() {
            return "YourBotName";
        }
    }