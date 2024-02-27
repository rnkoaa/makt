package io.amoakoagyei;

import com.google.auto.service.AutoService;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AutoService(Processor.class)
public class CommandHandlerProcessor extends AbstractAnnotationProcessor {
    private static final Set<CommandHandlerProperties> indexedClasses = new HashSet<>();
    private static final Set<HandleAggregateIdDetails> aggregateIdDetails = new HashSet<>();
    static final String COMMAND_HANDLER_INDEX = "command-handler-index.txt";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        var commandHandlers = roundEnv.getElementsAnnotatedWith(CommandHandler.class)
                .stream()
                .filter(it -> (it.getKind() == ElementKind.METHOD || it.getKind() == ElementKind.CONSTRUCTOR))
                .map(it -> (ExecutableElement) it)
                .filter(it -> !it.getParameters().isEmpty())
                .collect(Collectors.toSet());

        var targetAggregateIdElements = roundEnv.getElementsAnnotatedWith(TargetAggregateId.class)
                .stream()
                .filter(it -> (
                        it.getKind() == ElementKind.FIELD ||
                                it.getKind() == ElementKind.METHOD) ||
                        it.getKind() == ElementKind.RECORD_COMPONENT
                )
                .collect(Collectors.toSet());


        Set<String> intersection = validateCommands(commandHandlers, targetAggregateIdElements);
        if (!intersection.isEmpty()) {
            intersection.forEach(cmd ->
                    error(("Class %s is used as a CommandHandler Parameter but does not have a " +
                            "TargetAggregateIdenfier annotated field, method or record component").formatted(cmd))
            );

            return false;
        }

        targetAggregateIdElements.stream()
                .map(this::transformCommandElement)
                .filter(Objects::nonNull)
                .forEach(aggregateIdDetails::add);

        var handlerProperties = commandHandlers.stream()
                .filter(it -> !it.getParameters().isEmpty())
                .map(this::transform)
//                .map(CommandHandlerProperties::toString)
                .collect(Collectors.toSet());

        indexedClasses.addAll(handlerProperties);
        if (!roundEnv.processingOver()) {
            return false;
        }

        aggregateIdDetails
                .stream()
                .filter(HandleAggregateIdDetails::isValid)
                .sorted(Comparator.comparing(HandleAggregateIdDetails::commandClassName))
                .forEach(it ->
                note("Aggregate Details: " + it.toString()));
//        Map<String, HandleAggregateIdDetails> handleAggregateIdDetails = aggregateIdDetails
//                .stream()
//                .collect(Collectors.toMap(
//                        HandleAggregateIdDetails::commandClassName,
//                        it -> it
//                ));

//        try {
//            writeSimpleNameIndexFile(indexedClasses, ANNOTATED_INDEX_PREFIX + COMMAND_HANDLER_INDEX);
//        } catch (IOException e) {
//            error("[ClassIndexProcessor] Can't write index file: " + e.getMessage());
//        }

