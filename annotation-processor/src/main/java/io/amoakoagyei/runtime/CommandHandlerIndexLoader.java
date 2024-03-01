package io.amoakoagyei.runtime;

import java.util.*;
import java.util.stream.Collectors;

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

                    var idMetadataInfo = handler.rawAggregateIdMetadata();
                    var aggregateIdType = ClassIndexLoader.loadClass(idMetadataInfo.aggregateIdElementType());
                    var aggregateKind = getKind(idMetadataInfo.aggregateIdElementKind());

                    Set<ElementModifier> modifiers = transform(idMetadataInfo.modifiers());
                    var aggregateIdMetadata = new AggregateIdMetadata(
                            commandType,
                            aggregateKind,
                            aggregateIdType.orElse(null),
                            idMetadataInfo.aggregateIdAccessorName(),
                            modifiers,
                            getKind(idMetadataInfo.aggregateIdAccessorKind())
                    );
                    commandTypes.put(
                            handler.commandClassName(),
                            new CommandHandlerMetadata(
                                    commandType,
                                    aggregateType,
                                    handler.handlerMethodName(),
                                    aggregateIdMetadata
                            )
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

    static Set<ElementModifier> transform(Set<String> modifiers) {
        return modifiers.stream()
                .map(CommandHandlerIndexLoader::transform)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    static AccessorKind getKind(String kind) {
        return switch (kind) {
            case "CLASS", "RECORD", "RECORD_COMPONENT", "FIELD", "METHOD" -> AccessorKind.valueOf(kind);
            default -> null;
        };
    }

    static ElementModifier transform(String modifier) {
        return switch (modifier) {
            case "PRIVATE", "PUBLIC", "FINAL", "PROTECTED" -> ElementModifier.valueOf(modifier);
            default -> null;
        };
    }

    public static Optional<AggregateIdMetadata> findAggregateIdMetadata(Class<?> clzz) {
        return findCommandHandler(clzz)
                .map(CommandHandlerMetadata::aggregateIdMetadata)
                .filter(AggregateIdMetadata::isValid);
    }
}
