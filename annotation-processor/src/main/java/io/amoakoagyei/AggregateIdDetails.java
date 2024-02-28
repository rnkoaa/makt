package io.amoakoagyei;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import java.util.HashSet;
import java.util.Set;

public record AggregateIdDetails(
        String commandClassName,
        String accessorName,
        Set<Modifier> modifiers,
        String accessorType, // FQDN of type
        ElementKind accessorKind, // (FIELD, METHOD, RECORD_COMPONENT)
        boolean isRecord//
) {
    public boolean isValid() {
        return Strings.isNotNullOrEmpty(commandClassName) && Strings.isNotNullOrEmpty(accessorName)
                && !modifiers.isEmpty();
    }

    static class AggregateIdDetailsBuilder {
        private String commandClassName;
        private String accessorName;
        private Set<Modifier> modifiers;
        private String accessorType; // FQDN of type
        private ElementKind accessorKind;// (FIELD, METHOD, RECORD_COMPONENT)
        private boolean isRecord;

        AggregateIdDetailsBuilder commandClassName(String commandClassName) {
            this.commandClassName = commandClassName;
            return this;
        }

        AggregateIdDetailsBuilder accessorType(String accessorType) {
            this.accessorType = accessorType;
            return this;
        }

        AggregateIdDetailsBuilder accessorName(String accessorName) {
            this.accessorName = accessorName;
            return this;
        }

        AggregateIdDetailsBuilder isRecord(boolean isRecord) {
            this.isRecord = isRecord;
            return this;
        }

        AggregateIdDetailsBuilder accessorKind(ElementKind accessorKind) {
            this.accessorKind = accessorKind;
            return this;
        }

        AggregateIdDetailsBuilder modifiers(Set<Modifier> modifiers) {
            this.modifiers = modifiers;
            return this;
        }

        public AggregateIdDetails build() {
            return new AggregateIdDetails(
                    commandClassName,
                    accessorName,
                    (modifiers == null) ? new HashSet<>() : modifiers,
                    accessorType, accessorKind, isRecord);
        }
    }
}
