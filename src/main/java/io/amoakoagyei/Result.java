package io.amoakoagyei;

import java.util.Optional;
import java.util.function.Function;

public sealed interface Result<T> permits Success, Failure {

    static <T> Result<? extends T> failure(Exception err) {
        return new Failure<>(err);
    }

    static <T> Result<? extends T> success(T data) {
        return new Success<>(data);
    }

    @SuppressWarnings("")
    static <T> Result<? extends T> of(Optional<? extends T> optional) {
        if (optional.isPresent()) {
            return success(optional.get());
        }
        return failure(new IllegalArgumentException("Optional is empty"));
    }

    boolean isFailure();

    T getOrNull();

    Exception exceptionOrNull();

    boolean isSuccess();

    <U> Result<U> map(Function<T, U> func);
}

record Success<T>(T value) implements Result<T> {
    @Override
    public boolean isFailure() {
        return false;
    }

    @Override
    public T getOrNull() {
        return value;
    }

    @Override
    public Exception exceptionOrNull() {
        return null;
    }

    @Override
    public boolean isSuccess() {
        return true;
    }

    @Override
    public <U> Result<U> map(Function<T, U> func) {
        try {
            return new Success<>(func.apply(value));
        } catch (Exception ex) {
            return new Failure<>(ex);
        }
    }

}

record Failure<T>(Exception exception) implements Result<T> {
    @Override
    public boolean isFailure() {
        return true;
    }

    @Override
    public T getOrNull() {
        return null;
    }

    @Override
    public Exception exceptionOrNull() {
        return exception;
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public <U> Result<U> map(Function<T, U> func) {
        return new Failure<>(this.exception);
    }

}
