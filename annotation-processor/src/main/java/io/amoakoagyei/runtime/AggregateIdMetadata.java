package io.amoakoagyei.runtime;

import java.util.Objects;
import java.util.Set;

public record AggregateIdMetadata(
        Class<?> enclosingClass,
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
        return enclosingClass != null && aggregateIdClass != null
                && !Objects.equals(accessorName, "NULL");
    }

    public boolean isPublicAccessor() {
       return modifiers.contains(ElementModifier.PUBLIC);
    }

    public boolean isPrivateAccessor() {
       return modifiers.contains(ElementModifier.PRIVATE);
    }
    public boolean isProtectedAccessor() {
       return modifiers.contains(ElementModifier.PROTECTED);
    }
}
