package io.amoakoagyei.marketplace;

import java.util.UUID;

public record AdPublishedEvent(
        UUID aggregateId
) {
}
