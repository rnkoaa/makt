package io.amoakoagyei;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class CommandHandlers {

    private static final Map<Class<?>, Function<Object, Result<?>>> handlers = new HashMap<>();

    void register(Class<?> commandClass, Function<Object, Result<?>> function) {
        handlers.put(commandClass, function);
    }

    public Result<?> handle(Command command) {
        Class<?> aClass = command.getClass();

        Function<Object, Result<?>> function = handlers.get(aClass);
        if (function != null) {
            return function.apply(command);
        }

        return Result.failure(new IllegalArgumentException("unable to process command"));

    }
}
