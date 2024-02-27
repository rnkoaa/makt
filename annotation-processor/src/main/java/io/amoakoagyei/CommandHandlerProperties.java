package io.amoakoagyei;

import java.util.Objects;

public record CommandHandlerProperties(
        String commandType,
        String aggregateType,
        String methodName
) {
    boolean isConstructor() {
        return Objects.equals(methodName, "<init>");
    }

    static CommandHandlerProperties fromLine(String line) {
        var splitParts = line.split(",");
        if (splitParts.length < 3) {
            return null;
        }
        return new CommandHandlerProperties(
                splitParts[0], splitParts[1], splitParts[2]
        );
    }

    static CommandHandlerPropertiesBuilder builder() {
        return new CommandHandlerPropertiesBuilder();
    }

    @Override
    public String toString() {
        return "%s,%s,%s,%s".formatted(commandType, aggregateType, methodName, isConstructor());
    }

    static class CommandHandlerPropertiesBuilder {
        private String commandType;
        private String aggregateType;
        private String handlerName;

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

        CommandHandlerProperties build() {
            return new CommandHandlerProperties(
                    commandType,
                    aggregateType,
                    handlerName
            );
        }
    }
}
