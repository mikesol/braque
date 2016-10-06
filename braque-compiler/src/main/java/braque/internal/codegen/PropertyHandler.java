package braque.internal.codegen;

import java.util.Arrays;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;

import static braque.internal.codegen.Utils.ADD;
import static braque.internal.codegen.Utils.CTOR;
import static braque.internal.codegen.Utils.DOT;
import static braque.internal.codegen.Utils.GET;
import static braque.internal.codegen.Utils.PROP;
import static braque.internal.codegen.Utils.PROPLIST;
import static braque.internal.codegen.Utils.PROPSET;
import static braque.internal.codegen.Utils.REMOVE;
import static braque.internal.codegen.Utils.SET;
import static braque.internal.codegen.Utils.getPackage;
import static braque.internal.codegen.Utils.getPropertyAttachment;
import static braque.internal.codegen.Utils.getPropertyTypeElement;
import static braque.internal.codegen.Utils.isComplexProperty;
import static braque.internal.codegen.Utils.isProperty;
import static braque.internal.codegen.Utils.isPropertyList;
import static braque.internal.codegen.Utils.isPropertySet;
import static braque.internal.codegen.Utils.last;
import static braque.internal.codegen.Utils.braque;

/**
 * Handles the processing of @Property annotations.
 * Created by mikesolomon on 11/09/16.
 */

class PropertyHandler {

    enum PropertyType {
        SINGLE,
        SET,
        LIST
    }

    final private PropertyType mPropertyType;

    PropertyHandler(PropertyType propertyType) {
        mPropertyType = propertyType;

    }

    private String propertyString() {
        switch (mPropertyType) {
            case LIST:
                return PROPLIST;
            case SET:
                return PROPSET;
            case SINGLE:
            default:
                return PROP;
        }
    }

    private void buildBaseProperty(String pkg, TypeElement typeElement, ProcessingEnvironment processingEnvironment) {
        CodeBuilder builder = new CodeBuilder();
        String generic = maybeGeneric(typeElement);
        String attachment = getPropertyAttachment(typeElement).getQualifiedName().toString();
        builder.pkg(pkg).pubic().iface().a(typeElement).as(generic).ext().processing().a(propertyString()).cs().$a(last(attachment));
        builder.sbeg().spend();
        Utils.write(pkg+DOT+typeElement.getSimpleName(), builder, processingEnvironment);
    }

    private void buildLinkedOrCtorProperty(String pkg, TypeElement typeElement,
                                           ProcessingEnvironment processingEnvironment, String linkedOrCtor) {
        CodeBuilder builder = new CodeBuilder();
        String generic = maybeGeneric(typeElement);
        String simpleGeneric = maybeSimpleGeneric(typeElement);
        builder.pkg(pkg).pubic().iface().a(typeElement).a(linkedOrCtor).as(generic)
                .ext().processing().a(linkedOrCtor).cs().a(typeElement).a(simpleGeneric).sbeg().spend();
        Utils.write(pkg+DOT+typeElement.getSimpleName()+linkedOrCtor, builder, processingEnvironment);
    }


    private void buildCtorProperty(String pkg, TypeElement typeElement, ProcessingEnvironment processingEnvironment) {
        buildLinkedOrCtorProperty(pkg, typeElement, processingEnvironment, CTOR);
    }

    final private static String[] PRIMITIVES =
            new String[]{String.class.getCanonicalName(), Integer.class.getCanonicalName(),
                    Double.class.getCanonicalName(), Float.class.getCanonicalName(),
                    Long.class.getCanonicalName(), Boolean.class.getCanonicalName()};


    private String maybeCollection(String returnType) {
        switch (mPropertyType) {
            case SET:
                return new CB().jset(returnType).toS();
            case LIST:
                return new CB().jlist(returnType).toS();
            case SINGLE:
            default:
                return returnType;
        }
    }

    private static String getExtendedElement(TypeElement typeElement) {
        return braque(getPropertyTypeElement(typeElement).getQualifiedName().toString());
    }

    private static String maybeGeneric(TypeElement typeElement) {
        return isComplexProperty(typeElement) ? new CB().G().as("T").ext().a(getExtendedElement(typeElement)).g().toS() : "";
    }

    private static String maybeSimpleGeneric(TypeElement typeElement) {
        return isComplexProperty(typeElement) ? new CB().Gg("T").toS() : "";
    }

    private void buildOperationProperty(String pkg, TypeElement typeElement, AnnotationMirror annotationMirror, String op,
                                        ProcessingEnvironment processingEnvironment) {
        CodeBuilder builder = new CodeBuilder();
        String returnType = Utils.getAnnotationMirrorValueAsQualifiedName(annotationMirror);
        if (!Arrays.asList(PRIMITIVES).contains(returnType)) {
            returnType = braque(returnType);
        }
        String generic = maybeGeneric(typeElement);
        String simpleGeneric = maybeSimpleGeneric(typeElement);
        builder.pkg(pkg).pubic().iface().a(typeElement).a(op).as(generic).ext().processing().a(op).cs().a(typeElement).a(simpleGeneric).sbeg();
        returnType = generic != null && !"".equals(generic) ? "T" : returnType;
        switch (op) {
            case GET:
                builder.sp().as(maybeCollection(returnType)).get(typeElement).cn();
                break;
            case SET:
                builder.sp().voi().set(typeElement).phinal().as(returnType).val().p().cn();
                break;
            case ADD:
                builder.sp().voi().addTo(typeElement).phinal().as(returnType).val().p().cn();
                break;
            case REMOVE:
                builder.sp().voi().removeFrom(typeElement).phinal().as(returnType).val().p().cn();
                break;
        }
        builder.spend();
        Utils.write(pkg+DOT+typeElement.getSimpleName()+op, builder, processingEnvironment);
    }

    private boolean validAnnotationMirror(AnnotationMirror annotationMirror) {
        return mPropertyType.equals(PropertyType.SET)
                ? isPropertySet(annotationMirror)
                : mPropertyType.equals(PropertyType.LIST)
                ? isPropertyList(annotationMirror)
                : isProperty(annotationMirror);
    }

    void handleProperty(TypeElement typeElement, ProcessingEnvironment processingEnvironment) {
        // first check for links

        for (AnnotationMirror annotationMirror : typeElement.getAnnotationMirrors()) {
            if (validAnnotationMirror(annotationMirror)) {
                String pkg = getPackage(typeElement);
                String newPkg = pkg+ DOT+Utils.GENERATED_OBJECTS_PACKAGE_NAME;
                buildBaseProperty(newPkg, typeElement, processingEnvironment);
                buildCtorProperty(newPkg, typeElement, processingEnvironment);
                buildOperationProperty(newPkg, typeElement, annotationMirror, GET, processingEnvironment);
                if (mPropertyType.equals(PropertyType.SINGLE)) {
                    buildOperationProperty(newPkg, typeElement, annotationMirror, SET, processingEnvironment);
                } else {
                    buildOperationProperty(newPkg, typeElement, annotationMirror, ADD, processingEnvironment);
                    buildOperationProperty(newPkg, typeElement, annotationMirror, REMOVE, processingEnvironment);
                }
            }
        }
    }

}
