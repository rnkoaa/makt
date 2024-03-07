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
        Set<ElementModifier> modifiers,
        Class<?> methodReturnType,
        AggregateIdMetadata aggregateIdMetadata
) {
    public boolean isConstructor() {
        return Objects.equals(methodName, "<init>");
    }

    public boolean isMethodPublic() {
        return modifiers.contains(ElementModifier.PUBLIC);
    }

    public boolean isMethodProtected() {
        return modifiers.contains(ElementModifier.PROTECTED);
    }

    public boolean isMethodPrivate() {
        return modifiers.contains(ElementModifier.PRIVATE);
    }

    static RawCommandHandlerMetadata fromLine(String line) {
        var splitParts = line.split(",");
        if (splitParts.length < 9) {
            return null;
        }
        var aggregateAttributeClass = splitParts[0];
        var aggregateClassName = splitParts[1];
        var handlerMethodName = splitParts[2];
        var methodModifiers = splitParts[3];
        var methodReturnType = splitParts[4];
        var aggregateIdElementKind = splitParts[5];
        var aggregateIdElementType = splitParts[6];
        var aggregateIdAccessorName = splitParts[7];
        var aggregateIdAccessorKind = splitParts[8];
        var aggregateIdAccessorModifiers = splitParts[9] == null ?
                new HashSet<String>() :
                Sets.newHashSet(splitParts[9]);

        var rawAggregateIdMetadata = new RawAggregateIdMetadata(
                aggregateAttributeClass,
                aggregateIdElementKind,
                aggregateIdElementType,
                aggregateIdAccessorName,
                aggregateIdAccessorKind,
                aggregateIdAccessorModifiers
        );

        var itemMethodModifiers = methodModifiers == null ? new HashSet<String>() : Sets.newHashSet(methodModifiers.split(";"));
        return new RawCommandHandlerMetadata(
                aggregateAttributeClass, aggregateClassName, handlerMethodName, itemMethodModifiers, methodReturnType, rawAggregateIdMetadata
        );
    }

    public boolean isValid() {
        return aggregateAttributeClass != null && aggregateType != null
                && Strings.isNotNullOrEmpty(methodName);
    }

    record RawCommandHandlerMetadata(
            String aggregateAttributeClass, String aggregateClassName,
            String handlerMethodName,
            Set<String> modifiers,
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
