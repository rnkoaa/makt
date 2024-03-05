package io.amoakoagyei;

import com.google.auto.service.AutoService;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@AutoService(Processor.class)
public class CommandHandlerProcessor extends AbstractAnnotationProcessor {
    private static final Set<CommandHandlerProperties> indexedClasses = new HashSet<>();
    private static final Set<AggregateIdDetails> aggregateIdDetails = new HashSet<>();
    public static final String COMMAND_HANDLER_INDEX = "command-handler-index.txt";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        var commandHandlers = roundEnv.getElementsAnnotatedWith(CommandHandler.class)
                .stream()
                .filter(it -> (it.getKind() == ElementKind.METHOD || it.getKind() == ElementKind.CONSTRUCTOR))
                .map(it -> (ExecutableElement) it)
                .filter(it -> !it.getParameters().isEmpty())
                .collect(Collectors.toSet());

        var eventSourcingHandlers = roundEnv.getElementsAnnotatedWith(EventSourcingHandler.class)
                .stream()
                .filter(it -> (it.getKind() == ElementKind.METHOD || it.getKind() == ElementKind.CONSTRUCTOR))
                .map(it -> (ExecutableElement) it)
                .filter(it -> !it.getParameters().isEmpty())
                .collect(Collectors.toSet());

        Set<ExecutableElement> attributeHandlers = new HashSet<>(commandHandlers);
        attributeHandlers.addAll(eventSourcingHandlers);


        var targetAggregateIdElements = roundEnv.getElementsAnnotatedWith(TargetAggregateId.class)
                .stream()
                .filter(it -> (
                        it.getKind() == ElementKind.FIELD ||
                                it.getKind() == ElementKind.METHOD) ||
                        it.getKind() == ElementKind.RECORD_COMPONENT
                )
                .collect(Collectors.toSet());

        Set<String> intersection = validateCommands(attributeHandlers, targetAggregateIdElements);
        if (!intersection.isEmpty()) {
            intersection.forEach(cmd ->
                    error(("Class %s is used as a CommandHandler Parameter but does not have a " +
                            "TargetAggregateIdentifier annotated field, method or record component").formatted(cmd))
            );

            return false;
        }

        targetAggregateIdElements.stream()
                .map(this::transformCommandElement)
                .filter(Objects::nonNull)
                .forEach(aggregateIdDetails::add);

        var handlerProperties = attributeHandlers.stream()
                .filter(it -> !it.getParameters().isEmpty())
                .map(this::transform)
                .collect(Collectors.toSet());

        indexedClasses.addAll(handlerProperties);
        if (!roundEnv.processingOver()) {
            return false;
        }

        Map<String, AggregateIdDetails> aggregateIdDetailGroup = aggregateIdDetails
                .stream()
                .filter(AggregateIdDetails::isValid)
                .collect(Collectors.toMap(
                        AggregateIdDetails::commandClassName,
                        it -> it
                ));

        try {
            var indexedAggregateIdElements = enrich(indexedClasses, aggregateIdDetailGroup)
                    .stream()
                    .map(CommandHandlerProperties::toString)
                    .collect(Collectors.toSet());
            writeSimpleNameIndexFile(indexedAggregateIdElements, ANNOTATED_INDEX_PREFIX + COMMAND_HANDLER_INDEX);
        } catch (IOException e) {
            error("[ClassIndexProcessor] Can't write index file: " + e.getMessage());
        }

