package braque.internal.codegen;

import com.google.auto.common.MoreElements;
import com.google.auto.common.MoreTypes;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Validates the @Type annotation
 * Created by mikesolomon on 11/09/16.
 */

class TypeValidator {

    static void validateSingleUid(Element element, ProcessingEnvironment processingEnvironment) {
        boolean uidFound = false;
        String uidElementName = null;
        validateSingleUid(element, uidFound, uidElementName, processingEnvironment);
    }

    static void verifySupersAreNotForbidden(TypeTree tt, String forbidden, ProcessingEnvironment processingEnvironment) {
        if (tt.mSuper != null) {
            if (tt.mSuper.mBase.getQualifiedName().toString().equals(forbidden)) {
                processingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, "Circular dependency for type "+forbidden+".");
            }
            verifySupersAreNotForbidden(tt.mSuper, forbidden, processingEnvironment);
        }
    }

    static void verifySubsAreNotForbidden(TypeTree tt, String forbidden, ProcessingEnvironment processingEnvironment) {
        if (!tt.mSubs.isEmpty()) {
            for (TypeTree sub : tt.mSubs) {
                if (sub.mBase.getQualifiedName().toString().equals(forbidden)) {
                    processingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, "Circular dependency for type " + forbidden + ".");
                }
                verifySubsAreNotForbidden(sub, forbidden, processingEnvironment);
            }
        }
    }

    static void verifyCircularDependencies(Map<String, TypeTree> typeTreeMap, ProcessingEnvironment processingEnvironment) {
        for (Map.Entry<String, TypeTree> entry : typeTreeMap.entrySet()) {
            verifySupersAreNotForbidden(entry.getValue(), entry.getKey(), processingEnvironment);
            verifySubsAreNotForbidden(entry.getValue(), entry.getKey(), processingEnvironment);
        }
    }

    static void validateSingleUid(Element element, boolean uidFound,
                                  String uidElemenetName, ProcessingEnvironment processingEnvironment) {
        TypeElement typeElement = MoreElements.asType(element);
        PackageElement packageElement = MoreElements.getPackage(element);
        for (Element enclosed : packageElement.getEnclosedElements()) {
            TypeElement enclosedType = MoreElements.asType(enclosed);
            if (uidFound && Utils.hasUIDAnnotation(enclosedType)
                    && !enclosedType.getQualifiedName().toString().equals(typeElement.getQualifiedName().toString())) {
                processingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR.ERROR,
                        "Cannot have two @UID annotated elements in one package: " +
                        uidElemenetName + " and " +
                        enclosedType.getQualifiedName().toString() + ".");
            }
            uidFound = uidFound || Utils.hasUIDAnnotation(enclosedType);
            if (uidFound && uidElemenetName == null) {
                uidElemenetName = enclosedType.getQualifiedName().toString();
            }
        }
        TypeElement type = Utils.getTypeFromPackage(packageElement);
        List<TypeElement> typeElements = Utils.getAnnotationMirrorValueArrayAsTypeElementList(Utils.getTypeAnnotation(type));
        for (TypeElement elt : typeElements) {
            if (Utils.hasTypeAnnotation(elt)) {
                validateSingleUid(elt, uidFound, uidElemenetName, processingEnvironment);
            }
        }
    }

    static void validateTypes(Collection<Element> elements, ProcessingEnvironment processingEnvironment) {
        // types cannot be inner classes
        for (Element element : elements) {
            if (!(element.getEnclosingElement() instanceof PackageElement)) {
                processingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "@Type annotated elements cannot be inner classes: check " +
                        element.getSimpleName() + " in package " + MoreElements.getPackage(element));
            }
        }
        // there cannot be two types in one package
        for (Element element : elements) {
            TypeElement typeElement = MoreElements.asType(element);
            PackageElement packageElement = MoreElements.getPackage(element);
            for (Element enclosed : packageElement.getEnclosedElements()) {
                TypeElement enclosedType = MoreElements.asType(enclosed);
                if (Utils.hasTypeAnnotation(enclosedType)
                        && !enclosedType.getQualifiedName().toString().equals(typeElement.getQualifiedName().toString())) {
                    processingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR,
                            "Cannot have two @Type annotated elements in one package: " +
                            enclosedType.getQualifiedName().toString() + " and " +
                            typeElement.getQualifiedName().toString() + ".");
                }
            }
        }
        // types can only inherit from one type
        for (Element element : elements) {
            List<TypeElement> typeElementList =
                    Utils.getAnnotationMirrorValueArrayAsTypeElementList(Utils.getTypeAnnotation(element));
            boolean typeFound = false;
            String typeString = null;
            for (TypeElement typeElement : typeElementList) {
                if (typeFound && Utils.hasTypeAnnotation(typeElement)) {
                    processingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR,
                            "A @Type annotation may only contain one other type in its necessary value list. " +
                                    "This is treated as a super type.  Otherwise, a typed object cannot be \"necessary\" " +
                                    "for another one in Braque. Necessary types must be primitives like String or Integer. " +
                                    "This is a design limitation of Braque in order to provide unambiguous information " +
                                    "in \"necessary\" types - the Integer 5 will always be 5 but IdObject o can implement lots " +
                                    "of different interfaces.  If a sensible way to make IdObjects \"necessary\" is proposed, then " +
                                    "of course it is up for discussion.  At any rate, the "+
                            "annotation for " + MoreElements.asType(element).getQualifiedName().toString() +
                                    " has at least two @Type elements, pick one: " +
                            typeString + " and " + typeElement.getQualifiedName().toString());
                }
                typeFound = typeFound || Utils.hasTypeAnnotation(typeElement);
                if (typeFound && typeString == null) {
                    typeString = typeElement.getQualifiedName().toString();
                }
            }
        }
        // types can only have one property with a UID annotation
        for (Element element : elements) {
            validateSingleUid(element, processingEnvironment);
        }
        // type elements must have at least one property with a UID annotation
        for (Element element : elements) {
            TypeElement uidElement = Utils.getUID(MoreElements.asType(element));
            if (uidElement == null) {
                processingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        "A @Type annotation must be linked to one and only one UID element. " +
                                MoreElements.asType(element).getQualifiedName().toString() + " has none.");
            }
        }
        // types may not have circular dependencies
        Map<String, TypeTree> typeTreeMap = Utils.buildTypeTree(elements);
        verifyCircularDependencies(typeTreeMap, processingEnvironment);
    }
}
