package io.amoakoagyei.infra;

public record CommandAggregateIdDetails(
        CommandClassType commandClassType,
        CommandExtractionType accessorType,
        Class<?> aggregateIdType,
        String accessorName
) {
}