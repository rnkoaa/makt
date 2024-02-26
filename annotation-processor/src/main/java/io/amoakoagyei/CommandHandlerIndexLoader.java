package io.amoakoagyei;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static io.amoakoagyei.AbstractAnnotationProcessor.ANNOTATED_INDEX_PREFIX;
import static io.amoakoagyei.CommandHandlerProcessor.COMMAND_HANDLER_INDEX;

public class CommandHandlerIndexLoader {
    private static final Map<String, CommandHandlerDetails> commandTypes = new HashMap<>();

    static {
        String path = ANNOTATED_INDEX_PREFIX + COMMAND_HANDLER_INDEX;
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        ClassIndexLoader.loadResourceFile(classloader, path).stream()
                .map(CommandHandlerProperties::fromLine)
                .filter(Objects::nonNull)
                .forEach(handler -> {
                    var commandType = ClassIndexLoader.loadClass(handler.commandType()).orElse(null);
                    var aggregateType = ClassIndexLoader.loadClass(handler.aggregateType()).orElse(null);
                    commandTypes.put(
                            handler.commandType(), new CommandHandlerDetails(commandType, aggregateType, handler.methodName())
                    );
                });
    }

    public static Optional<CommandHandlerDetails> findCommandHandler(Class<?> commandType) {
        if (commandType == null) {
            return Optional.empty();
        }

        CommandHandlerDetails commandHandlerDetails = commandTypes.get(commandType.getCanonicalName());
        if (commandHandlerDetails == null) {
            return Optional.empty();
        }

        if (commandHandlerDetails.aggregateType() == null || commandHandlerDetails.commandType() == null) {
            return Optional.empty();
        }

        return Optional.of(commandHandlerDetails);
    }

}
