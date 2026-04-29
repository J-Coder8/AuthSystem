package com.authsystem.service;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.List;

import com.authsystem.model.ApiError;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiService {

    private static final String BASE_URL = "http://localhost:8082/api";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final int TIMEOUT_SECONDS = 30;

    private final OkHttpClient httpClient;
    private final Gson gson;

    public ApiService() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(java.time.Duration.ofSeconds(TIMEOUT_SECONDS))
                .readTimeout(java.time.Duration.ofSeconds(TIMEOUT_SECONDS))
                .writeTimeout(java.time.Duration.ofSeconds(TIMEOUT_SECONDS))
                .build();
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
                    @Override
                    public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        return LocalDateTime.parse(json.getAsJsonPrimitive().getAsString());
                    }
                })
                .registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
                    @Override
                    public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
                        return new JsonPrimitive(src.toString());
                    }
                })
                .create();
    }

    public <T> ApiResponse<T> get(String path, Class<T> responseType) {
        return get(path, responseType, null);
    }

    public <T> ApiResponse<T> get(String path, Class<T> responseType, String token) {
        try {
            Request.Builder builder = new Request.Builder()
                    .url(BASE_URL + path)
                    .header("Accept", "application/json")
                    .get();
            if (token != null && !token.isBlank()) {
                builder.header("Authorization", "Bearer " + token);
            }
            try (Response response = httpClient.newCall(builder.build()).execute()) {
                return parseResponse(response, responseType);
            }
        } catch (IOException e) {
            return ApiResponse.error("Network error: " + e.getMessage(), 0);
        }
    }

    public <T> ApiResponse<List<T>> getList(String path, TypeToken<List<T>> typeToken, String token) {
        try {
            Request.Builder builder = new Request.Builder()
                    .url(BASE_URL + path)
                    .header("Accept", "application/json")
                    .get();
            if (token != null && !token.isBlank()) {
                builder.header("Authorization", "Bearer " + token);
            }
            try (Response response = httpClient.newCall(builder.build()).execute()) {
                return parseListResponse(response, typeToken);
            }
        } catch (IOException e) {
            return ApiResponse.error("Network error: " + e.getMessage(), 0);
        }
    }

    public <T> ApiResponse<T> post(String path, Object body, Class<T> responseType) {
        return post(path, body, responseType, null);
    }

    public <T> ApiResponse<T> post(String path, Object body, Class<T> responseType, String token) {
        try {
            String jsonBody = body != null ? gson.toJson(body) : "{}";
            RequestBody requestBody = RequestBody.create(jsonBody, JSON);
            Request.Builder builder = new Request.Builder()
                    .url(BASE_URL + path)
                    .header("Accept", "application/json")
                    .post(requestBody);
            if (token != null && !token.isBlank()) {
                builder.header("Authorization", "Bearer " + token);
            }
            try (Response response = httpClient.newCall(builder.build()).execute()) {
                return parseResponse(response, responseType);
            }
        } catch (IOException e) {
            return ApiResponse.error("Network error: " + e.getMessage(), 0);
        }
    }

    public <T> ApiResponse<T> put(String path, Object body, Class<T> responseType, String token) {
        try {
            String jsonBody = body != null ? gson.toJson(body) : "{}";
            RequestBody requestBody = RequestBody.create(jsonBody, JSON);
            Request.Builder builder = new Request.Builder()
                    .url(BASE_URL + path)
                    .header("Accept", "application/json")
                    .put(requestBody);
            if (token != null && !token.isBlank()) {
                builder.header("Authorization", "Bearer " + token);
            }
            try (Response response = httpClient.newCall(builder.build()).execute()) {
                return parseResponse(response, responseType);
            }
        } catch (IOException e) {
            return ApiResponse.error("Network error: " + e.getMessage(), 0);
        }
    }

    public <T> ApiResponse<T> delete(String path, Class<T> responseType, String token) {
        try {
            Request.Builder builder = new Request.Builder()
                    .url(BASE_URL + path)
                    .header("Accept", "application/json")
                    .delete();
            if (token != null && !token.isBlank()) {
                builder.header("Authorization", "Bearer " + token);
            }
            try (Response response = httpClient.newCall(builder.build()).execute()) {
                return parseResponse(response, responseType);
            }
        } catch (IOException e) {
            return ApiResponse.error("Network error: " + e.getMessage(), 0);
        }
    }

    private <T> ApiResponse<T> parseResponse(Response response, Class<T> responseType) {
        int statusCode = response.code();
        String body;
        try {
            body = response.body() != null ? response.body().string() : "";
        } catch (IOException e) {
            return ApiResponse.error("Failed to read response body: " + e.getMessage(), statusCode);
        }
        if (statusCode >= 200 && statusCode < 300) {
            if (responseType == Void.class || body.isBlank()) {
                return ApiResponse.success(null, statusCode);
            }
            try {
                T data = gson.fromJson(body, responseType);
                return ApiResponse.success(data, statusCode);
            } catch (Exception e) {
                return ApiResponse.error("Failed to parse response: " + e.getMessage(), statusCode);
            }
        } else {
            try {
                ApiError error = gson.fromJson(body, ApiError.class);
                return ApiResponse.error(error != null && error.getMessage() != null ? error.getMessage() : "HTTP " + statusCode, statusCode);
            } catch (Exception e) {
                return ApiResponse.error(body.isBlank() ? "HTTP " + statusCode : body, statusCode);
            }
        }
    }

    private <T> ApiResponse<List<T>> parseListResponse(Response response, TypeToken<List<T>> typeToken) {
        int statusCode = response.code();
        String body;
        try {
            body = response.body() != null ? response.body().string() : "";
        } catch (IOException e) {
            return ApiResponse.error("Failed to read response body: " + e.getMessage(), statusCode);
        }
        if (statusCode >= 200 && statusCode < 300) {
            if (body.isBlank()) {
                return ApiResponse.success(List.of(), statusCode);
            }
            try {
                Type type = typeToken.getType();
                List<T> data = gson.fromJson(body, type);
                return ApiResponse.success(data, statusCode);
            } catch (Exception e) {
                return ApiResponse.error("Failed to parse response: " + e.getMessage(), statusCode);
            }
        } else {
            try {
                ApiError error = gson.fromJson(body, ApiError.class);
                return ApiResponse.error(error != null && error.getMessage() != null ? error.getMessage() : "HTTP " + statusCode, statusCode);
            } catch (Exception e) {
                return ApiResponse.error(body.isBlank() ? "HTTP " + statusCode : body, statusCode);
            }
        }
    }

    public Gson getGson() {
        return gson;
    }

    public static class ApiResponse<T> {
        private final T data;
        private final String errorMessage;
        private final int statusCode;
        private final boolean success;

        private ApiResponse(T data, String errorMessage, int statusCode, boolean success) {
            this.data = data;
            this.errorMessage = errorMessage;
            this.statusCode = statusCode;
            this.success = success;
        }

        public static <T> ApiResponse<T> success(T data, int statusCode) {
            return new ApiResponse<>(data, null, statusCode, true);
        }

        public static <T> ApiResponse<T> error(String message, int statusCode) {
            return new ApiResponse<>(null, message, statusCode, false);
        }

        public boolean isSuccess() { return success; }
        public T getData() { return data; }
        public String getErrorMessage() { return errorMessage; }
        public int getStatusCode() { return statusCode; }
    }
}
