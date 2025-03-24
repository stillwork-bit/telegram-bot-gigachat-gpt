package org.bot;

import com.google.gson.*;
import okhttp3.*;
import org.bot.DTO.ChatMessage;
import org.bot.DTO.ChatRequest;
import org.bot.DTO.ChatResponse;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import javax.net.ssl.*;

public class GigaChatClient {
    private static final String AUTH_URL = "https://ngw.devices.sberbank.ru:9443/api/v2/oauth";
    private static final String API_URL = "https://gigachat.devices.sberbank.ru/api/v1/chat/completions";
    private static final String SCOPE = "GIGACHAT_API_PERS";
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
        if (accessToken == null || Instant.now().isAfter(tokenExpiration)) {
            tokenLock.lock();
            try {
                if (accessToken == null || Instant.now().isAfter(tokenExpiration)) {
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
                throw new IOException("Auth failed: " + response.code());
            }

            JsonObject json = gson.fromJson(response.body().string(), JsonObject.class);
            this.accessToken = json.get("access_token").getAsString();
//            long expiresIn = json.get("expires_at").getAsLong() - json.get("created_at").getAsLong();
//            this.tokenExpiration = Instant.now().plusSeconds(expiresIn - 300);
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
                .addHeader("Content-Type", "application/json")
                .addHeader("Content-Type", "application/json")
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