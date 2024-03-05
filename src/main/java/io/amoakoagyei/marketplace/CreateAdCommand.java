package io.amoakoagyei.marketplace;

import io.amoakoagyei.Command;
import io.amoakoagyei.TargetAggregateId;

import java.util.UUID;

public record CreateAdCommand(
        @TargetAggregateId
        UUID id,
        String title) implements Command {
}
