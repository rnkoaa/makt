package io.amoakoagyei.marketplace;

import io.amoakoagyei.TargetAggregateId;

import java.util.UUID;

public record AdDisabledEvent(
        UUID aggregateId
) {
}
