package io.amoakoagyei.runtime;

import java.lang.reflect.Modifier;
import java.util.Set;

public record AggregateIdMetadata(
        Class<?> commandClass,
        Class<?> aggregateIdClass,
        String accessorName,
        Set<Modifier> modifiers,
        AccessorKind accessorType, // field, method, record_component
        boolean isRecord
) {
}
