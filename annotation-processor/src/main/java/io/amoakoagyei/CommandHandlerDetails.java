package io.amoakoagyei;

import java.util.Objects;

public record CommandHandlerDetails(
        Class<?> commandType,
        Class<?> aggregateType,
        String methodName
) {
    boolean isConstructor() {
        return Objects.equals(methodName, "<init>");
    }
}
