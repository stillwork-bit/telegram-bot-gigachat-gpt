package org.tan.bot;

import lombok.extern.slf4j.Slf4j;
import org.tan.testIt.DTO.TestCreateRequestBuilder;
import org.tan.testIt.service.CreateWorkItemsTestItService;
import org.tan.testIt.service.ReaderCsvService;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.menubutton.SetChatMenuButton;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeChat;
import org.telegram.telegrambots.meta.api.objects.menubutton.MenuButtonCommands;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

@Slf4j
public class GigaChatBot extends TelegramLongPollingBot {
    private final GigaChatClient gigaClient;
    private DialogMode currentMode = DialogMode.MAIN;
    private static final String BOT_NAME = "YourBotName";
    private String pendingChatId;

    public GigaChatBot(String botToken, String gigachatAuthKey) {
        super(botToken);
        this.gigaClient = new GigaChatClient(gigachatAuthKey);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) return;

        String messageText = update.getMessage().getText();
        String chatId = update.getMessage().getChatId().toString();

        try {
            if (currentMode == DialogMode.TESTIT) {
                handleTestItInput(chatId, messageText);
                return;
            } else if (currentMode == DialogMode.GIGACHAT) {
                handleGetAnswearFromGigaChat(chatId, messageText);
                currentMode = DialogMode.MAIN;
                return;
            }

            switch (messageText) {
                case "/start" -> handleStartCommand(chatId);
                case "/gpt" -> handleGptCommand(chatId);
                case "/testit" -> handleTestItCommand(chatId);
                default -> showInvalidCommandMessage(chatId, messageText);
            }
        } catch (Exception e) {
            sendErrorMessage(chatId);
            e.printStackTrace();
        }
    }

    private void showInvalidCommandMessage(String chatId, String messageText) {
        log.error("Пользователь ввел некорректную команду: {}", messageText);
        String errorMessage = """
                ❌ Введена некорректная команда, используйте:
                    
                `/start` — главное меню бота
                `/gpt` — генерация тест-кейсов
                `/testit` — отправка тест-кейсов в TMS TestIt
                """;
        sendTextMessageMarkdown(chatId, errorMessage);
    }

    private void handleStartCommand(String chatId) {
        currentMode = DialogMode.MAIN;
        sendTextMessageMarkdown(chatId, loadMessage("main.txt"));
        showMainMenu(chatId,
                "Главное меню бота", "/start",
                "Генерация тест-кейсов \uD83D\uDE0E", "/gpt",
                "Отправка в TMS TestIt \uD83E\uDD70", "/testit");
    }

    private void handleGptCommand(String chatId) {
        currentMode = DialogMode.GIGACHAT;
        sendPhotoMessage(chatId, "gigachat");
        sendTextMessageMarkdown(chatId, loadMessage("gigachat.txt"));
    }

    private void handleTestItCommand(String chatId) {
        currentMode = DialogMode.TESTIT;
        pendingChatId = chatId;
        sendPhotoMessage(chatId, "testit");
        sendTextMessageMarkdown(chatId, loadMessage("testit.txt"));
    }

    private void handleTestItInput(String chatId, String csvData) {
        currentMode = DialogMode.MAIN;
        sendTextMessageMarkdown(chatId, "Начинаю обработку данных...");

        CompletableFuture.runAsync(() -> processTestItRequests(chatId, csvData));
    }

    private void processTestItRequests(String chatId, String csvData) {
        try {
            List<TestCreateRequestBuilder> requests = new ReaderCsvService()
                    .convertCsvToTestCreateRequestBuilder(csvData);

            CreateWorkItemsTestItService service = new CreateWorkItemsTestItService();
            requests.forEach(request -> {
                try {
                    log.info("Send test-keys to TMS TestIt: REQUEST");
                    service.createTestWithBuilder(request);
                    log.info("Send test-keys to TMS TestIt: DONE");
                } catch (Exception e) {
                    log.error("Send test-keys to TMS TestIt: ERROR");
                    sendErrorMessage(chatId);
                    e.printStackTrace();
                }
            });
            sendTextMessageMarkdown(chatId, "Обработка завершена\\! Успешно создано: " + requests.size() + " тестов");
        } catch (Exception e) {
            sendErrorMessage(chatId);
            sendTextMessageMarkdown(chatId, "Ошибка при обработке CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleGetAnswearFromGigaChat(String chatId, String message) {
        log.info("Request from telegram bot to GigaChat");
        try {
            String response = gigaClient.getResponse(chatId, message);
            String formattedResponse = "<b>📋 Ответ для копирования:</b>\n<pre><code>"
                    + escapeHtml(response)
                    + "</code></pre>";
            log.info("Response from GigaChat to telegram bot");
            sendTextMessage(chatId, formattedResponse);
        } catch (Exception e) {
            sendErrorMessage(chatId);
            e.printStackTrace();
        }
    }

    // Добавляем метод для экранирования HTML-символов
    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    // Обновляем метод отправки сообщений
    private void sendTextMessage(String chatId, String text) {
        SendMessage message = new SendMessage(chatId, text);
        message.setParseMode("HTML");
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.info("Failed to send message: " + e.getMessage());
        }
    }

    private void sendTextMessageMarkdown(String chatId, String text) {
        SendMessage message = new SendMessage(chatId, text);
        message.setParseMode("Markdown"); // Меняем на Markdown
        message.disableWebPagePreview();
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.info("Failed to send message: " + e.getMessage());
        }
    }

    private void sendPhotoMessage(String chatId, String photoKey) {
        try (InputStream imageStream = loadImage(photoKey)) {
            SendPhoto photo = new SendPhoto(chatId, new InputFile(imageStream, photoKey));
            execute(photo);
        } catch (Exception e) {
            sendErrorMessage(chatId);
            e.printStackTrace();
        }
    }

    private void sendErrorMessage(String chatId) {
        sendTextMessage(chatId, "❌ Ошибка обработки запроса");
    }

    private void showMainMenu(String chatId, String... menuItems) {
        List<BotCommand> commands = IntStream.iterate(0, i -> i < menuItems.length, i -> i + 2)
                .mapToObj(i -> new BotCommand(menuItems[i + 1].substring(1), menuItems[i]))
                .toList();

        try {
            SetMyCommands setCommands = new SetMyCommands();
            setCommands.setCommands(commands);
            setCommands.setScope(BotCommandScopeChat.builder().chatId(chatId).build());
            execute(setCommands);

            SetChatMenuButton menuButton = new SetChatMenuButton();
            menuButton.setChatId(chatId);
            menuButton.setMenuButton(MenuButtonCommands.builder().build());
            execute(menuButton);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return BOT_NAME;
    }

    private static String loadMessage(String name) {
        try (InputStream is = ClassLoader.getSystemResourceAsStream("messages/" + name)) {
            return new String(is.readAllBytes());
        } catch (IOException | NullPointerException e) {
            throw new RuntimeException("Can't load message: " + name, e);
        }
    }

    private static InputStream loadImage(String name) {
        InputStream is = ClassLoader.getSystemResourceAsStream("images/" + name + ".jpg");
        if (is == null) throw new RuntimeException("Image not found: " + name);
        return is;
    }

    private enum DialogMode {
        MAIN, GIGACHAT, TESTIT
    }
}