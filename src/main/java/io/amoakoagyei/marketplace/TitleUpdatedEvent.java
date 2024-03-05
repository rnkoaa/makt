package io.amoakoagyei.marketplace;

import io.amoakoagyei.TargetAggregateId;

import java.util.UUID;

public record TitleUpdatedEvent(
        @TargetAggregateId
        UUID aggregateId,
        String title
) {
}
