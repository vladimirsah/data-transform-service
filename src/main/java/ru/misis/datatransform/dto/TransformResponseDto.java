package ru.misis.datatransform.dto;

import java.util.List;

public class TransformResponseDto {

    private String status;
    private String data;
    private List<ErrorDto> errors;
    private String traceId;
    private Long processingTimeMs;
    private String validationStrategy;

    public static TransformResponseDto ok(String data, String traceId, Long processingTimeMs, String validationStrategy) {
        TransformResponseDto response = new TransformResponseDto();
        response.setStatus("OK");
        response.setData(data);
        response.setTraceId(traceId);
        response.setProcessingTimeMs(processingTimeMs);
        response.setValidationStrategy(validationStrategy);
        return response;
    }

    public static TransformResponseDto fail(List<ErrorDto> errors, String traceId) {
        TransformResponseDto response = new TransformResponseDto();
        response.setStatus("ERROR");
        response.setErrors(errors);
        response.setTraceId(traceId);
        return response;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public List<ErrorDto> getErrors() {
        return errors;
    }

    public void setErrors(List<ErrorDto> errors) {
        this.errors = errors;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public Long getProcessingTimeMs() {
        return processingTimeMs;
    }

    public void setProcessingTimeMs(Long processingTimeMs) {
        this.processingTimeMs = processingTimeMs;
    }

    public String getValidationStrategy() {
        return validationStrategy;
    }

    public void setValidationStrategy(String validationStrategy) {
        this.validationStrategy = validationStrategy;
    }
}
