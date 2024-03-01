package io.amoakoagyei.runtime;

import java.util.Objects;
import java.util.Set;

public record AggregateIdMetadata(
        Class<?> commandClass,
        AccessorKind commandElementKind,
        Class<?> aggregateIdClass,
        String accessorName,
        Set<ElementModifier> modifiers,
        AccessorKind accessorType // field, method, record_component
) {

    public boolean isRecordCommand() {
        return commandElementKind == AccessorKind.RECORD;
    }

    public boolean isValid() {
        return commandClass != null && aggregateIdClass != null
                && !Objects.equals(accessorName, "NULL");
    }
}
