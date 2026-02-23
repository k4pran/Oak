package io.ryanjames.oak.config;

public class ConfigLoadingException extends RuntimeException {

    public ConfigLoadingException() {
        super();
    }

    public ConfigLoadingException(String message) {
        super(message);
    }

    public ConfigLoadingException(String message, Throwable cause) {
        super(message, cause);
    }
}
