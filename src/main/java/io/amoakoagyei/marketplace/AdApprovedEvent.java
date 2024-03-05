package io.amoakoagyei.marketplace;

import io.amoakoagyei.TargetAggregateId;

import java.util.UUID;

public record AdApprovedEvent(
        @TargetAggregateId
        UUID aggregateId,
        String approver
) {
}
