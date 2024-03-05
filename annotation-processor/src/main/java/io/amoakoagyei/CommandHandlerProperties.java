package io.amoakoagyei;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import java.util.Set;
import java.util.stream.Collectors;

public record CommandHandlerProperties(
        String aggregateAttributeType, // Command or Event Type
        String aggregateType,
        String methodName,
        String methodReturnType,
        ElementKind commandElementType,
        String aggregateElementType,
        String aggregateIdAccessorName,
        ElementKind aggregateIdAccessorKind,
        Set<Modifier> modifiers
) {
    static CommandHandlerPropertiesBuilder builder() {
        return new CommandHandlerPropertiesBuilder();
    }

    @Override
    public String toString() {
        String mfs = modifiers == null ? "NULL" : modifiers
                .stream()
                .map(Enum::name)
                .collect(Collectors.joining(";"));
        return "%s,%s,%s,%s,%s,%s,%s,%s,%s".formatted(
                aggregateAttributeType,
                aggregateType,
                methodName,
                methodReturnType == null ? "void.class" : methodReturnType,
                commandElementType == null ? "NULL" : commandElementType.name(),
                aggregateElementType == null ? "NULL" : aggregateElementType,
                aggregateIdAccessorName == null ? "NULL" : aggregateIdAccessorName,
                aggregateIdAccessorKind == null ? "NULL" : aggregateIdAccessorKind.name(),
                mfs
        );
    }

    CommandHandlerPropertiesBuilder toBuilder() {
        return new CommandHandlerPropertiesBuilder()
                .aggregateAttributeType(aggregateAttributeType)
                .aggregateType(aggregateType)
                .handlerName(methodName);
    }

    static class CommandHandlerPropertiesBuilder {
        private String aggregateAttributeType;
        private String aggregateType;
        private String handlerName;
        private String methodReturnType;
        private ElementKind commandElementType;
        private String aggregateIdAccessorName;
        private String aggregateIdType;
        private Set<Modifier> aggregateIdModifiers;
        private ElementKind aggregateIdAccessorKind;

        public CommandHandlerPropertiesBuilder aggregateAttributeType(String aggregateAttributeType) {
            this.aggregateAttributeType = aggregateAttributeType;
            return this;
        }

        public CommandHandlerPropertiesBuilder aggregateType(String aggregateType) {
            this.aggregateType = aggregateType;
            return this;
        }

        public CommandHandlerPropertiesBuilder handlerName(String handlerName) {
            this.handlerName = handlerName;
            return this;
        }
        public CommandHandlerPropertiesBuilder methodReturnType(String methodReturnType) {
            this.methodReturnType = methodReturnType;
            return this;
        }

        public CommandHandlerPropertiesBuilder commandElementType(ElementKind commandElementType) {
            this.commandElementType = commandElementType;
            return this;
        }

        public CommandHandlerPropertiesBuilder aggregateIdAccessorName(String aggregateIdAccessorName) {
            this.aggregateIdAccessorName = aggregateIdAccessorName;
            return this;
        }

        public CommandHandlerPropertiesBuilder aggregateIdType(String aggregateIdType) {
            this.aggregateIdType = aggregateIdType;
            return this;
        }

        public CommandHandlerPropertiesBuilder aggregateIdModifiers(Set<Modifier> aggregateIdModifiers) {
            this.aggregateIdModifiers = aggregateIdModifiers;
            return this;
        }

        public CommandHandlerPropertiesBuilder aggregateIdAccessorKind(ElementKind aggregateIdAccessorKind) {
            this.aggregateIdAccessorKind = aggregateIdAccessorKind;
            return this;
        }

        CommandHandlerProperties build() {
            return new CommandHandlerProperties(
                    aggregateAttributeType,
                    aggregateType,
                    handlerName,
                    methodReturnType,
                    commandElementType,
                    aggregateIdType,
                    aggregateIdAccessorName,
                    aggregateIdAccessorKind,
                    aggregateIdModifiers
            );
        }

    }
}
