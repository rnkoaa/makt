package io.amoakoagyei.infra;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AggregateEventStore {
    private final Map<Object, List<AggregateEvent>> aggregateEvents = new ConcurrentHashMap<>();
    private static AggregateEventStore aggregateEventStore = null;

    private AggregateEventStore() {
    }

    public static AggregateEventStore getInstance() {
        if (aggregateEventStore == null) {
            aggregateEventStore = new AggregateEventStore();
        }
        return aggregateEventStore;
    }

    public void store(AggregateEvent event) {
        List<AggregateEvent> events = aggregateEvents.get(event.aggregateId());
        AggregateEvent versionedEvent;
        if (events == null) {
            versionedEvent = event.withVersion(0);
            events = new ArrayList<>();
        } else {
            versionedEvent = event.withVersion(events.size());
        }
        events.add(versionedEvent);
        aggregateEvents.put(event.aggregateId(), events);
    }

    public List<AggregateEvent> find(Object aggregateId) {
        return aggregateEvents.computeIfAbsent(aggregateId, k -> new ArrayList<>());
    }

    public void clear() {
        aggregateEvents.clear();
    }

    public int count() {
        return aggregateEvents.size();
    }
}
