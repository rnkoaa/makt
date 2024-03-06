package io.amoakoagyei;

import io.amoakoagyei.infra.AggregateEventStore;
import io.amoakoagyei.marketplace.CreateAdCommand;
import io.amoakoagyei.marketplace.MarketPlaceAd;
import io.amoakoagyei.marketplace.UpdateTitleCommand;
import io.amoakoagyei.runtime.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class CommandGatewayTest {

    private final CommandGateway commandGateway = new CommandGateway();
    private final AggregateEventStore aggregateEventStore = AggregateEventStore.getInstance();

    @AfterEach
    void tearDown() {
        aggregateEventStore.clear();
    }

    @Test
    void constructorCanBeInvokedForCommand() {
        var createCommand = new CreateAdCommand(UUID.randomUUID(), "created a new ad");
        var result = commandGateway.handle(createCommand);
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getOrNull()).isNotNull().satisfies(s -> {
            assertThat(s).isNotNull();
            assertThat(s.getClass()).isEqualTo(MarketPlaceAd.class);
            var marketPlace = (MarketPlaceAd) s;
            assertThat(aggregateEventStore.count()).isEqualTo(1);
//            assertThat(marketPlace.getEvents()).isNotEmpty().hasSize(1);
        });
    }

    @Test
    void ableToFindCommandHandlerDetailsForConstructor() {
        Optional<CommandHandlerMetadata> commandHandler = CommandHandlerIndexLoader.findCommandHandler(CreateAdCommand.class);
        assertThat(commandHandler).isNotEmpty().hasValueSatisfying(commandHandlerDetail -> {
            assertThat(commandHandlerDetail.aggregateType()).isNotNull().isEqualTo(MarketPlaceAd.class);
            assertThat(commandHandlerDetail.aggregateAttributeClass()).isNotNull().isEqualTo(CreateAdCommand.class);
            assertThat(commandHandlerDetail.methodName()).isEqualTo("<init>");
            assertThat(commandHandlerDetail.isConstructor()).isTrue();
            AggregateIdMetadata aggregateIdMetadata = commandHandlerDetail.aggregateIdMetadata();
            assertThat(aggregateIdMetadata).isNotNull();
            assertThat(aggregateIdMetadata.isValid()).isTrue();
        });
    }

    @Test
    void ableToFindCommandHandlerDetailsForNonConstructor() {
        Optional<CommandHandlerMetadata> commandHandler = CommandHandlerIndexLoader.findCommandHandler(UpdateTitleCommand.class);
        assertThat(commandHandler).isNotEmpty().hasValueSatisfying(commandHandlerDetail -> {
            assertThat(commandHandlerDetail.aggregateType()).isNotNull().isEqualTo(MarketPlaceAd.class);
            assertThat(commandHandlerDetail.aggregateAttributeClass()).isNotNull().isEqualTo(UpdateTitleCommand.class);
            assertThat(commandHandlerDetail.methodName()).isEqualTo("handle");
            assertThat(commandHandlerDetail.isConstructor()).isFalse();
            AggregateIdMetadata aggregateIdMetadata = commandHandlerDetail.aggregateIdMetadata();
            assertThat(aggregateIdMetadata).isNotNull();
            assertThat(aggregateIdMetadata.isValid()).isTrue();
        });
    }

    @Test
    void ableToFindAggregateIdForCommand() {
        Optional<AggregateIdMetadata> aggregateMetadata = CommandHandlerIndexLoader.findAggregateIdMetadata(UpdateTitleCommand.class);
        assertThat(aggregateMetadata).isNotEmpty().hasValueSatisfying(aggregateIdMetadataValue -> {
            assertThat(aggregateIdMetadataValue.enclosingClass()).isNotNull().isEqualTo(UpdateTitleCommand.class);
            assertThat(aggregateIdMetadataValue.aggregateIdClass()).isEqualTo(UUID.class);
            assertThat(aggregateIdMetadataValue.accessorName()).isEqualTo("aggregateId");
            assertThat(aggregateIdMetadataValue.accessorType()).isEqualTo(AccessorKind.RECORD_COMPONENT);
            assertThat(aggregateIdMetadataValue.commandElementKind()).isEqualTo(AccessorKind.RECORD);
            assertThat(aggregateIdMetadataValue.modifiers()).isNotEmpty().contains(ElementModifier.PUBLIC);
        });

    }
}