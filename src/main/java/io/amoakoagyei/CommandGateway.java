package io.amoakoagyei;

import io.amoakoagyei.infra.CommandAggregateIdDetails;
import io.amoakoagyei.runtime.CommandHandlerIndexLoader;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Optional;

import static io.amoakoagyei.infra.CommandClassType.CLASS;
import static io.amoakoagyei.infra.CommandClassType.RECORD;
import static io.amoakoagyei.infra.CommandExtractionType.FIELD;
import static io.amoakoagyei.infra.CommandExtractionType.METHOD;

public class CommandGateway {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    private final CommandHandlers commandHandlers;

    public CommandGateway(CommandHandlers commandHandlers) {
        this.commandHandlers = commandHandlers;
    }

    public Result<?> handle(Command command) {
        if (command == null) {
            return Result.failure(new IllegalArgumentException("Command cannot be null"));
        }
        var handlerDetails = CommandHandlerIndexLoader.findCommandHandler(command.getClass());

//        var result = Result.of(handlerDetails)
//                .map(value -> {
//                    // TODO handle getting details
//                });
        var res = commandHandlers.handle(command);

        if (res.isFailure()) {
            return res;
        }

        var aggregate = (Aggregate) res.getOrNull();
        var events = aggregate.getEvents();

        return res;
    }

    public void findHandlingAggregateClass(Command command) {
//        Iterable<Class<? extends Aggregate>> subclasses = ClassIndex.getSubclasses(Aggregate.class);
//        subclasses.spliterator().st
    }

    Result<?> findAggregateId(Object command) {
        var aggregateIdDetails = findAggregateIdDetails(command);
        var maybeId = aggregateIdDetails.map(details -> {
            // use method handles to process command to retrieve the item
            if (details.commandClassType() == RECORD || details.accessorType() == METHOD) {
                var methodType = MethodType.methodType(details.aggregateIdType());
                try {
                    var methodHandle = lookup.findVirtual(command.getClass(), details.accessorName(), methodType);
                    return Result.success(methodHandle.invoke(command));
                } catch (Throwable e) {
                    return Result.failure(new RuntimeException(e));
                }
            }
            try {
                Field declaredField = command.getClass().getDeclaredField(details.accessorName());
//                if(declaredField.)
                // TODO - check if it is private
                MethodHandles.Lookup privateLookupIn = MethodHandles.privateLookupIn(command.getClass(), lookup);
                var varHandle = privateLookupIn.unreflectVarHandle(declaredField);
                return Result.success(varHandle.get(command));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                return Result.failure(new RuntimeException(e));
            }
        });

        return maybeId.orElseGet(() -> Result.failure(new RuntimeException("not found")));
    }

    // find AggregateId on command
    private Optional<CommandAggregateIdDetails> findAggregateIdDetails(Object command) {
        if (command == null) {
            return Optional.empty();
        }

        var commandClass = command.getClass();
        if (commandClass.isRecord()) {
            RecordComponent[] recordComponents = commandClass.getRecordComponents();
            return Arrays.stream(recordComponents)
                    .filter(it -> it.isAnnotationPresent(TargetAggregateId.class))
                    .findFirst()
                    .map(it -> new CommandAggregateIdDetails(RECORD, METHOD, it.getType(), it.getAccessor().getName()));
        }
        var fieldWithAnnotation = Arrays.stream(commandClass.getDeclaredFields())
                .filter(it -> it.isAnnotationPresent(TargetAggregateId.class))
                .findFirst();

        return fieldWithAnnotation
                .map(it -> new CommandAggregateIdDetails(CLASS, FIELD, it.getType(), it.getName()))
                .or(() ->
                        Arrays.stream(commandClass.getDeclaredMethods())
                                .filter(it -> it.isAnnotationPresent(TargetAggregateId.class))
                                .findFirst()
                                .map(it -> new CommandAggregateIdDetails(CLASS, METHOD, it.getReturnType(), it.getName()))
                );
    }

}
