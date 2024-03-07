package io.amoakoagyei.infra;

import io.amoakoagyei.Result;
import io.amoakoagyei.marketplace.AdApprovedEvent;
import io.amoakoagyei.marketplace.AdCreatedEvent;
import io.amoakoagyei.marketplace.MarketPlaceAd;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class AggregateEventStoreTest {

    @Test
    void storeAggregateEvents() {
        var aggregateEventStore = AggregateEventStore.getInstance();
        var aggregateId = UUID.randomUUID();
        var adCreatedEvent = new AdCreatedEvent(aggregateId, "title 1");
        aggregateEventStore.store(new AggregateEvent(aggregateId, adCreatedEvent, MarketPlaceAd.class, 0));
        assertThat(aggregateEventStore.count()).isEqualTo(1);
        aggregateEventStore.clear();
    }

    @Test
    void storeMultipleAggregateEvents() {
        var aggregateEventStore = AggregateEventStore.getInstance();
        var aggregateId = UUID.randomUUID();
        var adCreatedEvent = new AdCreatedEvent(aggregateId, "title 1");
        aggregateEventStore.store(new AggregateEvent(aggregateId, adCreatedEvent, MarketPlaceAd.class, 0));
        assertThat(aggregateEventStore.count()).isEqualTo(1);

        var adApprovedEvent = new AdApprovedEvent(aggregateId, "Richard");
        aggregateEventStore.store(new AggregateEvent(aggregateId, adApprovedEvent, MarketPlaceAd.class, 1));
        assertThat(aggregateEventStore.count()).isEqualTo(1);

        List<AggregateEvent> aggregateEvents = aggregateEventStore.find(aggregateId);
        assertThat(aggregateEvents.size()).isEqualTo(2);
        aggregateEventStore.clear();
    }

    @Test
    void loadStoredAggregateEvents() {
        var aggregateEventStore = AggregateEventStore.getInstance();
        var aggregateId = UUID.randomUUID();
        var adCreatedEvent = new AdCreatedEvent(aggregateId, "title 1");
        aggregateEventStore.store(new AggregateEvent(aggregateId, adCreatedEvent, MarketPlaceAd.class, 0));

        var adApprovedEvent = new AdApprovedEvent(aggregateId, "Richard");
        aggregateEventStore.store(new AggregateEvent(aggregateId, adApprovedEvent, MarketPlaceAd.class, 1));

        // when
        Result<Object> loadedResult = aggregateEventStore.load(aggregateId);
        assertThat(loadedResult.isSuccess()).isTrue();
        assertThat(loadedResult.getOrNull()).isNotNull().satisfies(a -> {
            assertThat(a.getClass()).isEqualTo(MarketPlaceAd.class);

            MarketPlaceAd marketPlaceAd = (MarketPlaceAd) a;
            assertThat(marketPlaceAd.getTitle()).isEqualTo(adCreatedEvent.title());
            assertThat(marketPlaceAd.getApprover()).isEqualTo(adApprovedEvent.approver());
        });
        aggregateEventStore.clear();
    }

    @Test
    void loadStoredMissingAggregate() {
        var aggregateEventStore = AggregateEventStore.getInstance();
        var aggregateId = UUID.randomUUID();
        var result = aggregateEventStore.load(aggregateId);
        assertThat(result.isFailure()).isTrue();
        aggregateEventStore.clear();
    }
}