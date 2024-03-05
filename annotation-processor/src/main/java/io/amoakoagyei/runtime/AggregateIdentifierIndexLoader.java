package io.amoakoagyei.runtime;

import com.google.common.collect.Sets;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.amoakoagyei.AbstractAnnotationProcessor.ANNOTATED_INDEX_PREFIX;
import static io.amoakoagyei.AggregateIdentifierProcessor.COMMAND_HANDLER_INDEX;

public class AggregateIdentifierIndexLoader {
    private static final Map<Class<?>, AggregateIdMetadata> commandTypes = new HashMap<>();

    // id,java.util.UUID,io.amoakoagyei.marketplace.MarketPlaceAd,FIELD,PRIVATE
    static {
        String path = ANNOTATED_INDEX_PREFIX + COMMAND_HANDLER_INDEX;
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        ClassIndexLoader.loadResourceFile(classloader, path).stream()
                .map(it -> it.split(","))
                .filter(it -> it.length >= 5)
                .map(it -> {
                    var modifierIdentifiers = Sets.newHashSet(it[4].split(";"));
                    var modifierSet = CommandHandlerIndexLoader.transform(modifierIdentifiers);

                    var accessorName = it[0];
                    var aggregateIdType = ClassIndexLoader.loadClass(it[1]).orElse(null);
                    var enclosingClass = ClassIndexLoader.loadClass(it[2]).orElse(null);
                    var modifiers = CommandHandlerIndexLoader.transform(Sets.newHashSet(it[4].split(";")));
                    var kind = CommandHandlerIndexLoader.getKind(it[3]);
                    return new AggregateIdMetadata(
                            enclosingClass,
                            null,
                            aggregateIdType,
                            accessorName,
                            modifiers,
                            kind

                    );
                })
                .forEach(it -> commandTypes.put(it.enclosingClass(), it));
    }

    public static Optional<AggregateIdMetadata> findAggregateIdMetadata(Class<?> enclosingClass) {
        return Optional.ofNullable(commandTypes.get(enclosingClass));
    }
}
