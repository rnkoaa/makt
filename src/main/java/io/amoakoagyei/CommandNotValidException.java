package io.amoakoagyei;

public class CommandNotValidException extends RuntimeException {
    public CommandNotValidException(String message) {
        super(message);
    }
}
