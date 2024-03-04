package io.amoakoagyei;

import io.amoakoagyei.runtime.CommandHandlerIndexLoader;
import io.amoakoagyei.runtime.CommandHandlerMetadata;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class CommandGateway {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    private final AggregateStore aggregateStore;

    public CommandGateway(AggregateStore aggregateStore) {
        this.aggregateStore = aggregateStore;
    }

    public Result<Object> handle(Command command) {
        if (command == null) {
            return Result.failure(new IllegalArgumentException("Command cannot be null"));
        }
        var handlerDetails = CommandHandlerIndexLoader.findCommandHandler(command.getClass());
        return Result.of(handlerDetails)
                .flatMap(hd -> {
                    if (hd.isConstructor()) {
                        return handleConstructorAggregate(hd, command);
                    }
                    return handleNonConstructorAggregate(hd, command);
                });
    }

    private Result<Object> handleNonConstructorAggregate(CommandHandlerMetadata commandHandlerMetadata, Command command) {
        return AggregateIdLoader.extractAggregateId(commandHandlerMetadata, command)
                .flatMap(aggregateStore::load
                )
                .map(aggregate -> {
                    return applyCommandOnAggregate(commandHandlerMetadata, aggregate, command);
                })
                ;
    }

    private Result<Object> applyCommandOnAggregate(CommandHandlerMetadata handlerDetails,
                                                   Object aggregate,
                                                   Command command) {
        if (aggregate == null) {
            return Result.failure(new IllegalArgumentException("Aggregate cannot be null"));
        }

        try {
            var methodType = MethodType.methodType(void.class, command.getClass());
            var methodHandle = lookup.findVirtual(command.getClass(), handlerDetails.methodName(), methodType);
            methodHandle.invoke(aggregate, command);
            return Result.success(aggregate);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            return Result.failure(e);
        } catch (Throwable throwable) {
            return Result.failure(new RuntimeException(throwable));
        }
    }

    private Result<Object> handleConstructorAggregate(CommandHandlerMetadata handlerDetails, Command command) {
        try {
            var methodType = MethodType.methodType(void.class, command.getClass());
            var methodHandle = lookup.findConstructor(handlerDetails.aggregateType(), methodType);
            return Result.success(methodHandle.invokeWithArguments(command));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            return Result.failure(e);
        } catch (Throwable throwable) {
            return Result.failure(new RuntimeException(throwable));
        }
    }

//    Result<Object> findAggregateId(Object command) {
//        var aggregateIdDetails = findAggregateIdDetails(command);
//        var maybeId = aggregateIdDetails.map(details -> {
//            // use method handles to process command to retrieve the item
//            if (details.commandClassType() == RECORD || details.accessorType() == METHOD) {
//                var methodType = MethodType.methodType(details.aggregateIdType());
//                try {
//                    var methodHandle = lookup.findVirtual(command.getClass(), details.accessorName(), methodType);
//                    return Result.success(methodHandle.invoke(command));
//                } catch (Throwable e) {
//                    return Result.failure(new RuntimeException(e));
//                }
//            }
//            try {
//                Field declaredField = command.getClass().getDeclaredField(details.accessorName());
////                if(declaredField.)
//                // TODO - check if it is private
//                MethodHandles.Lookup privateLookupIn = MethodHandles.privateLookupIn(command.getClass(), lookup);
//                var varHandle = privateLookupIn.unreflectVarHandle(declaredField);
//                return Result.success(varHandle.get(command));
//            } catch (NoSuchFieldException | IllegalAccessException e) {
//                return Result.failure(new RuntimeException(e));
//            }
//        });
//
//        return maybeId.orElseGet(() -> Result.failure(new RuntimeException("not found")));
//    }
//
//    // find AggregateId on command
//    private Optional<CommandAggregateIdDetails> findAggregateIdDetails(Object command) {
//        if (command == null) {
//            return Optional.empty();
//        }
//
//        var enclosingClass = command.getClass();
//        if (enclosingClass.isRecord()) {
//            RecordComponent[] recordComponents = enclosingClass.getRecordComponents();
//            return Arrays.stream(recordComponents)
//                    .filter(it -> it.isAnnotationPresent(TargetAggregateId.class))
//                    .findFirst()
//                    .map(it -> new CommandAggregateIdDetails(RECORD, METHOD, it.getType(), it.getAccessor().getName()));
//        }
//        var fieldWithAnnotation = Arrays.stream(enclosingClass.getDeclaredFields())
//                .filter(it -> it.isAnnotationPresent(TargetAggregateId.class))
//                .findFirst();
//
//        return fieldWithAnnotation
//                .map(it -> new CommandAggregateIdDetails(CLASS, FIELD, it.getType(), it.getName()))
//                .or(() ->
//                        Arrays.stream(enclosingClass.getDeclaredMethods())
//                                .filter(it -> it.isAnnotationPresent(TargetAggregateId.class))
//                                .findFirst()
//                                .map(it -> new CommandAggregateIdDetails(CLASS, METHOD, it.getReturnType(), it.getName()))
//                );
//    }

}
