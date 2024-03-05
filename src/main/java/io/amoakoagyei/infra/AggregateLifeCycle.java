package io.amoakoagyei.infra;

import io.amoakoagyei.Result;
import io.amoakoagyei.runtime.EventSourcingIndexLoader;
import io.amoakoagyei.runtime.EventSourcingMetadata;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class AggregateLifeCycle {
    private static final MethodHandles.Lookup lookup = MethodHandles.lookup();
    private static final Map<Object, List<AggregateEvent>> aggregateEvents = new ConcurrentHashMap<>();

    public static Result<Object> applyEvent(Object aggregate, Object event) {
        if (aggregate == null) {
            return Result.failure(new RuntimeException("aggregate is required to be non-null"));
        }
        if (event == null) {
            return Result.success(aggregate);
        }

        return applyOnlyEvent(aggregate, event);
    }

    private static Result<Object> applyOnlyEvent(Object aggregate, Object event) {
        return Result.of(EventSourcingIndexLoader.findAggregateIdMetadata(event.getClass()))
                .flatMap(eventSourcingMetadata -> applyHandleEvent(aggregate, event, eventSourcingMetadata));
    }

    public static Result<Object> applyEvents(Object aggregate, List<Object> events) {
        if (aggregate == null) {
            return Result.failure(new RuntimeException("aggregate is required to be non-null"));
        }
        if (events == null || events.isEmpty()) {
            return Result.success(aggregate);
        }

        return Result.success(events.stream()
                .reduce(aggregate,
                        (agg, ev) -> {
                            var aggregateResult = applyOnlyEvent(agg, ev)
                                    .getOrNull();
                            return Objects.requireNonNullElse(aggregateResult, agg);
                        },
                        (agg, ev) -> agg
                ));
    }

    private static Result<Object> applyHandleEvent(Object aggregate, Object event, EventSourcingMetadata eventSourcingMetadata) {
        try {
            var methodType = MethodType.methodType(eventSourcingMetadata.methodReturnType(), event.getClass());
            var methodHandle = lookup.findVirtual(aggregate.getClass(), eventSourcingMetadata.accessorName(), methodType);
            methodHandle.invoke(aggregate, event);
            return Result.success(aggregate);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            return Result.failure(e);
        } catch (Throwable throwable) {
            return Result.failure(new RuntimeException(throwable));
        }
    }
}
