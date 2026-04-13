package ru.misis.datatransform.dto;

import jakarta.validation.constraints.NotBlank;

public class TransformRequestDto {

    @NotBlank
    private String payload;

    private String xsdPath;
    private String validationStrategy;

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getXsdPath() {
        return xsdPath;
    }

    public void setXsdPath(String xsdPath) {
        this.xsdPath = xsdPath;
    }

    public String getValidationStrategy() {
        return validationStrategy;
    }

    public void setValidationStrategy(String validationStrategy) {
        this.validationStrategy = validationStrategy;
    }
}
