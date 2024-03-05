package io.amoakoagyei;

import io.amoakoagyei.marketplace.*;
import io.amoakoagyei.runtime.AccessorKind;
import io.amoakoagyei.runtime.AggregateIdMetadata;
import io.amoakoagyei.runtime.ElementModifier;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class AggregateIdLoaderTest {

    @Test
    void findIdOnAggregateUsingAggregateIdMetadata() {
        var aggregateId = UUID.randomUUID();
        var aggregate = new MarketPlaceAd();
        aggregate.on(new AdCreatedEvent(aggregateId, "First Title"));

        assertThat(aggregate.getTitle()).isEqualTo("First Title");
        AssertionsForClassTypes.assertThat(aggregate.getId()).isEqualTo(aggregateId);

        var aggregateMetadata = new AggregateIdMetadata(
                MarketPlaceAd.class,
                null,
                UUID.class,
                "id",
                Set.of(ElementModifier.PRIVATE),
                AccessorKind.FIELD
        );

        var aggregateIdResult = AggregateIdLoader.extractAggregateId(aggregateMetadata, aggregate);
        assertThat(aggregateIdResult.isSuccess()).isTrue();
        assertThat(aggregateIdResult.getOrNull()).satisfies(aggregateItem -> {
            assertThat(aggregateItem).isEqualTo(aggregateId);
        });
    }

    @Test
    void loadIdFromValidCommand() {
        var updateTitleCommand = new UpdateTitleCommand(UUID.randomUUID(), "update title");
        var aggregateIdOptions = AggregateIdLoader.extractAggregateId(updateTitleCommand);
        assertThat(aggregateIdOptions.isSuccess()).isTrue();
        assertThat(aggregateIdOptions).satisfies(s -> {
            AggregateIdLoader.AggregateIdOptions idOptions = s.getOrNull();
            assertThat(idOptions.id())
                    .isNotNull()
                    .isInstanceOf(UUID.class)
                    .isEqualTo(updateTitleCommand.aggregateId());
        });
    }

    @Test
    void loadIdFromValidEvent() {
        var titleUpdatedEvent = new TitleUpdatedEvent(UUID.randomUUID(), "update title");
        var aggregateIdOptions = AggregateIdLoader.extractAggregateId(titleUpdatedEvent);
        assertThat(aggregateIdOptions.isSuccess()).isTrue();
        assertThat(aggregateIdOptions).satisfies(s -> {
            AggregateIdLoader.AggregateIdOptions idOptions = s.getOrNull();
            assertThat(idOptions.id())
                    .isNotNull()
                    .isInstanceOf(UUID.class)
                    .isEqualTo(titleUpdatedEvent.aggregateId());
        });
    }

    @Test
    void loadIdFromValidEventClass() {
        var adCompletedEvent = new AdCompletedEvent(UUID.randomUUID());
        var aggregateIdOptions = AggregateIdLoader.extractAggregateId(adCompletedEvent);
        assertThat(aggregateIdOptions.isSuccess()).isTrue();
        assertThat(aggregateIdOptions).satisfies(s -> {
            AggregateIdLoader.AggregateIdOptions idOptions = s.getOrNull();
            assertThat(idOptions.id())
                    .isNotNull()
                    .isInstanceOf(UUID.class)
                    .isEqualTo(adCompletedEvent.getAggregateId());
        });
    }
}
