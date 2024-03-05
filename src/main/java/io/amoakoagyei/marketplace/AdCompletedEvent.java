package io.amoakoagyei.marketplace;

import io.amoakoagyei.TargetAggregateId;

import java.util.UUID;

public class AdCompletedEvent {

    @TargetAggregateId
    private final UUID aggregateId;

    public AdCompletedEvent(UUID aggregateId) {
        this.aggregateId = aggregateId;
    }

    public UUID getAggregateId() {
        return aggregateId;
    }
}
