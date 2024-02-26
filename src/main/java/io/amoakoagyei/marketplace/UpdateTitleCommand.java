package io.amoakoagyei.marketplace;

import io.amoakoagyei.TargetAggregateId;

import java.util.UUID;

public record UpdateTitleCommand(

        @TargetAggregateId
        UUID aggregateId,

        String title
) {
}