        return false;
    }

    private Set<CommandHandlerProperties> enrich(Set<CommandHandlerProperties> indexedClasses, Map<String, AggregateIdDetails> aggregateIdDetailGroup) {
        return indexedClasses.stream()
                .map(it -> {
                    var builder = it.toBuilder();
                    var aggregateIdDetails = aggregateIdDetailGroup.get(it.aggregateAttributeType());
                    if (aggregateIdDetails != null) {
                        builder
                                .commandElementType(aggregateIdDetails.isRecord() ? ElementKind.RECORD : ElementKind.CLASS)
                                .aggregateIdAccessorName(aggregateIdDetails.accessorName())
                                .aggregateIdType(aggregateIdDetails.accessorType())
                                .aggregateIdModifiers(aggregateIdDetails.modifiers())
                                .aggregateIdAccessorKind(aggregateIdDetails.accessorKind())
                                .build();
                    }
                    return builder.build();
                })
                .collect(Collectors.toSet());
    }

    private AggregateIdDetails transformCommandElement(Element t) {
        TypeElement enclosingElement = (TypeElement) typeUtils.asElement(t.getEnclosingElement().asType());
        if (enclosingElement.getKind() == ElementKind.RECORD && t.getKind() == ElementKind.RECORD_COMPONENT) {
            return transformRecordComponentElement(t, enclosingElement);
        } else if (enclosingElement.getKind() != ElementKind.RECORD) {
            // for constructor Records, fields are implicitly available, so we need to ignore those
            var builder = new AggregateIdDetails.AggregateIdDetailsBuilder()
                    .accessorKind(t.getKind())
                    .modifiers(t.getModifiers())
                    .accessorName(t.getSimpleName().toString())
                    .commandClassName(enclosingElement.getQualifiedName().toString());

            switch (t.getKind()) {
                case METHOD -> {
                    var exec = (ExecutableElement) t;
                    var typeElement = (TypeElement) typeUtils.asElement(exec.getReturnType());
                    builder.isRecord(false)
                            .accessorType(typeElement.getQualifiedName().toString());
                }
                case FIELD -> {
                    var typeElement = (TypeElement) typeUtils.asElement(t.asType());
                    builder.isRecord(false)
                            .accessorType(typeElement.getQualifiedName().toString());
                }
                default -> note("unhandled");
            }
            return builder.build();
        }
        return null;
    }

    private AggregateIdDetails transformRecordComponentElement(Element t, TypeElement enclosingElement) {
        var builder = new AggregateIdDetails.AggregateIdDetailsBuilder()
                .accessorKind(t.getKind())
                .modifiers(t.getModifiers())
                .accessorName(t.getSimpleName().toString())
                .commandClassName(enclosingElement.getQualifiedName().toString());

        var recordComponent = (RecordComponentElement) t;
        ExecutableElement accessor = recordComponent.getAccessor();
        var returnType = (TypeElement) typeUtils.asElement(accessor.getReturnType());
        builder.isRecord(true)
                .accessorType(returnType.getQualifiedName().toString());

        return builder.build();
    }

    private Set<String> validateCommands(Set<ExecutableElement> commandHandlers, Set<? extends Element> targetAggregateIdElements) {
        var targetAggregateIdEnclosingElement = targetAggregateIdElements.stream()
                .map(Element::getEnclosingElement)
                .map(it -> (TypeElement) it)
                .map(it -> it.getQualifiedName().toString())
                .collect(Collectors.toSet());

        var commandHandlerNames = commandHandlers.stream()
                .map(it -> TypeElements.getParamType(typeUtils, it))
                .filter(Objects::nonNull)
                .map(it -> it.getQualifiedName().toString())
                .collect(Collectors.toSet());
        return disjunction(commandHandlerNames, targetAggregateIdEnclosingElement);
    }

    private CommandHandlerProperties transform(ExecutableElement it) {
        TypeElement paramTypeElement = TypeElements.getParamType(typeUtils, it);
        if (paramTypeElement == null) {
            return null;
        }

        String returnTypeName = "void.class";
        if (it.getKind() != ElementKind.CONSTRUCTOR) {
            var returnTypeElement = (TypeElement) typeUtils.asElement(it.getReturnType());
            if (returnTypeElement != null) {
                returnTypeName = returnTypeElement.getQualifiedName().toString();
            }
        }

        String name = it.getSimpleName().toString();
        TypeElement enclosingElement = (TypeElement) it.getEnclosingElement();

        return CommandHandlerProperties.builder()
                .aggregateAttributeType(paramTypeElement.getQualifiedName().toString())
                .aggregateType(enclosingElement.getQualifiedName().toString())
                .handlerName(name)
                .methodReturnType(returnTypeName)
                .build();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(
                CommandHandler.class.getCanonicalName(),
                EventSourcingHandler.class.getCanonicalName()
        );
    }

    private static Set<String> disjunction(final Set<String> first, final Set<String> second) {
        final Set<String> copy = new HashSet<>(first);
        copy.removeAll(second);
        return copy;
    }
}
