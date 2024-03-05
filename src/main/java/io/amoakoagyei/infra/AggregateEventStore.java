package io.amoakoagyei.infra;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AggregateEventStore {
    private final Map<Object, List<AggregateEvent>> aggregateEvents = new ConcurrentHashMap<>();

    public void store(AggregateEvent event) {
        aggregateEvents.computeIfAbsent(event.aggregateId(), k -> new ArrayList<>()).add(event);
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
