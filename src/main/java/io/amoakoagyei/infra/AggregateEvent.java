package io.amoakoagyei.infra;

public record AggregateEvent(
        Object aggregateId,
        Object event,
        int version
) {
}
