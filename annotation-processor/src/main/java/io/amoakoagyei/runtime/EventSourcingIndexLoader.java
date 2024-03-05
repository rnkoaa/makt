package io.amoakoagyei.runtime;

import com.google.common.collect.Sets;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import static io.amoakoagyei.AbstractAnnotationProcessor.ANNOTATED_INDEX_PREFIX;
import static io.amoakoagyei.EventSourcingHandlerProcessor.EVENT_SOURCE_HANDLER_INDEX;
import static io.amoakoagyei.runtime.CommandHandlerIndexLoader.transform;

public class EventSourcingIndexLoader {
    private static final Map<Class<?>, EventSourcingMetadata> eventSourcingMetadata = new HashMap<>();

    static {
        String path = ANNOTATED_INDEX_PREFIX + EVENT_SOURCE_HANDLER_INDEX;
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        ClassIndexLoader.loadResourceFile(classloader, path).stream()
                .map(line -> {
                    String[] eventSourcingLines = line.split(",");
                    var eventClass = ClassIndexLoader.loadClass(eventSourcingLines[0]).orElse(null);
                    var aggregateHandler = ClassIndexLoader.loadClass(eventSourcingLines[1]).orElse(null);
                    var eventHandlerMethod = eventSourcingLines[2];
                    var handlerReturnTypeName = eventSourcingLines[3];

                    Class<?> handlerReturnType = null;
                    if (handlerReturnTypeName == null || handlerReturnTypeName.equals("void")) {
                        handlerReturnType = void.class;
                    } else {
                        handlerReturnType = ClassIndexLoader.loadClass(handlerReturnTypeName).orElse(void.class);
                    }

                    var handlerMethodModifiers = eventSourcingLines[4] == null ?
                            new HashSet<String>() :
                            Sets.newHashSet(eventSourcingLines[4].split(";"));

                    var modifiers = transform(handlerMethodModifiers);

                    return new EventSourcingMetadata(
                            eventClass,
                            aggregateHandler,
                            eventHandlerMethod,
                            handlerReturnType,
                            modifiers
                    );
                })
                .filter(EventSourcingMetadata::isValid)
                .forEach(it -> eventSourcingMetadata.put(it.eventClass(), it));
    }

    public static Optional<EventSourcingMetadata> findAggregateIdMetadata(Class<?> eventClass) {
        return Optional.ofNullable(eventSourcingMetadata.get(eventClass));
    }
}
