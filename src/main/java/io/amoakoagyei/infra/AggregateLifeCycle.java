package io.amoakoagyei.infra;

import io.amoakoagyei.AggregateIdLoader;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AggregateLifeCycle {
    private static final MethodHandles.Lookup lookup = MethodHandles.lookup();
    private static final Map<Object, List<AggregateEvent>> aggregateEvents = new ConcurrentHashMap<>();
    private static final AggregateEventStore aggregateEventStore = AggregateEventStore.getInstance();

    public static void apply(Object event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }

        var aggregateOptions = AggregateIdLoader.extractAggregateId(event);
        aggregateOptions.map(aggregateIdOptions -> {
            var aggregateType = aggregateIdOptions.aggregateType();
            var aggregateId = aggregateIdOptions.id();
            aggregateEventStore.store(new AggregateEvent(aggregateId, event, aggregateType));
            return aggregateId;
        });
    }
}