        return false;
    }

    private HandleAggregateIdDetails transformCommandElement(Element t) {
        TypeElement enclosingElement = (TypeElement) typeUtils.asElement(t.getEnclosingElement().asType());
//        if(enclosingElement.getKind() == ElementKind.RECORD) {
//            if(t.getKind() == ElementKind.RECORD_COMPONENT) {
//                var recordComponent = (RecordComponentElement) t;
//                ExecutableElement accessor = recordComponent.getAccessor();
//                var returnType = (TypeElement) typeUtils.asElement(accessor.getReturnType());
//                builder.isRecord(true)
//                        .accessorType(returnType.getQualifiedName().toString());
//
//                return builder.build();
//            }
//            return null;
//        }

        var builder = switch (enclosingElement.getKind()) {
            case RECORD, CLASS -> new HandleAggregateIdDetailsBuilder()
                    .accessorKind(t.getKind())
                    .modifiers(t.getModifiers())
                    .accessorName(t.getSimpleName().toString())
                    .commandClassName(enclosingElement.getQualifiedName().toString());
            default -> new HandleAggregateIdDetailsBuilder();
        };

        if (enclosingElement.getKind() == ElementKind.RECORD && t.getKind() == ElementKind.RECORD_COMPONENT) {
            var recordComponent = (RecordComponentElement) t;
            ExecutableElement accessor = recordComponent.getAccessor();
            var returnType = (TypeElement) typeUtils.asElement(accessor.getReturnType());
            builder.isRecord(true)
                    .accessorType(returnType.getQualifiedName().toString());
        }

        if (enclosingElement.getKind() == ElementKind.CLASS) {
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
        }
        return builder.build();
    }

    private CommandHandlerProperties transform(ExecutableElement commandElement, Map<String, HandleAggregateIdDetails> handleAggregateDetails) {
        TypeElement paramTypeElement = getParamType(commandElement);
        var commandType = paramTypeElement.getQualifiedName().toString();

        String name = commandElement.getSimpleName().toString();
        TypeElement enclosingElement = (TypeElement) commandElement.getEnclosingElement();
        HandleAggregateIdDetails handleAggregateIdDetails = handleAggregateDetails.get(commandType);

        var builder = CommandHandlerProperties.builder()
                .commandType(commandType)
                .aggregateType(enclosingElement.getQualifiedName().toString())
                .handlerName(name);

        if (handleAggregateIdDetails != null) {

            builder.build();
        }
        return builder.build();
    }

    private Set<String> validateCommands(Set<ExecutableElement> commandHandlers, Set<? extends Element> targetAggregateIdElements) {
        var targetAggregateIdEnclosingElement = targetAggregateIdElements.stream()
                .map(Element::getEnclosingElement)
                .map(it -> (TypeElement) it)
                .map(it -> it.getQualifiedName().toString())
                .collect(Collectors.toSet());

        var nonConstructorHandlers = commandHandlers.stream()
                .filter(it -> it.getKind() != ElementKind.CONSTRUCTOR)
                .map(this::getParamType)
                .map(it -> it.getQualifiedName().toString())
                .collect(Collectors.toSet());
        return disjunction(nonConstructorHandlers, targetAggregateIdEnclosingElement);
    }

    private CommandHandlerProperties transform(ExecutableElement it) {
        TypeElement paramTypeElement = getParamType(it);
        String name = it.getSimpleName().toString();
        TypeElement enclosingElement = (TypeElement) it.getEnclosingElement();
        return new CommandHandlerProperties(
                paramTypeElement.getQualifiedName().toString(),
                enclosingElement.getQualifiedName().toString(),
                name
        );
    }

    private TypeElement getParamType(ExecutableElement it) {
        VariableElement firstParameter = it.getParameters().getFirst();
        TypeMirror typeParameterMirror = firstParameter.asType();
        return (TypeElement) typeUtils.asElement(typeParameterMirror);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(
                CommandHandler.class.getCanonicalName()
//                TargetAggregateId.class.getCanonicalName()
        );
    }

    private static Set<String> disjunction(final Set<String> first, final Set<String> second) {
        final Set<String> copy = new HashSet<>(first);
        copy.removeAll(second);
        return copy;
    }

    record HandleAggregateIdDetails(
            String commandClassName,
            String accessorName,
            Set<Modifier> modifiers,
            String accessorType, // FQDN of type
            ElementKind accessorKind, // (FIELD, METHOD, RECORD_COMPONENT)
            boolean isRecord//
    ) {
        public boolean isValid() {
            return Strings.isNotNullOrEmpty(commandClassName) && Strings.isNotNullOrEmpty(accessorName)
                    && !modifiers.isEmpty();
        }
    }

    static class HandleAggregateIdDetailsBuilder {
        private String commandClassName;
        private String accessorName;
        private Set<Modifier> modifiers;
        private String accessorType; // FQDN of type
        private ElementKind accessorKind;// (FIELD, METHOD, RECORD_COMPONENT)
        private boolean isRecord;

        HandleAggregateIdDetailsBuilder commandClassName(String commandClassName) {
            this.commandClassName = commandClassName;
            return this;
        }

        HandleAggregateIdDetailsBuilder accessorType(String accessorType) {
            this.accessorType = accessorType;
            return this;
        }

        HandleAggregateIdDetailsBuilder accessorName(String accessorName) {
            this.accessorName = accessorName;
            return this;
        }

        HandleAggregateIdDetailsBuilder isRecord(boolean isRecord) {
            this.isRecord = isRecord;
            return this;
        }

        HandleAggregateIdDetailsBuilder accessorKind(ElementKind accessorKind) {
            this.accessorKind = accessorKind;
            return this;
        }

        HandleAggregateIdDetailsBuilder modifiers(Set<Modifier> modifiers) {
            this.modifiers = modifiers;
            return this;
        }

        public HandleAggregateIdDetails build() {
            return new HandleAggregateIdDetails(
                    commandClassName,
                    accessorName,
                    (modifiers == null) ? new HashSet<>() : modifiers,
                    accessorType, accessorKind, isRecord);
        }
    }
}
