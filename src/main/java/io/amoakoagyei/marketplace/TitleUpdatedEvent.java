package io.amoakoagyei.marketplace;

import io.amoakoagyei.Event;
import io.amoakoagyei.TargetAggregateId;

import java.util.UUID;

public record TitleUpdatedEvent(
        @TargetAggregateId
        UUID aggregateId,
        String title
) implements Event {
    @Override
    public UUID getAggregateId() {
        return aggregateId;
    }
}
