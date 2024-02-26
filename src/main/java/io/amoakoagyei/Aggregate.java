package io.amoakoagyei;

import java.util.ArrayList;
import java.util.List;

@IndexSubClasses
public abstract class Aggregate {
    private final List<Event> events;

    public Aggregate() {
        this.events = new ArrayList<>();
    }

    public List<Event> getEvents() {
        return events;
    }

    public void apply(Event event) {
        this.events.add(event);
    }
}
