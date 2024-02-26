package io.amoakoagyei.marketplace;

import io.amoakoagyei.Event;

import java.util.UUID;

public record TitleUpdatedEvent(
        UUID aggregateId,
        String title
) implements Event {
    @Override
    public UUID getAggregateId() {
        return aggregateId;
    }
}
