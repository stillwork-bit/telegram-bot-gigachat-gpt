package org.tan.bot;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.tan.bot.DTO.ChatMessage;
import org.tan.bot.DTO.ChatRequest;
import org.tan.bot.DTO.ChatResponse;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import static org.tan.Constants.*;

public class GigaChatClient {
    private String AUTH_URL = AUTH_URL_SBER;
    private String API_URL = API_URL_SBER;
    private String SCOPE = SCOPE_MODEL;
    private static final int MAX_HISTORY = 10;

    private final OkHttpClient client;
    private final Gson gson = new GsonBuilder().create();
    private final String authKey;

    private String accessToken;
    private Instant tokenExpiration;
    private final ReentrantLock tokenLock = new ReentrantLock();
    private final Map<String, ChatSession> sessions = new ConcurrentHashMap<>();

    private static class ChatSession {
        String sessionId = UUID.randomUUID().toString();
        Deque<ChatMessage> history = new LinkedList<>();
    }

    public GigaChatClient(String authKey) {
        this.authKey = authKey;
        this.client = createUnsafeClient();
    }

    private OkHttpClient createUnsafeClient() {
        try {
            TrustManager[] trustAll = new TrustManager[]{
                    new X509TrustManager() {
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] chain, String authType) {
                        }

                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAll, new java.security.SecureRandom());

            return new OkHttpClient.Builder()
                    .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) trustAll[0])
                    .hostnameVerifier((host, session) -> true)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void ensureTokenValid() throws IOException {
        // Добавлена проверка на null для tokenExpiration
        if (accessToken == null || (tokenExpiration != null && Instant.now().isAfter(tokenExpiration))) {
            tokenLock.lock();
            try {
                if (accessToken == null || (tokenExpiration != null && Instant.now().isAfter(tokenExpiration))) {
                    refreshAccessToken();
                }
            } finally {
                tokenLock.unlock();
            }
        }
    }

    private void refreshAccessToken() throws IOException {
        String rqUID = UUID.randomUUID().toString();
        String encodedKey = authKey;

        Request request = new Request.Builder()
                .url(AUTH_URL)
                .post(new FormBody.Builder().add("scope", SCOPE).build())
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Accept", "application/json")
                .addHeader("RqUID", rqUID)
                .addHeader("Authorization", "Bearer " + encodedKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Auth failed: " + response.code() + ", body: " + response.body().string());
            }

            JsonObject json = gson.fromJson(response.body().string(), JsonObject.class);

            // Безопасное извлечение access_token
            if (!json.has("access_token")) {
                throw new IOException("Missing access_token in auth response");
            }
            this.accessToken = json.get("access_token").getAsString();

            // Безопасное извлечение expires_in с fallback
            long expiresIn = 3600; // значение по умолчанию
            if (json.has("expires_in")) {
                expiresIn = json.get("expires_in").getAsLong();
            } else if (json.has("expires_at")) { // альтернативное поле
                long expiresAt = json.get("expires_at").getAsLong();
                expiresIn = expiresAt - Instant.now().getEpochSecond();
            }

            this.tokenExpiration = Instant.now().plusSeconds(expiresIn - 300);
        }
    }

    public String getResponse(String chatId, String message) throws IOException {
        ensureTokenValid();
        ChatSession session = sessions.computeIfAbsent(chatId, k -> new ChatSession());

        synchronized (session.history) {
            session.history.addLast(new ChatMessage("user", message));
            trimHistory(session.history);
        }

        ChatRequest request = new ChatRequest(
                "GigaChat",
                new ArrayList<>(session.history),
                0.7,
                512
        );

        RequestBody body = RequestBody.create(
                gson.toJson(request),
                MediaType.get("application/json")
        );

        String rqUID = UUID.randomUUID().toString();

        Request apiRequest = new Request.Builder()
                .url(API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json") // Убрал дублирование
                .addHeader("RqUID", rqUID)
                .addHeader("X-Session-ID", session.sessionId)
                .build();

        try (Response response = client.newCall(apiRequest).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("API error: " + response.code());
            }

            ChatResponse chatResponse = gson.fromJson(
                    response.body().string(),
                    ChatResponse.class
            );

            String answer = chatResponse.getChoices().get(0).getMessage().getContent();

            synchronized (session.history) {
                session.history.addLast(new ChatMessage("assistant", answer));
                trimHistory(session.history);
            }

            return answer;
        }
    }

    private void trimHistory(Deque<ChatMessage> history) {
        while (history.size() > MAX_HISTORY) {
            history.removeFirst();
        }
    }

    public void clearHistory(String chatId) {
        sessions.remove(chatId);
    }
}