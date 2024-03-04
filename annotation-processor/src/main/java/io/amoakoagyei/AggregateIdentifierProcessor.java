package io.amoakoagyei;

import com.google.auto.service.AutoService;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.*;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

record AggregateIdentifierMetadata(
        String accessorName,
        String accessorElementType,
        String enclosingElementType,
        ElementKind accessorKind,
        Set<Modifier> modifiers
) {

    @Override
    public String toString() {
        String mfs = modifiers == null ? "NULL" : modifiers
                .stream()
                .map(Enum::name)
                .collect(Collectors.joining(";"));
        return "%s,%s,%s,%s,%s".formatted(
                accessorName,
                accessorElementType,
                enclosingElementType,
                accessorKind.name(),
                mfs
        );
    }

    static class AggregateIdentifierMetadataBuilder {
        private String accessorName;
        private String accessorElementType; // FQDN of type
        private String enclosingElementType;
        private ElementKind accessorKind;// (FIELD, METHOD, RECORD_COMPONENT)
        private Set<Modifier> modifiers;

        AggregateIdentifierMetadataBuilder enclosingElementType(String enclosingElementType) {
            this.enclosingElementType = enclosingElementType;
            return this;
        }

        AggregateIdentifierMetadataBuilder accessorElementType(String accessorElementType) {
            this.accessorElementType = accessorElementType;
            return this;
        }

        AggregateIdentifierMetadataBuilder accessorName(String accessorName) {
            this.accessorName = accessorName;
            return this;
        }

        AggregateIdentifierMetadataBuilder accessorKind(ElementKind accessorKind) {
            this.accessorKind = accessorKind;
            return this;
        }

        AggregateIdentifierMetadataBuilder modifiers(Set<Modifier> modifiers) {
            this.modifiers = modifiers;
            return this;
        }

        public AggregateIdentifierMetadata build() {
            return new AggregateIdentifierMetadata(
                    accessorName,
                    accessorElementType,
                    enclosingElementType,
                    accessorKind,
                    (modifiers == null) ? new HashSet<>() : modifiers);
        }
    }
}

@AutoService(Processor.class)
public class AggregateIdentifierProcessor extends AbstractAnnotationProcessor {
    private static final Set<AggregateIdentifierMetadata> indexedClasses = new HashSet<>();
    public static final String COMMAND_HANDLER_INDEX = "target-aggregate-id-index.txt";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        var aggregateIdMetadata = roundEnv.getElementsAnnotatedWith(AggregateIdentifier.class)
                .stream()
                .filter(it -> (
                        it.getKind() == ElementKind.FIELD ||
                                it.getKind() == ElementKind.METHOD) ||
                        it.getKind() == ElementKind.RECORD_COMPONENT
                )
                .map(this::transformCommandElement)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        indexedClasses.addAll(aggregateIdMetadata);

        if (!roundEnv.processingOver()) {
            return false;
        }

        try {
            var indexedIdentifiers = indexedClasses.stream()
                    .map(AggregateIdentifierMetadata::toString)
                    .collect(Collectors.toSet());
            writeSimpleNameIndexFile(indexedIdentifiers, ANNOTATED_INDEX_PREFIX + COMMAND_HANDLER_INDEX);
        } catch (IOException e) {
            error("[ClassIndexProcessor] Can't write index file: " + e.getMessage());
        }
        return false;
    }

    private AggregateIdentifierMetadata transformCommandElement(Element t) {
        TypeElement enclosingElement = (TypeElement) typeUtils.asElement(t.getEnclosingElement().asType());
        if (enclosingElement.getKind() == ElementKind.RECORD && t.getKind() == ElementKind.RECORD_COMPONENT) {
            return transformRecordComponentElement(t, enclosingElement);
        } else if (enclosingElement.getKind() != ElementKind.RECORD) {
            // for constructor Records, fields are implicitly available so we need to ignore those
            var builder = new AggregateIdentifierMetadata.AggregateIdentifierMetadataBuilder()
                    .accessorKind(t.getKind())
                    .modifiers(t.getModifiers())
                    .accessorName(t.getSimpleName().toString())
                    .enclosingElementType(enclosingElement.getQualifiedName().toString());

            switch (t.getKind()) {
                case METHOD -> {
                    var exec = (ExecutableElement) t;
                    var typeElement = (TypeElement) typeUtils.asElement(exec.getReturnType());
                    builder.accessorElementType(typeElement.getQualifiedName().toString());
                }
                case FIELD -> {
                    var typeElement = (TypeElement) typeUtils.asElement(t.asType());
                    builder.accessorElementType(typeElement.getQualifiedName().toString());
                }
                default -> note("unhandled");
            }
            return builder.build();
        }
        return null;
    }

    private AggregateIdentifierMetadata transformRecordComponentElement(Element t, TypeElement enclosingElement) {
        var builder = new AggregateIdentifierMetadata.AggregateIdentifierMetadataBuilder()
                .accessorKind(t.getKind())
                .modifiers(t.getModifiers())
                .accessorName(t.getSimpleName().toString())
                .enclosingElementType(enclosingElement.getQualifiedName().toString());

        var recordComponent = (RecordComponentElement) t;
        ExecutableElement accessor = recordComponent.getAccessor();
        var returnType = (TypeElement) typeUtils.asElement(accessor.getReturnType());
        builder.accessorElementType(returnType.getQualifiedName().toString());

        return builder.build();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(
                AggregateIdentifier.class.getCanonicalName()
        );
    }
}
