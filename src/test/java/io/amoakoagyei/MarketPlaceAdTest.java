package io.amoakoagyei;

import io.amoakoagyei.infra.AggregateEvent;
import io.amoakoagyei.infra.AggregateEventStore;
import io.amoakoagyei.marketplace.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class MarketPlaceAdTest {
    AggregateEventStore aggregateEventStore = AggregateEventStore.getInstance();

    @AfterEach
    void tearDown() {
        aggregateEventStore.clear();
    }

    @Test
    void applyEvents() {
        var adId = UUID.randomUUID();
        var ad = new MarketPlaceAd(new CreateAdCommand(adId, "First Title"));
        ad.handle(new UpdateTitleCommand(adId, "Second Title"));
        ad.handle(new ApproveAdCommand(adId, "Richard"));
        ad.handle(new DisableAdCommand(adId));

        List<AggregateEvent> aggregateEvents = aggregateEventStore.find(adId);
        assertThat(aggregateEvents).hasSize(4);
        assertThat(aggregateEvents.getLast()).satisfies(a -> {
            assertThat(a.version()).isEqualTo(3);
            assertThat(a.event()).isInstanceOf(AdDisabledEvent.class);
        });
    }
}
