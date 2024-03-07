package io.amoakoagyei.infra;

import io.amoakoagyei.Command;
import io.amoakoagyei.Result;
import io.amoakoagyei.runtime.CommandHandlerIndexLoader;
import io.amoakoagyei.runtime.CommandHandlerMetadata;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AggregateEventStore {
    private static final MethodHandles.Lookup lookup = MethodHandles.lookup();
    private final Map<Object, List<AggregateEvent>> aggregateEvents = new ConcurrentHashMap<>();
    private static AggregateEventStore aggregateEventStore = null;

    private AggregateEventStore() {
    }

    public static AggregateEventStore getInstance() {
        if (aggregateEventStore == null) {
            aggregateEventStore = new AggregateEventStore();
        }
        return aggregateEventStore;
    }

    public void store(AggregateEvent event) {
        List<AggregateEvent> events = aggregateEvents.get(event.aggregateId());
        AggregateEvent versionedEvent;
        if (events == null) {
            versionedEvent = event.withVersion(0);
            events = new ArrayList<>();
        } else {
            versionedEvent = event.withVersion(events.size());
        }
        events.add(versionedEvent);
        aggregateEvents.put(event.aggregateId(), events);
    }

    public List<AggregateEvent> find(Object aggregateId) {
        return aggregateEvents.computeIfAbsent(aggregateId, k -> new ArrayList<>());
    }

    public Result<Object> load(Object aggregateId) {
        List<AggregateEvent> foundAggregates = find(aggregateId);
        if (foundAggregates.isEmpty()) {
            return Result.failure(new RuntimeException("Aggregate with id " + aggregateId + " not found"));
        }

        var aggregateObject = createEmptyObject(foundAggregates.getFirst().aggregateType());
        return aggregateObject.flatMap(it -> {
            List<Object> aggregateEventItems = foundAggregates
                    .stream()
                    .sorted(Comparator.comparing(AggregateEvent::version))
                    .map(AggregateEvent::event)
                    .toList();
            return applyEvents(it, aggregateEventItems);
        });
    }

    public void clear() {
        aggregateEvents.clear();
    }

    public int count() {
        return aggregateEvents.size();
    }

    private static Result<Object> applyEvents(Object aggregate, List<Object> events) {
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

    private static Result<Object> applyHandleEvent(Object aggregate, Object event, CommandHandlerMetadata eventSourcingMetadata) {
        try {
            var methodType = MethodType.methodType(eventSourcingMetadata.methodReturnType(), event.getClass());
            var methodHandle = lookup.findVirtual(aggregate.getClass(), eventSourcingMetadata.methodName(), methodType);
            methodHandle.invoke(aggregate, event);
            return Result.success(aggregate);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            return Result.failure(e);
        } catch (Throwable throwable) {
            return Result.failure(new RuntimeException(throwable));
        }
    }

    private static Result<Object> applyOnlyEvent(Object aggregate, Object event) {
        return Result.of(CommandHandlerIndexLoader.findCommandHandler(event.getClass()))
                .flatMap(eventSourcingMetadata -> applyHandleEvent(aggregate, event, eventSourcingMetadata));
    }

    private Result<Object> createEmptyObject(Class<?> aggregateClass) {
        try {
            var methodType = MethodType.methodType(void.class); // find default constructor
            var methodHandle = lookup.findConstructor(aggregateClass, methodType);
            return Result.success(methodHandle.invoke());
        } catch (NoSuchMethodException | IllegalAccessException e) {
            return Result.failure(e);
        } catch (Throwable throwable) {
            return Result.failure(new RuntimeException(throwable));
        }
    }
}
