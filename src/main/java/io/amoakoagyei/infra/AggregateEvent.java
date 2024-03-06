package io.amoakoagyei.infra;

public record AggregateEvent(
        Object aggregateId,
        Object event,
        Class<?> aggregateType,
        int version
) {

    public AggregateEvent(Object aggregateId, Object event, Class<?> aggregateType) {
        this(aggregateId, event, aggregateType, 0);
    }

    public AggregateEvent withVersion(int version) {
        return new AggregateEvent(aggregateId, event, aggregateType, version);
    }
}
