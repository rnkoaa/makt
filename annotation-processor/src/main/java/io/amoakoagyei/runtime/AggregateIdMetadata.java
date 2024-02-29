package io.amoakoagyei.runtime;

import java.util.Set;

public record AggregateIdMetadata(
        Class<?> commandClass,
        Class<?> aggregateIdClass,
        AccessorKind aggregateIdKind,
        String accessorName,
        Set<ElementModifier> modifiers,
        AccessorKind accessorType // field, method, record_component
) {

    boolean isRecord() {
        return aggregateIdKind == AccessorKind.RECORD;
    }
}
