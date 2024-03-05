package io.amoakoagyei;

import io.amoakoagyei.infra.AggregateLifeCycle;
import io.amoakoagyei.marketplace.*;
import io.amoakoagyei.runtime.EventSourcingIndexLoader;
import io.amoakoagyei.runtime.EventSourcingMetadata;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class EventSourcingIndexLoaderTest {

    @Test
    void loadEventSourcingMetadata() {
        var event = new AdCreatedEvent(UUID.randomUUID(), "title one");
        Optional<EventSourcingMetadata> aggregateIdMetadata = EventSourcingIndexLoader.findAggregateIdMetadata(event.getClass());
        assertThat(aggregateIdMetadata).isPresent().hasValueSatisfying(t -> {
            assertThat(t.aggregateClass()).isNotNull().isEqualTo(MarketPlaceAd.class);
            assertThat(t.eventClass()).isNotNull().isEqualTo(AdCreatedEvent.class);
            assertThat(t.accessorName()).isEqualTo("on");
            assertThat(t.methodReturnType()).isNotNull().isEqualTo(void.class);
            assertThat(t.isPublicAccessor()).isTrue();
        });
    }

    @Test
    void aggregateCanBeAppliedUsingMetadata() {
        var event = new AdCreatedEvent(UUID.randomUUID(), "title one");
        var appliedEventResult = AggregateLifeCycle.applyEvent(new MarketPlaceAd(), event);
        assertThat(appliedEventResult.isSuccess()).isTrue();
        assertThat(appliedEventResult.getOrNull()).satisfies(s -> {
            assertThat(s).isInstanceOf(MarketPlaceAd.class);
            var marketPlaceAd = (MarketPlaceAd) s;
            assertThat(marketPlaceAd.getTitle()).isEqualTo("title one");
            assertThat(marketPlaceAd.getId()).isEqualTo(event.getAggregateId());
        });
    }

    @Test
    void multipleEventsProcessEvent() {
        var aggregateId = UUID.randomUUID();
        List<Object> events = List.of(
                new AdCreatedEvent(aggregateId, "title one"),
                new TitleUpdatedEvent(aggregateId, "title two"),
                new AdApprovedEvent(aggregateId, "richard"),
                new AdPublishedEvent(aggregateId),
                new AdDisabledEvent(aggregateId)
        );

        var marketPlaceAd = new MarketPlaceAd();
        var marketPlaceApplied = AggregateLifeCycle.applyEvents(marketPlaceAd, events);
        assertThat(marketPlaceApplied.isSuccess()).isTrue();
        assertThat(marketPlaceApplied.getOrNull()).satisfies(s -> {
            assertThat(s).isInstanceOf(MarketPlaceAd.class);
            var marketPlace = (MarketPlaceAd) s;
            assertThat(marketPlace.getTitle()).isEqualTo("title two");
            assertThat(marketPlace.getApprover()).isEqualTo("richard");
            assertThat(marketPlace.isEnabled()).isFalse();
            assertThat(marketPlace.isPublished()).isTrue();
            System.out.println(marketPlace);
        });
    }
}