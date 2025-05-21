package com.test.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponse {
    private String code;
    private String error;
    private long timestamp;
    private String message;
    private String path;
}
