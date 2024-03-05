package io.amoakoagyei;

import java.util.ArrayList;
import java.util.List;

@IndexSubClasses
public abstract class Aggregate {
    private final List<Object> events;

    public Aggregate() {
        this.events = new ArrayList<>();
    }

    public List<Object> getEvents() {
        return events;
    }

    public void apply(Object event) {
        this.events.add(event);
    }
}
