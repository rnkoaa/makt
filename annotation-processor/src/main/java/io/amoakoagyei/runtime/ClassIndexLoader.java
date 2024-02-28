package io.amoakoagyei.runtime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.amoakoagyei.AbstractAnnotationProcessor.ANNOTATED_INDEX_PREFIX;
import static io.amoakoagyei.IndexSubClassesProcessor.INDEXED_SUB_CLASSES;

public class ClassIndexLoader {

    private static final List<String> subClassIndex = new ArrayList<>();
    private static final Map<String, Class<?>> classes = new HashMap<>();

    static {
        String path = ANNOTATED_INDEX_PREFIX + INDEXED_SUB_CLASSES;
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();

        // keeping a copy of the index in memory for further manipulation
        subClassIndex.addAll(loadResourceFile(classloader, path));

        subClassIndex.stream()
                .map(ClassIndexLoader::loadClass)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(it -> classes.put(it.getCanonicalName(), it));
    }

    static List<String> loadResourceFile(ClassLoader classLoader, String path) {
        List<String> lines = new ArrayList<>();
        try (InputStream is = classLoader.getResourceAsStream(path)) {
            if (is != null) {
                var bufferedReader = new BufferedReader(new InputStreamReader(is));
                for (String line; (line = bufferedReader.readLine()) != null; ) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            return Collections.emptyList();
        }
        return lines;
    }

    static Optional<Class<?>> loadClass(String canonicalName) {
        try {
            return Optional.of(Class.forName(canonicalName));
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    public static <T> List<Class<?>> getSubClasses(Class<T> tClass) {
        return classes.values().stream()
                .filter(it -> it.getSuperclass() == tClass)
                .toList();
    }
}
