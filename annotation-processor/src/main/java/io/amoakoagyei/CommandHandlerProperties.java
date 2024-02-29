package io.amoakoagyei;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public record CommandHandlerProperties(
        String commandType,
        String aggregateType,
        String methodName,
        ElementKind commandElementType,
        String aggregateElementType,
        String aggregateIdAccessorName,
        ElementKind aggregateIdAccessorKind,
        Set<Modifier> modifiers
) {
    boolean isConstructor() {
        return Objects.equals(methodName, "<init>");
    }

    static CommandHandlerPropertiesBuilder builder() {
        return new CommandHandlerPropertiesBuilder();
    }

    @Override
    public String toString() {
        String mfs = modifiers == null ? "NULL" : modifiers
                .stream()
                .map(Enum::name)
                .collect(Collectors.joining(";"));
        return "%s,%s,%s,%s,%s,%s,%s,%s".formatted(
                commandType,
                aggregateType,
                methodName,
                commandElementType == null ? "NULL" : commandElementType.name(),
                aggregateElementType == null ? "NULL" : aggregateElementType,
                aggregateIdAccessorName == null ? "NULL" : aggregateIdAccessorName,
                aggregateIdAccessorKind == null ? "NULL" : aggregateIdAccessorKind.name(),
                mfs
        );
    }

    CommandHandlerPropertiesBuilder toBuilder() {
        return new CommandHandlerPropertiesBuilder()
                .commandType(commandType)
                .aggregateType(aggregateType)
                .handlerName(methodName);
    }

    static class CommandHandlerPropertiesBuilder {
        private String commandType;
        private String aggregateType;
        private String handlerName;
        private ElementKind commandElementType;
        private String aggregateIdAccessorName;
        private String aggregateIdType;
        private Set<Modifier> aggregateIdModifiers;
        private ElementKind aggregateIdAccessorKind;

        public CommandHandlerPropertiesBuilder commandType(String commandType) {
            this.commandType = commandType;
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
                    commandType,
                    aggregateType,
                    handlerName,
                    commandElementType,
                    aggregateIdType,
                    aggregateIdAccessorName,
                    aggregateIdAccessorKind,
                    aggregateIdModifiers
            );
        }

    }
}
