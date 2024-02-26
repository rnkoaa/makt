package io.amoakoagyei;

import com.google.auto.service.AutoService;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@AutoService(Processor.class)
public class CommandHandlerProcessor extends AbstractAnnotationProcessor {
    private static final Set<String> indexedClasses = new HashSet<>();
    static final String COMMAND_HANDLER_INDEX = "command-handler-index.txt";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        var commandHandlers = roundEnv.getElementsAnnotatedWith(CommandHandler.class)
                .stream()
                .filter(it -> (it.getKind() == ElementKind.METHOD || it.getKind() == ElementKind.CONSTRUCTOR))
                .map(it -> (ExecutableElement) it)
                .filter(it -> !it.getParameters().isEmpty())
                .map(this::transform)
                .map(CommandHandlerProperties::toString)
                .collect(Collectors.toSet());

var         roundEnv.getElementsAnnotatedWith(TargetAggregateId.class)

        indexedClasses.addAll(commandHandlers);

//        commandHandlers.stream()
//                .filter(it -> it.getKind() != ElementKind.CONSTRUCTOR)
//                .filter(it -> !it.getParameters().isEmpty())
//                .forEach(it -> {
//                    VariableElement first = it.getParameters().getFirst();
//                    TypeElement paramTypeElement = (TypeElement) typeUtils.asElement(first.asType());
//
//                    var isRecord = paramTypeElement.getKind() == ElementKind.RECORD;
//                    note(paramTypeElement.getQualifiedName().toString() + " -> Type: IsRecord: " + isRecord);
//                });
//                .filter(it -> {
////                    paramTypeElement.getKind().is
//
//                })

        if (!roundEnv.processingOver()) {
            return false;
        }

        try {
            writeSimpleNameIndexFile(indexedClasses, ANNOTATED_INDEX_PREFIX + COMMAND_HANDLER_INDEX);
        } catch (IOException e) {
            error("[ClassIndexProcessor] Can't write index file: " + e.getMessage());
        }

        return false;
    }

    private CommandHandlerProperties transform(ExecutableElement it) {
        VariableElement firstParameter = it.getParameters().getFirst();
        TypeMirror typeParameterMirror = firstParameter.asType();
        TypeElement paramTypeElement = (TypeElement) typeUtils.asElement(typeParameterMirror);
        String name = it.getSimpleName().toString();
        TypeElement enclosingElement = (TypeElement) it.getEnclosingElement();
        return new CommandHandlerProperties(
                paramTypeElement.getQualifiedName().toString(),
                enclosingElement.getQualifiedName().toString(),
                name
        );
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(
                CommandHandler.class.getCanonicalName(),
                TargetAggregateId.class.getCanonicalName()
        );
    }
}
