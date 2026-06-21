package com.br.pokefichas.commons.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Resposta de erro padronizada")
public class ErrorResponse {

    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private final LocalDateTime timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String code;
    private final String path;
    private final Map<String, String> fieldErrors;
    private final List<String> errors;

    private ErrorResponse(Builder builder) {
        this.timestamp = LocalDateTime.now();
        this.status = builder.status;
        this.error = builder.error;
        this.message = builder.message;
        this.code = builder.code;
        this.path = builder.path;
        this.fieldErrors = builder.fieldErrors;
        this.errors = builder.errors;
    }

    public static Builder builder() {
        return new Builder();
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public int getStatus() { return status; }
    public String getError() { return error; }
    public String getMessage() { return message; }
    public String getCode() { return code; }
    public String getPath() { return path; }
    public Map<String, String> getFieldErrors() { return fieldErrors; }
    public List<String> getErrors() { return errors; }

    public static class Builder {
        private int status;
        private String error;
        private String message;
        private String code;
        private String path;
        private Map<String, String> fieldErrors;
        private List<String> errors;

        public Builder status(int status) { this.status = status; return this; }
        public Builder error(String error) { this.error = error; return this; }
        public Builder message(String message) { this.message = message; return this; }
        public Builder code(String code) { this.code = code; return this; }
        public Builder path(String path) { this.path = path; return this; }
        public Builder fieldErrors(Map<String, String> fieldErrors) { this.fieldErrors = fieldErrors; return this; }
        public Builder errors(List<String> errors) { this.errors = errors; return this; }

        public ErrorResponse build() {
            return new ErrorResponse(this);
        }
    }
}
