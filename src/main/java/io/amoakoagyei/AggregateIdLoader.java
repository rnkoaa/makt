package io.amoakoagyei;

import io.amoakoagyei.runtime.AccessorKind;
import io.amoakoagyei.runtime.CommandHandlerIndexLoader;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Optional;

public class AggregateIdLoader {

    private static final MethodHandles.Lookup lookup = MethodHandles.lookup();

    static Optional<Object> load(Command command) {
        var commandHandler = CommandHandlerIndexLoader.findCommandHandler(command.getClass());
        commandHandler
                .filter(it -> it.aggregateIdMetadata() != null)
                .flatMap(commandHandlerMetadata -> {
                    var aggregateIdMetadata = commandHandlerMetadata.aggregateIdMetadata();
                    if (aggregateIdMetadata.isRecordCommand() || aggregateIdMetadata.accessorType() == AccessorKind.METHOD) {
                        var methodType = MethodType.methodType(aggregateIdMetadata.aggregateIdClass());
                        try {
                            var methodHandle = lookup.findVirtual(command.getClass(), aggregateIdMetadata.accessorName(), methodType);
                            return Result.success(methodHandle.invoke(command));
                        } catch (Throwable e) {
                            return Result.failure(new RuntimeException(e));
                        }
                    }
                })

    }

//    Result<?> findAggregateId(Object command) {
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
}
