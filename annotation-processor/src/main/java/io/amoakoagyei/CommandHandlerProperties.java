package io.amoakoagyei;

import java.util.Objects;

public record CommandHandlerProperties(
        String commandType,
        String aggregateType,
        String methodName
) {
    boolean isConstructor() {
        return Objects.equals(methodName, "<init>");
    }

    static CommandHandlerProperties fromLine(String line) {
        var splitParts = line.split(",");
        if (splitParts.length < 3) {
            return null;
        }
        return new CommandHandlerProperties(
                splitParts[0], splitParts[1], splitParts[2]
        );
    }

    @Override
    public String toString() {
        return "%s,%s,%s,%s".formatted(commandType, aggregateType, methodName, isConstructor());
    }
}
