package io.amoakoagyei;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

public class TypeElements {

    static TypeElement getParamType(Types typeUtils, ExecutableElement it) {
        if (it.getParameters().isEmpty()) {
            return null;
        }
        VariableElement firstParameter = it.getParameters().getFirst();
        TypeMirror typeParameterMirror = firstParameter.asType();
        return (TypeElement) typeUtils.asElement(typeParameterMirror);
    }

}
