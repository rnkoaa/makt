package io.amoakoagyei;

import io.amoakoagyei.infra.AggregateEventStore;
import io.amoakoagyei.infra.AggregateLifeCycle;
import io.amoakoagyei.runtime.CommandHandlerIndexLoader;
import io.amoakoagyei.runtime.CommandHandlerMetadata;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;

public class CommandGateway {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    private final AggregateEventStore aggregateEventStore;

    public CommandGateway(AggregateEventStore aggregateEventStore) {
        this.aggregateEventStore = aggregateEventStore;
    }

    public Result<Object> send(Object command) {
        if (command == null) {
            return Result.failure(new IllegalArgumentException("Command cannot be null"));
        }
        var handlerDetails = CommandHandlerIndexLoader.findCommandHandler(command.getClass());
        return Result.of(handlerDetails)
                .flatMap(hd -> {
                    Result<Object> objectResult = AggregateIdLoader.extractAggregateId(hd, command);
                    if (hd.isConstructor()) {
                        return handleConstructorAggregate(hd, command)
                                .flatMap(aggregate -> objectResult.flatMap(aggregateEventStore::load));
                    }

                    return handleNonConstructorAggregate(hd, command)
                            .flatMap(aggregate -> objectResult.flatMap(aggregateEventStore::load));
                });
    }

    private Result<Object> handleNonConstructorAggregate(CommandHandlerMetadata commandHandlerMetadata, Object command) {
        record AggregateWithId(Object id, Object aggregate) {
        }
        return AggregateIdLoader.extractAggregateId(commandHandlerMetadata, command)
                .flatMap(aggregateId -> aggregateEventStore.load(aggregateId)
                        .map(aggregate -> new AggregateWithId(aggregateId, aggregate)))
                .flatMap(aggregateWithId -> applyCommandOnAggregate(commandHandlerMetadata, aggregateWithId.aggregate, command));
    }

    private Result<Object> applyCommandOnAggregate(CommandHandlerMetadata handlerDetails,
                                                   Object aggregate,
                                                   Object command) {
        if (aggregate == null) {
            return Result.failure(new IllegalArgumentException("Aggregate cannot be null"));
        }

        try {
            var methodType = MethodType.methodType(void.class, command.getClass());
            var methodHandle = lookup.findVirtual(handlerDetails.aggregateType(), handlerDetails.methodName(), methodType);
            methodHandle.invoke(aggregate, command);
            return Result.success(aggregate);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            return Result.failure(e);
        } catch (Throwable throwable) {
            return Result.failure(new RuntimeException(throwable));
        }
    }

    private Result<Object> handleConstructorAggregate(CommandHandlerMetadata handlerDetails, Object command) {
        try {
            var methodType = MethodType.methodType(void.class, command.getClass());
            var methodHandle = lookup.findConstructor(handlerDetails.aggregateType(), methodType);
            return Result.success(methodHandle.invokeWithArguments(command));

//            Constructor<?> declaredConstructor = handlerDetails.aggregateType().getDeclaredConstructor();
//            MethodHandles.Lookup privateLookupIn = MethodHandles.privateLookupIn(handlerDetails.aggregateType(), lookup);
//            var methodHandle = privateLookupIn.unreflectConstructor(declaredConstructor);
//            return Result.success(methodHandle.invokeWithArguments(command));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            return Result.failure(e);
        } catch (Throwable throwable) {
            return Result.failure(new RuntimeException(throwable));
        }
    }
}
