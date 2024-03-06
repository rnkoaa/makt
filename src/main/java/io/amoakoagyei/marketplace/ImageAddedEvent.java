package io.amoakoagyei.marketplace;

import io.amoakoagyei.TargetAggregateId;

import java.util.UUID;

public class ImageAddedEvent {

    private final UUID aggregateId;

    public ImageAddedEvent(UUID aggregateId) {
        this.aggregateId = aggregateId;
    }

    @TargetAggregateId
    public UUID getAggregateId() {
        return aggregateId;
    }
}
