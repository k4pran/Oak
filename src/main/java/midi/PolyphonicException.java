package midi;

public class PolyphonicException extends RuntimeException{
    public PolyphonicException() {
        super();
    }

    public PolyphonicException(String message) {
        super(message);
    }
}