package io.amoakoagyei.marketplace;

import io.amoakoagyei.TargetAggregateId;

import java.util.UUID;

public class PublishAdCommand {
    private final UUID id;

    public PublishAdCommand(UUID id) {
        this.id = id;
    }

    @TargetAggregateId
    public UUID getId() {
        return id;
    }
}
