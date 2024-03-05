package io.amoakoagyei;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

record EventSourcingElementMetadata(
        String eventClassName, // class of element
        String enclosingAggregateName,
        String executionElementName,
        String executionReturnType,
        Set<Modifier> modifiers

) {

    @Override
    public String toString() {
        String mfs = modifiers == null ? "void" : modifiers
                .stream()
                .map(Enum::name)
                .collect(Collectors.joining(";"));

        return "%s,%s,%s,%s,%s".formatted(
                eventClassName,
                enclosingAggregateName,
                executionElementName,
                executionReturnType == null ? "NULL" : executionReturnType,
                mfs
        );
    }
}

//@AutoService(Processor.class)
public class EventSourcingHandlerProcessor extends AbstractAnnotationProcessor {
    private static final Set<EventSourcingElementMetadata> eventSourcingElementMetadata = new HashSet<>();
    public static final String EVENT_SOURCE_HANDLER_INDEX = "event-source-handler-index.txt";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        var eventSourcedHandlers = roundEnv.getElementsAnnotatedWith(EventSourcingHandler.class)
                .stream()
                .filter(it -> (it.getKind() == ElementKind.METHOD || it.getKind() == ElementKind.CONSTRUCTOR))
                .map(it -> (ExecutableElement) it)
                .filter(it -> !it.getParameters().isEmpty())
                .map(this::transform)
                .collect(Collectors.toSet());

        eventSourcingElementMetadata.addAll(eventSourcedHandlers);
        if (!roundEnv.processingOver()) {
            return false;
        }

        try {
            var eventSourcedHandlerStrings = eventSourcingElementMetadata
                    .stream()
                    .map(EventSourcingElementMetadata::toString)
                    .collect(Collectors.toSet());
            writeSimpleNameIndexFile(eventSourcedHandlerStrings, ANNOTATED_INDEX_PREFIX + EVENT_SOURCE_HANDLER_INDEX);
        } catch (IOException e) {
            error("[ClassIndexProcessor] Can't write index file: " + e.getMessage());
        }
        return false;
    }

    EventSourcingElementMetadata transform(ExecutableElement executableElement) {
        TypeElement paramTypeElement = TypeElements.getParamType(typeUtils, executableElement);
        if (paramTypeElement == null) {
            return null;
        }
        String name = executableElement.getSimpleName().toString();
        TypeElement enclosingElement = (TypeElement) executableElement.getEnclosingElement();

        TypeMirror returnType = executableElement.getReturnType();
        TypeElement element = (TypeElement) typeUtils.asElement(returnType);

        return new EventSourcingElementMetadata(
                paramTypeElement.getQualifiedName().toString(),
                enclosingElement.getQualifiedName().toString(),
                name,
                (element == null) ? "NULL" : element.getQualifiedName().toString(),
                executableElement.getModifiers()
        );
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(
                EventSourcingHandler.class.getCanonicalName()
        );
    }
}
