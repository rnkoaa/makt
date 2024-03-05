package io.amoakoagyei.runtime;

import io.amoakoagyei.Strings;

import java.util.Set;

// io.amoakoagyei.marketplace.AdCreatedEvent,io.amoakoagyei.marketplace.MarketPlaceAd,on,NULL,PUBLIC
public record EventSourcingMetadata(
        Class<?> eventClass,
        Class<?> aggregateClass,
        String accessorName,
        Class<?> methodReturnType,
        Set<ElementModifier> modifiers
) {

    public boolean isValid() {
        return eventClass != null &&
                aggregateClass != null &&
                Strings.isNotNullOrEmpty(accessorName)
                && methodReturnType != null
                && modifiers != null;
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
