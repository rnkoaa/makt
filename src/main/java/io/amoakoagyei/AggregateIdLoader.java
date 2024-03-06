package io.amoakoagyei;

import io.amoakoagyei.runtime.AccessorKind;
import io.amoakoagyei.runtime.AggregateIdMetadata;
import io.amoakoagyei.runtime.CommandHandlerIndexLoader;
import io.amoakoagyei.runtime.CommandHandlerMetadata;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.util.Objects;

public class AggregateIdLoader {

    private static final MethodHandles.Lookup lookup = MethodHandles.lookup();

    public static Result<AggregateIdOptions> extractAggregateId(Object command) {
        var commandHandler = CommandHandlerIndexLoader.findCommandHandler(command.getClass());
        return Result.of(commandHandler)
                .filter(it -> it.aggregateIdMetadata() != null)
                .flatMap(it -> extractAggregateId(it, command)
                        .map(id -> {
                            var aggregateIdClass = it.aggregateIdMetadata().aggregateIdClass();
                            return new AggregateIdOptions(id, aggregateIdClass, it.aggregateType());
                        }));
    }

    public static Result<Object> extractAggregateId(AggregateIdMetadata aggregateIdMetadata, Object aggregate) {
        if (Objects.equals(aggregateIdMetadata.accessorName(), "NULL")) {
            return Result.failure(new RuntimeException("id does not exist"));
        }
        if (aggregateIdMetadata.isRecordCommand() || aggregateIdMetadata.accessorType() == AccessorKind.METHOD) {
            return extractMethodBasedAttribute(aggregate, aggregateIdMetadata);
        }

        return extractFieldBasedAttribute(aggregate, aggregateIdMetadata);
    }

    public static Result<Object> extractAggregateId(CommandHandlerMetadata it, Object command) {
        var aggregateIdMetadata = it.aggregateIdMetadata();
        return extractAggregateId(aggregateIdMetadata, command);
    }

    private static Result<Object> extractFieldBasedAttribute(Object command, AggregateIdMetadata aggregateIdMetadata) {
        try {
            var varHandle = getAccessorVarHandle(command.getClass(), aggregateIdMetadata);
            return Result.success(varHandle.get(command));

            // this is public so we can use normal
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return Result.failure(new RuntimeException(e));
        }
    }

    private static VarHandle getAccessorVarHandle(Class<?> commandClass, AggregateIdMetadata aggregateIdMetadata) throws IllegalAccessException, NoSuchFieldException {
        var declaredField = commandClass.getDeclaredField(aggregateIdMetadata.accessorName());
        if (aggregateIdMetadata.isPublicAccessor()) {
            return lookup.unreflectVarHandle(declaredField);
        }
        MethodHandles.Lookup privateLookupIn = MethodHandles.privateLookupIn(commandClass, lookup);
        return privateLookupIn.unreflectVarHandle(declaredField);
    }

    private static Result<Object> extractMethodBasedAttribute(Object command, AggregateIdMetadata aggregateIdMetadata) {
        var methodType = MethodType.methodType(aggregateIdMetadata.aggregateIdClass());
        try {
            var methodHandle = lookup.findVirtual(command.getClass(), aggregateIdMetadata.accessorName(), methodType);
            return Result.success(methodHandle.invoke(command));
        } catch (Throwable e) {
            return Result.failure(new RuntimeException(e));
        }
    }

    public record AggregateIdOptions(Object id, Class<?> idClass, Class<?> aggregateType) {
    }
}


