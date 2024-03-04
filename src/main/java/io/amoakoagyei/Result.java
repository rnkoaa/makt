package io.amoakoagyei;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

// https://github.com/mverleg/java-result/blob/main/src/main/java/nl/markv/result/Result.java
public sealed interface Result<T> permits Success, Failure {

    static <T> Result< T> failure(Exception err) {
        return new Failure<>(err);
    }

    static <T> Result<T> success(T data) {
        return new Success<>(data);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    static <T> Result< T> of(Optional< T> optional) {
        return optional
                .map(Result::success)
                .orElse(failure(new IllegalArgumentException("optional is empty")));
    }

    boolean isFailure();

    T getOrNull();

    Exception exceptionOrNull();

    boolean isSuccess();

    <U> Result<U> map(Function<T, U> func);

    <U> Result<U> flatMap(Function<T, Result<U>> func);

    Result<? extends T> filter(Predicate<T> predicate);
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

    @Override
    public <U> Result<U> flatMap(Function<T, Result<U>> func) {
        try {
            return func.apply(value);
        } catch (Exception ex) {
            return new Failure<>(ex);
        }
    }

    @Override
    public Result<? extends T> filter(Predicate<T> predicate) {
        try {
            if (predicate.test(this.value)) {
                return new Success<>(this.value);
            } else {
                return Result.failure(new IllegalArgumentException("failed to process predicate"));
            }
        } catch (Exception ex) {
            return Result.failure(ex);
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

    @Override
    public <U> Result<U> flatMap(Function<T, Result<U>> func) {
        return new Failure<>(this.exception);
    }

    @Override
    public Result<? extends T> filter(Predicate<T> predicate) {
        return new Failure<>(this.exception);
    }

}
