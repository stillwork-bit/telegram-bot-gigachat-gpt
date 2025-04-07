package org.tan.testIt.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.tan.testIt.DTO.TestCreateRequest;
import org.tan.testIt.DTO.TestCreateRequestBuilder;
import org.tan.Constants;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class CreateWorkItemsTestItService {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CreateWorkItemsTestItService() {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public HttpResponse<String> createTest(TestCreateRequest request) throws Exception {
        String requestBody = objectMapper.writeValueAsString(request);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(Constants.BASE_URL + "/workItems"))
                .header("Content-Type", "application/json")
                .header("Authorization", "PrivateToken " + Constants.PRIVATE_TOKEN)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        return httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> createTestWithBuilder(TestCreateRequestBuilder builder) throws Exception {
        return createTest(builder.build());
    }
}