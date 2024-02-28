package io.amoakoagyei.runtime;

import io.amoakoagyei.CommandHandlerProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static io.amoakoagyei.AbstractAnnotationProcessor.ANNOTATED_INDEX_PREFIX;
import static io.amoakoagyei.CommandHandlerProcessor.COMMAND_HANDLER_INDEX;

public class CommandHandlerIndexLoader {
    private static final Map<String, CommandHandlerMetadata> commandTypes = new HashMap<>();

    static {
        String path = ANNOTATED_INDEX_PREFIX + COMMAND_HANDLER_INDEX;
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        ClassIndexLoader.loadResourceFile(classloader, path).stream()
                .map(CommandHandlerMetadata::fromLine)
                .filter(Objects::nonNull)
                .forEach(handler -> {
                    var commandType = ClassIndexLoader.loadClass(handler.commandClassName()).orElse(null);
                    var aggregateType = ClassIndexLoader.loadClass(handler.aggregateClassName()).orElse(null);
                    commandTypes.put(
                            handler.commandClassName(), new CommandHandlerMetadata(commandType, aggregateType, handler.aggregateClassName(), null)
                    );
                });
    }

    public static Optional<CommandHandlerMetadata> findCommandHandler(Class<?> commandType) {
        if (commandType == null) {
            return Optional.empty();
        }

        CommandHandlerMetadata commandHandlerMetadata = commandTypes.get(commandType.getCanonicalName());
        if (commandHandlerMetadata == null) {
            return Optional.empty();
        }

        if (!commandHandlerMetadata.isValid()) {
            return Optional.empty();
        }

        return Optional.of(commandHandlerMetadata);
    }

}
