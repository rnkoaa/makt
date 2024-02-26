package io.amoakoagyei.marketplace;

import io.amoakoagyei.Event;

import java.util.UUID;

public record AdCreatedEvent(UUID id, String title) implements Event {
    @Override
    public UUID getAggregateId() {
        return id;
    }
}
