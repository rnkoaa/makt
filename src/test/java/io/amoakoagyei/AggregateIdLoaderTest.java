package io.amoakoagyei;

import io.amoakoagyei.marketplace.AdCreatedEvent;
import io.amoakoagyei.marketplace.CreateAdCommand;
import io.amoakoagyei.marketplace.MarketPlaceAd;
import io.amoakoagyei.marketplace.UpdateTitleCommand;
import io.amoakoagyei.runtime.AccessorKind;
import io.amoakoagyei.runtime.AggregateIdMetadata;
import io.amoakoagyei.runtime.ElementModifier;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class AggregateIdLoaderTest {

    @Test
    void findIdOnAggregateUsingAggregateIdMetadata() {
        var aggregateId = UUID.randomUUID();
        var aggregate = new MarketPlaceAd();
        aggregate.on(new AdCreatedEvent(aggregateId, "First Title"));

        AssertionsForClassTypes.assertThat(aggregate.getTitle()).isEqualTo("First Title");
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
}
