package braque.internal.codegen;

import com.google.auto.common.MoreElements;
import com.google.common.collect.SetMultimap;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import static braque.internal.codegen.Utils.getAnnotationMirrorValueAsTypeElement;

/**
 * Adds on code to the deserializer and serializer to make things work.
 * // TODO all these functions are not needed by both classes all the time, perhaps make common class.
 * Created by mikesolomon on 15/09/16.
 */

class DeserializerHelper {

    static void addStaticFooterFunctions(CodeBuilder builder, SetMultimap<Class<? extends Annotation>, Element> elementsByAnnotation) {
        builder.a("    static public <T extends ").processing().a("RESTOperation> java.util.List<T> deserialize(java.util.Map<String, Object> serialized, Class<T> type) {").n()
                .a("      return deserialize(serialized, type, new java.util.ArrayList<String>());").n()
                .a("   }").n().n()
                .a("    static public <T extends ").processing().a("RESTOperation> java.util.List<T> deserialize(java.util.Map<String, Object> serialized, Class<T> type, java.util.Collection<String> remainingPaths) {").n()
                .a("        if (serialized.size() == 0) {").n()
                .a("            return new java.util.ArrayList<>();").n()
                .a("        }").n().n()
                .a("        java.util.Map<String, java.util.Map<String, Object>> validSerialized = new java.util.HashMap<>();").n().n()
                .a("        String restEndpoint = null;").n()
                .a("        for (java.util.Map.Entry<String,Object> entry : serialized.entrySet()) {").n()
                .a("            String[] split = entry.getKey().split(\"/\");").n()
                .a("            if (split.length >= 3) {").n()
                .a("                if (restEndpoint == null) {").n()
                .a("                    restEndpoint = split[0];").n()
                .a("                }").n()
                .a("                // sanity check: this operation only makes sense if we are deserializing the same REST endpoint").n()
                .a("                if (!restEndpoint.equals(split[0])) {").n()
                .a("                    return new java.util.ArrayList<>();").n()
                .a("                }").n()
                .a("                if (!validSerialized.containsKey(split[1])) {").n()
                .a("                    validSerialized.put(split[1], new java.util.HashMap<String, Object>());").n()
                .a("                }").n()
                .a("                validSerialized.get(split[1]).put(entry.getKey(), entry.getValue());").n()
                .a("            }").n()
                .a("        }").n().n()
                .a("        if (restEndpoint == null) {").n()
                .a("            return new java.util.ArrayList<>();").n()
                .a("        }").n().n()
                .a("        java.util.List<T> out = new java.util.ArrayList<>();").n().n();
        int i = 0;
        for (Map.Entry<Class<? extends Annotation>, Element> entry : elementsByAnnotation.entries()) {
            if (Arrays.asList(new String[]{Utils.TYPE}).contains(entry.getKey().getSimpleName())) {
                continue;
            }
            String op = entry.getKey().getSimpleName();
            String path = entry.getValue().getSimpleName().toString();
            String pkg = Utils.getPackage(entry.getValue());
            String newPkg = pkg + Utils.DOT + Utils.GENERATED_OBJECTS_PACKAGE_NAME;
            if (!Utils.SHOW.equals(entry.getKey().getSimpleName())) {
                continue;
            }
            AnnotationMirror annotationMirror = Utils.getShowAnnotation(MoreElements.asType(entry.getValue()));
            TypeElement maybeReturnType = Utils.getAnnotationMirrorValueAsTypeElement(annotationMirror, Utils.BASETYPE);
            maybeReturnType = maybeReturnType == null
                    ? Utils.getPropertyAttachment(Utils.getAnnotationMirrorValueAsTypeElement(annotationMirror, Utils.ARGUMENT))
                    : maybeReturnType;
            builder.a(i++ == 0 ? "        if" : " else if").a(" (type.equals(").a(newPkg).a(Utils.DOT).a(path).a(op).a(maybeReturnType).a(".class)) {").n()
                    .a("            for (java.util.Map.Entry<String, java.util.Map<String, Object>> toDeserialize : validSerialized.entrySet()) {").n()
                    .a("              ").a(newPkg).d().a(path).a(op).a(maybeReturnType).a(" result = deserialize" ).a(path).a(op).a("(toDeserialize.getValue(),").n()
                    .a("                        java.util.Arrays.asList(new String[]{toDeserialize.getKey()}), false);").n()
                    .a("                if (result != null) {").n()
                    .a("                  out.add((T)result);").n()
                    .a("                }").n()
                    .a("            }").n()
                    .a("        }");
        }

        builder.a("        for (java.util.Map.Entry<String, java.util.Map<String, Object>> toRemoveFromOriginalMap : validSerialized.entrySet()) {").n()
                .a("          for (String s : toRemoveFromOriginalMap.getValue().keySet()) {").n()
                .a("            remainingPaths.add(s);").n()
                .a("          }").n()
                .a("        }").n()
                .a("        return out;").n()
                .a("    }").n();
    }
}
