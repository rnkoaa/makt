package io.amoakoagyei;

import java.util.HashMap;
import java.util.Map;

public class AggregateStore {
    private static final Map<Object, Aggregate> store = new HashMap<>();

    public void save(Object aggregateId, Aggregate aggregate) {
        store.put(aggregateId, aggregate);
    }

    public Result<Aggregate> load(Object aggregateId) {
        Aggregate aggregate = store.get(aggregateId);
        if (aggregate == null) {
            return Result.failure(new RuntimeException("Aggregate with id " + aggregateId + " not found"));
        }
        return Result.success(aggregate);
    }

    public boolean contains(Object aggregateId) {
        return store.containsKey(aggregateId);
    }

    public int size() {
        return store.size();
    }

    public void clear() {
        store.clear();
    }

}
