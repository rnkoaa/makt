package io.amoakoagyei;

import io.amoakoagyei.marketplace.CreateAdCommand;
import io.amoakoagyei.marketplace.MarketPlaceAd;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class AggregateStoreTest {

    @Test
    void validateAggregateStore() {
        var aggregateStore = new AggregateStore();

        var marketAd = new MarketPlaceAd(new CreateAdCommand(UUID.randomUUID(), "created Ad"));

        aggregateStore.save(marketAd.getId(), marketAd);

        assertThat(aggregateStore.size()).isEqualTo(1);
        assertThat(aggregateStore.contains(marketAd.getId())).isTrue();

        Result<Aggregate> loadResult = aggregateStore.load(marketAd.getId());
        assertThat(loadResult.isSuccess()).isTrue();
        assertThat(loadResult.getOrNull()).isNotNull().satisfies(s -> {
            assertThat(s).isNotNull();
            assertThat(s).isInstanceOf(MarketPlaceAd.class);
            var m = (MarketPlaceAd) s;
            assertThat(m.getTitle()).isEqualTo(marketAd.getTitle());
        });
        aggregateStore.clear();
    }

}