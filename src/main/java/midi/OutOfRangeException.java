package midi;

public class OutOfRangeException extends RuntimeException {
    public OutOfRangeException() {
    }

    public OutOfRangeException(String message) {
        super(message);
    }
}
