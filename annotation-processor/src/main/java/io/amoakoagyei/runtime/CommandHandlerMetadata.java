package io.amoakoagyei.runtime;

import io.amoakoagyei.Strings;

import java.util.Objects;

public record CommandHandlerMetadata(
        Class<?> commandType,
        Class<?> aggregateType,
        String methodName,
        AggregateIdMetadata aggregateIdMetadata
) {
    public boolean isConstructor() {
        return Objects.equals(methodName, "<init>");
    }
    static RawCommandHandlerMetadata fromLine(String line) {
        var splitParts = line.split(",");
        if (splitParts.length < 3) {
            return null;
        }
        return new RawCommandHandlerMetadata(
//                splitParts[0], splitParts[1], splitParts[2], null, null, null, null, new HashSet<>()
                splitParts[0], splitParts[1], splitParts[2]
        );
    }

    public boolean isValid() {
        return commandType != null && aggregateType != null
                && Strings.isNotNullOrEmpty(methodName);
    }

    record RawCommandHandlerMetadata(
            String commandClassName, String aggregateClassName, String handlerMethodName
    ){}
}
