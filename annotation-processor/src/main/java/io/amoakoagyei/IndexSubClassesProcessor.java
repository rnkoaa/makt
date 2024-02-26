package io.amoakoagyei;

import com.google.auto.service.AutoService;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@AutoService(Processor.class)
public class IndexSubClassesProcessor extends AbstractAnnotationProcessor {

    private static final Set<String> indexedClasses = new HashSet<>();
    protected static final String INDEXED_SUB_CLASSES = "index-sub-classes.txt";

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        var indexedSubClasses = roundEnv.getElementsAnnotatedWith(IndexSubClasses.class)
                .stream()
                .filter(it -> (it.getKind() == ElementKind.CLASS)
                        && (!it.getModifiers().contains(Modifier.ABSTRACT)))
                .map(it -> (TypeElement) it)
                .map(it -> it.getQualifiedName().toString())
                .collect(Collectors.toSet());

        indexedClasses.addAll(indexedSubClasses);

        if (!roundEnv.processingOver()) {
            return false;
        }

        try {
            writeSimpleNameIndexFile(indexedClasses, ANNOTATED_INDEX_PREFIX + INDEXED_SUB_CLASSES);
        } catch (IOException e) {
            error("[ClassIndexProcessor] Can't write index file: " + e.getMessage());
        }

        return false;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(IndexSubClasses.class.getCanonicalName());
    }
}
