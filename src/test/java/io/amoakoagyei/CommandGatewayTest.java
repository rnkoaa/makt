package io.amoakoagyei;

import io.amoakoagyei.marketplace.ApproveAdCommand;
import io.amoakoagyei.marketplace.CreateAdCommand;
import io.amoakoagyei.marketplace.MarketPlaceAd;
import io.amoakoagyei.marketplace.PublishAdCommand;
import io.amoakoagyei.marketplace.UpdateTitleCommand;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class CommandGatewayTest {

    private final CommandHandlers commandHandlers = new CommandHandlers();
    private final CommandGateway commandGateway = new CommandGateway(commandHandlers);

    @Test
    void findAggregateIdOnCommandWithoutId() {
        var createCommand = new CreateAdCommand("title");
        Result<?> aggregateId = commandGateway.findAggregateId(createCommand);
        assertThat(aggregateId.isFailure()).isTrue();
        assertThat(aggregateId.exceptionOrNull()).isNotNull().isInstanceOf(RuntimeException.class);
    }

    @Test
    void findAggregateIdOnCommandForRecordTypeWithAnnotation() {
        var updateCommand = new UpdateTitleCommand(UUID.randomUUID(), "title");
        Result<?> aggregateIdResult = commandGateway.findAggregateId(updateCommand);
        assertThat(aggregateIdResult.isSuccess()).isTrue();

        var aggregateId = (UUID) aggregateIdResult.getOrNull();
        assertThat(aggregateId).isNotNull().isEqualTo(updateCommand.aggregateId());
    }

    @Test
    void findAggregateIdOnCommandForClassTypeWithAnnotation() {
        var updateCommand = new ApproveAdCommand(UUID.randomUUID(), "unknown");
        Result<?> aggregateIdResult = commandGateway.findAggregateId(updateCommand);
        assertThat(aggregateIdResult.isSuccess()).isTrue();

        var aggregateId = (UUID) aggregateIdResult.getOrNull();
        assertThat(aggregateId).isNotNull().isEqualTo(updateCommand.getId());
    }

    @Test
    void findAggregateIdOnCommandForClassMethodWithAnnotation() {
        var updateCommand = new PublishAdCommand(UUID.randomUUID());
        Result<?> aggregateIdResult = commandGateway.findAggregateId(updateCommand);

        var aggregateId = (UUID) aggregateIdResult.getOrNull();
        assertThat(aggregateId).isNotNull().isEqualTo(updateCommand.getId());
    }

    @Test
    void findAggregateClasses() {
        List<Class<?>> subClasses = ClassIndexLoader.getSubClasses(Aggregate.class);
        assertThat(subClasses).hasSize(1);
    }

    @Test
    void ableToFindCommandHandlerDetailsForConstructor() {
        Optional<CommandHandlerDetails> commandHandler = CommandHandlerIndexLoader.findCommandHandler(CreateAdCommand.class);
        assertThat(commandHandler).isNotEmpty().hasValueSatisfying(commandHandlerDetail -> {
            assertThat(commandHandlerDetail.aggregateType()).isNotNull().isEqualTo(MarketPlaceAd.class);
            assertThat(commandHandlerDetail.commandType()).isNotNull().isEqualTo(CreateAdCommand.class);
            assertThat(commandHandlerDetail.isConstructor()).isTrue();
        });
    }
    @Test
    void ableToFindCommandHandlerDetailsForNonConstructor() {
        Optional<CommandHandlerDetails> commandHandler = CommandHandlerIndexLoader.findCommandHandler(UpdateTitleCommand.class);
        assertThat(commandHandler).isNotEmpty().hasValueSatisfying(commandHandlerDetail -> {
            assertThat(commandHandlerDetail.aggregateType()).isNotNull().isEqualTo(MarketPlaceAd.class);
            assertThat(commandHandlerDetail.commandType()).isNotNull().isEqualTo(UpdateTitleCommand.class);
            assertThat(commandHandlerDetail.isConstructor()).isFalse();
        });
    }
}