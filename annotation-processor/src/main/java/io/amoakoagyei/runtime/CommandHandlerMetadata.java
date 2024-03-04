package io.amoakoagyei.runtime;

import com.google.common.collect.Sets;
import io.amoakoagyei.Strings;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

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
        if (splitParts.length < 8) {
            return null;
        }
        var commandClassName = splitParts[0];
        var aggregateClassName = splitParts[1];
        var handlerMethodName = splitParts[2];
        var aggregateIdElementKind = splitParts[3];
        var aggregateIdElementType = splitParts[4];
        var aggregateIdAccessorName = splitParts[5];
        var aggregateIdAccessorKind = splitParts[6];
        var aggregateIdAccessorModifiers = splitParts[7] == null ?
                new HashSet<String>() :
                Sets.newHashSet(splitParts[7]);

        var rawAggregateIdMetadata = new RawAggregateIdMetadata(
                commandClassName,
                aggregateIdElementKind,
                aggregateIdElementType,
                aggregateIdAccessorName,
                aggregateIdAccessorKind,
                aggregateIdAccessorModifiers
        );

        return new RawCommandHandlerMetadata(
                commandClassName, aggregateClassName, handlerMethodName, rawAggregateIdMetadata
        );
    }

    public boolean isValid() {
        return commandType != null && aggregateType != null
                && Strings.isNotNullOrEmpty(methodName);
    }

    record RawCommandHandlerMetadata(
            String commandClassName, String aggregateClassName, String handlerMethodName,
            RawAggregateIdMetadata rawAggregateIdMetadata
    ) {
        boolean isConstructor() {
            return Objects.equals(handlerMethodName, "<init>");
        }
    }

    record RawAggregateIdMetadata(
            String enclosingClassName,
            String aggregateIdElementKind,
            String aggregateIdElementType,
            String aggregateIdAccessorName,
            String aggregateIdAccessorKind,
            Set<String> modifiers
    ) {


    }
}
