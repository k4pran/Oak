package io.ryanjames.oak;

public class AudioRenderingException extends RuntimeException {

    public AudioRenderingException() {
        super();
    }

    public AudioRenderingException(String message) {
        super(message);
    }

    public AudioRenderingException(String message, Throwable cause) {
        super(message, cause);
    }
}
