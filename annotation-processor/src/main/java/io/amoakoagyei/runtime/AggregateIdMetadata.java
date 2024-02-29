package io.amoakoagyei.runtime;

import java.util.Objects;
import java.util.Set;

public record AggregateIdMetadata(
        Class<?> commandClass,
        Class<?> aggregateIdClass,
        AccessorKind aggregateIdKind,
        String accessorName,
        Set<ElementModifier> modifiers,
        AccessorKind accessorType // field, method, record_component
) {

    public boolean isRecord() {
        return aggregateIdKind == AccessorKind.RECORD;
    }

    public boolean isValid() {
        return commandClass != null && aggregateIdClass != null
                && !Objects.equals(accessorName, "NULL");
    }
}
