package com.worklog.common;

import java.time.Instant;

public class ApiResponse<T> {
    private String status;
    private T data;
    private String message;
    private String code;
    private Instant timestamp;

    public ApiResponse(String status, T data) {
        this.status = status;
        this.data = data;
        this.timestamp = Instant.now();
    }

    public ApiResponse(String status, T data, String message) {
        this(status, data);
        this.message = message;
    }

    public ApiResponse(String status, T data, String message, String code) {
        this(status, data, message);
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
} 