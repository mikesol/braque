package braque.internal.codegen;

import com.google.auto.common.MoreElements;
import com.google.common.collect.SetMultimap;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import static braque.internal.codegen.Utils.getAnnotationMirrorValueArrayAsTypeElementList;
import static braque.internal.codegen.Utils.getAnnotationMirrorValueAsTypeElement;

/**
 * Builds the StringProvisioner class
 * Created by mikesolomon on 14/09/16.
 */

class StringHandler {

    private static String removeTrailingWildcard(String s) {
        return "/*".equals(s.substring(s.length()-2)) ? s.substring(0, s.length() - 2) : s;
    }

    private static int buildRestType(Map<String, String> stringMap, TypeElement attachment,
                                     String path, TypeElement typeElement, List<TypeElement> properties,
                                     Map<String, TypeTree> typeTreeMap, String comingFrom, int pos) {
        final int startPos = pos;

        String elementName = typeElement.getSimpleName() + comingFrom;


        stringMap.put("path"+elementName, removeTrailingWildcard(path));
        stringMap.put("path"+elementName+"_type", path+"/_type");
        for (TypeElement typeElement1 : Utils.getAllNecessaryElements(typeTreeMap.get(attachment.getQualifiedName().toString()))) {
            stringMap.put("path"+elementName+typeElement1.getSimpleName(), path+"/"+typeElement1.getSimpleName().toString().toLowerCase());
            stringMap.put("prop"+typeElement1.getSimpleName(), typeElement1.getSimpleName().toString());
        }

        while (pos < properties.size()) {
            if (Utils.DOLLAR.equals(properties.get(pos).getSimpleName().toString())) {
                pos++;
                break;
            }
            TypeElement currentProperty = properties.get(pos);
            stringMap.put("prop"+currentProperty.getSimpleName(), currentProperty.getSimpleName().toString());
            TypeElement baseType = Utils.getPropertyTypeElement(currentProperty);
            stringMap.put("path"+elementName+currentProperty.getSimpleName(), path+"/"+currentProperty.getSimpleName().toString().toLowerCase());
            if (Utils.hasTypeAnnotation(baseType)) {
                String wildcard = Utils.isPropertyList(Utils.getPropertyAnnotation(currentProperty)) || Utils.isPropertySet(Utils.getPropertyAnnotation(currentProperty))
                        ? "/*"
                        : "";
                pos += buildRestType(stringMap, baseType, path+"/"+currentProperty.getSimpleName().toString().toLowerCase()+wildcard,
                        typeElement, properties, typeTreeMap, comingFrom + currentProperty.getSimpleName(), pos + 1);
            }
            pos++;
        }
        return pos - startPos;
    }

    static void handleElements(SetMultimap<Class<? extends Annotation>, Element> elementsByAnnotation,
                               ProcessingEnvironment processingEnvironment) {
        CodeBuilder builder = new CodeBuilder();
        builder.pkg(Utils.STATIC_METHOD_CLASSES_PACKAGE).pubic().klass().a("StringProvisioner").sbeg();
        List<TypeElement> typedElements = new ArrayList<>();
        Map<String, String> stringMap = new LinkedHashMap<>();
        for (Map.Entry<Class<? extends Annotation>, Element> elementByAnnotation : elementsByAnnotation.entries()) {
            TypeElement typeElement = MoreElements.asType(elementByAnnotation.getValue());
            if (Utils.TYPE.equals(elementByAnnotation.getKey().getSimpleName())) {
                stringMap.put("type" + typeElement.getSimpleName(), typeElement.getSimpleName().toString());
                typedElements.add(typeElement);
            }
        }
        Map<String, TypeTree> typeTreeMap = Utils.buildTypeTree(typedElements);
        for (Map.Entry<Class<? extends Annotation>, Element> elementByAnnotation : elementsByAnnotation.entries()) {
            TypeElement typeElement = MoreElements.asType(elementByAnnotation.getValue());
            if (!Utils.TYPE.equals(elementByAnnotation.getKey().getSimpleName())) {
                AnnotationMirror annotationMirror = Utils.SHOW.equals(elementByAnnotation.getKey().getSimpleName())
                        ? Utils.getShowAnnotation(typeElement)
                        : Utils.UPDATE.equals(elementByAnnotation.getKey().getSimpleName())
                        ? Utils.getUpdateAnnotation(typeElement)
                        : Utils.CREATE.equals(elementByAnnotation.getKey().getSimpleName())
                        ? Utils.getCreateAnnotation(typeElement)
                        : Utils.getDestroyAnnotation(typeElement);
                TypeElement maybeReturnType = Utils.getAnnotationMirrorValueAsTypeElement(annotationMirror, Utils.BASETYPE);
                buildRestType(stringMap,
                        maybeReturnType == null
                                ? Utils.getPropertyAttachment(Utils.getAnnotationMirrorValueAsTypeElement(annotationMirror, Utils.ARGUMENT))
                                : maybeReturnType,
                        typeElement.getSimpleName().toString().toLowerCase()+"/*",
                        typeElement,
                        Utils.getAnnotationMirrorValueArrayAsTypeElementList(annotationMirror, Utils.PROPERTIES),
                        typeTreeMap, "", 0);
            }

        }
        for (Map.Entry<String, String> entry : stringMap.entrySet()) {
            builder.n().sp().statik().pubic().str().s().a(entry.getKey()).Pp().sbeg()
                    .spret().q(entry.getValue()).cn().spend();
        }
        builder.spend();
        Utils.write(Utils.STATIC_METHOD_CLASSES_PACKAGE + Utils.DOT+"StringProvisioner", builder, processingEnvironment);
    }
}
