package io.amoakoagyei.runtime;

import com.google.common.collect.Sets;
import io.amoakoagyei.Strings;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public record CommandHandlerMetadata(
        Class<?> aggregateAttributeClass, // class of Event or Command
        Class<?> aggregateType,
        String methodName,
        Class<?> methodReturnType,
        AggregateIdMetadata aggregateIdMetadata
) {
    public boolean isConstructor() {
        return Objects.equals(methodName, "<init>");
    }

    static RawCommandHandlerMetadata fromLine(String line) {
        var splitParts = line.split(",");
        if (splitParts.length < 9) {
            return null;
        }
        var aggregateAttributeClass = splitParts[0];
        var aggregateClassName = splitParts[1];
        var handlerMethodName = splitParts[2];
        var methodReturnType = splitParts[3];
        var aggregateIdElementKind = splitParts[4];
        var aggregateIdElementType = splitParts[5];
        var aggregateIdAccessorName = splitParts[6];
        var aggregateIdAccessorKind = splitParts[7];
        var aggregateIdAccessorModifiers = splitParts[8] == null ?
                new HashSet<String>() :
                Sets.newHashSet(splitParts[8]);

        var rawAggregateIdMetadata = new RawAggregateIdMetadata(
                aggregateAttributeClass,
                aggregateIdElementKind,
                aggregateIdElementType,
                aggregateIdAccessorName,
                aggregateIdAccessorKind,
                aggregateIdAccessorModifiers
        );

        return new RawCommandHandlerMetadata(
                aggregateAttributeClass, aggregateClassName, handlerMethodName, methodReturnType, rawAggregateIdMetadata
        );
    }

    public boolean isValid() {
        return aggregateAttributeClass != null && aggregateType != null
                && Strings.isNotNullOrEmpty(methodName);
    }

    record RawCommandHandlerMetadata(
            String aggregateAttributeClass, String aggregateClassName,
            String handlerMethodName,
            String methodReturnType,
            RawAggregateIdMetadata rawAggregateIdMetadata
    ) {
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
