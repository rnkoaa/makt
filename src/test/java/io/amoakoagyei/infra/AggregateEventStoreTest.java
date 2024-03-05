package io.amoakoagyei.infra;

import io.amoakoagyei.marketplace.AdApprovedEvent;
import io.amoakoagyei.marketplace.AdCreatedEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class AggregateEventStoreTest {

    @Test
    void storeAggregateEvents() {
        var aggregateEventStore = new AggregateEventStore();
        var aggregateId = UUID.randomUUID();
        var adCreatedEvent = new AdCreatedEvent(aggregateId, "title 1");
        aggregateEventStore.store(new AggregateEvent(aggregateId, adCreatedEvent, 0));
        assertThat(aggregateEventStore.count()).isEqualTo(1);
        aggregateEventStore.clear();
    }

    @Test
    void storeMultipleAggregateEvents() {
        var aggregateEventStore = new AggregateEventStore();
        var aggregateId = UUID.randomUUID();
        var adCreatedEvent = new AdCreatedEvent(aggregateId, "title 1");
        aggregateEventStore.store(new AggregateEvent(aggregateId, adCreatedEvent, 0));
        assertThat(aggregateEventStore.count()).isEqualTo(1);

        var adApprovedEvent = new AdApprovedEvent(aggregateId, "Richard");
        aggregateEventStore.store(new AggregateEvent(aggregateId, adApprovedEvent, 1));
        assertThat(aggregateEventStore.count()).isEqualTo(1);

        List<AggregateEvent> aggregateEvents = aggregateEventStore.find(aggregateId);
        assertThat(aggregateEvents.size()).isEqualTo(2);
        aggregateEventStore.clear();
    }
}