package io.amoakoagyei;

import io.amoakoagyei.marketplace.MarketPlaceAd;
import io.amoakoagyei.runtime.AccessorKind;
import io.amoakoagyei.runtime.AggregateIdMetadata;
import io.amoakoagyei.runtime.AggregateIdentifierIndexLoader;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class AggregateIdentifierIndexLoaderTest {

    @Test
    void findKnownAggregateIdMetadata() {
        Optional<AggregateIdMetadata> aggregateIdMetadata = AggregateIdentifierIndexLoader.findAggregateIdMetadata(MarketPlaceAd.class);
        assertThat(aggregateIdMetadata).isPresent()
                .hasValueSatisfying(metadata -> {
                    assertThat(metadata.accessorName()).isEqualTo("id");
                    assertThat(metadata.aggregateIdClass()).isEqualTo(UUID.class);
                    assertThat(metadata.enclosingClass()).isEqualTo(MarketPlaceAd.class);
                    assertThat(metadata.accessorType()).isEqualTo(AccessorKind.FIELD);
                });
    }
}