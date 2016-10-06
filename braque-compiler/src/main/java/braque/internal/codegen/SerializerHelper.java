package braque.internal.codegen;

import static braque.internal.codegen.Utils.SERIALIZE;
import static braque.internal.codegen.Utils.DESERIALIZE;
import static braque.internal.codegen.Utils.DESERIALIZED;
import static braque.internal.codegen.Utils.PREVIOUSUIDS;

import com.google.auto.common.MoreElements;
import com.google.common.collect.SetMultimap;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Map;

/**
 * Adds on code to the deserializer and serializer to make things work.
 * // TODO all these functions are not needed by both classes all the time, perhaps make common class.
 * Created by mikesolomon on 15/09/16.
 */

class SerializerHelper {

    static void addStaticFooterFunctions(CodeBuilder builder, SetMultimap<Class<? extends Annotation>, Element> elementsByAnnotation) {

        builder.sp().statik().pubic().as(new CB().G().as("T").ext().processing().a("RESTEndpoint").g().toS())
                .sojmap().a(SERIALIZE)
                .P().phinal().as("T").a(DESERIALIZED).psbeg();
        int i = 0;
        for (Map.Entry<Class<? extends Annotation>, Element> entry : elementsByAnnotation.entries()) {
            if (Arrays.asList(new String[]{Utils.TYPE}).contains(entry.getKey().getSimpleName())) {
                continue;
            }
            String op = entry.getKey().getSimpleName();
            String path = entry.getValue().getSimpleName().toString();
            String pkg = Utils.getPackage(entry.getValue());
            String newPkg = pkg + Utils.DOT + Utils.GENERATED_OBJECTS_PACKAGE_NAME;
            AnnotationMirror annotationMirror = op.equals(Utils.SHOW)
                    ? Utils.getShowAnnotation(MoreElements.asType(entry.getValue()))
                    : op.equals(Utils.DESTROY)
                    ? Utils.getDestroyAnnotation(MoreElements.asType(entry.getValue()))
                    : op.equals(Utils.CREATE)
                    ? Utils.getCreateAnnotation(MoreElements.asType(entry.getValue()))
                    : Utils.getUpdateAnnotation(MoreElements.asType(entry.getValue()));
            TypeElement maybeReturnType = Utils.getAnnotationMirrorValueAsTypeElement(annotationMirror, Utils.BASETYPE);
            maybeReturnType = maybeReturnType == null
                    ? Utils.getPropertyAttachment(Utils.getAnnotationMirrorValueAsTypeElement(annotationMirror, Utils.ARGUMENT))
                    : maybeReturnType;
            builder.a(i++ == 0 ? "        if" : " else if").s().P().as(DESERIALIZED).instof().a(newPkg).a(Utils.DOT).a(path).a(op).a(maybeReturnType).a(") {").n()
                    .a("            return serialize((").a(newPkg).a(Utils.DOT).a(path).a(op).a(maybeReturnType).p().a(DESERIALIZED).a(")").cn()
                    .a("        }");
        }
        builder.n().spret().hashmap("String", "Object").cn();
        builder.spend().n();
    }
}
