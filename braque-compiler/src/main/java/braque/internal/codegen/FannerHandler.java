package braque.internal.codegen;

import braque.Operation;

import braque.Type;
import com.google.common.collect.SetMultimap;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import static braque.internal.codegen.Utils.*;

/**
 * Creates the Fanner, an element that helps take certain endpoints and propogate them to other endpoints.
 * Created by mikesolomon on 14/09/16.
 */

class FannerHandler {
    static void fan(PropertyToInfoMap propertyToInfoMap, PathInfoMap pathInfoMap, Map<String, TypeTree> typeTreeMap,
                    SetMultimap<Class<? extends Annotation>, Element> elementsByAnnotation,
                    ProcessingEnvironment processingEnvironment) {
        CodeBuilder codeBuilder = new CodeBuilder();
        codeBuilder.pkg(Utils.STATIC_METHOD_CLASSES_PACKAGE).pubic().klass().a("Fanner").sbeg()
                .sp().statik().pubic().phinal()
                .jmap("Class<? extends " + Utils.BRAQUE + ".Prop>", new CB().jset("String").toS()).mas("PropertyToPaths")
                .eq().hashmap().cn()
                .sp().statik().pubic().phinal()
                .jmap("String", "Class<? extends " + Utils.BRAQUE + ".Prop>").mas("PathToProperty")
                .eq().hashmap().cn()
                .sp().statik().pubic().phinal()
                .jmap("String", new CB().jset(Utils.BRAQUE + ".Operation").toS()).mas("PathToOperations")
                .eq().hashmap().cn()
                .sp().statik().pubic().phinal()
                .jmap(Utils.BRAQUE + ".Operation", new CB().jset("String").toS()).mas("OperationToPaths")
                .eq().hashmap().cn()
                .sp().statik().pubic().phinal()
                .jmap("Class<? extends " + Utils.BRAQUE + ".Prop>", Utils.BRAQUE + ".PropertyManyness").mas("PropertyToPropertyManyness")
                .eq().hashmap().cn()
                .sp().statik().pubic().phinal()
                .jmap("Class<? extends " + Utils.BRAQUE + ".Prop>", "String").mas("PropertyToPropertyName")
                .eq().hashmap().cn()
                .sp().statik().pubic().phinal()
                .jmap("String", "Class<? extends " + Utils.BRAQUE + ".Prop>").mas("PropertyNameToProperty")
                .eq().hashmap().cn()
                .sp().statik().pubic().phinal()
                .jmap("Class<? extends " + Utils.BRAQUE + ".BraqueObject>", "String").mas("TypeToTypeName")
                .eq().hashmap().cn()
                .sp().statik().pubic().phinal()
                .jmap("String", "Class<? extends " + Utils.BRAQUE + ".BraqueObject>").mas("TypeNameToType")
                .eq().hashmap().cn()
                .sp().statik().pubic().phinal()
                .jmap("Class<?>", "java.util.Set<String>").mas("TypeToPaths")
                .eq().hashmap().cn()
                .sp().statik().pubic().phinal()
                .jmap("String", "java.util.Set<Class<?>>").mas("PathToTypes")
                .eq().hashmap().cn()
                .sp().statik().pubic().phinal()
                .jset(new CB().a("Class<? ").ext().processing().a("Prop").g().toS()).mas("UIDProperties")
                .eq().hashset().cn()
                .sp().statik().pubic().phinal()
                .jmap("Class<? extends " + Utils.BRAQUE + ".BraqueObject>", "java.util.List<Class<? extends " + Utils.BRAQUE + ".BraqueObject>>").mas("TypeToSubTypes")
                .eq().hashmap().cn().n()
                .sp().statik().pubic().phinal()
                .jmap("Class<? extends " + Utils.BRAQUE + ".BraqueObject>", "Class<? extends " + Utils.BRAQUE + ".BraqueObject>").mas("TypeToSuperType")
                .eq().hashmap().cn().n()
                .sp().statik().sbeg();
        // we use the usedAlready anti-pattern because paths are repeated for multiple operations
        for (Map.Entry<String, PropertyInfo> entry : propertyToInfoMap.entrySet()) {
            codeBuilder.sp().ma("PropertyToPaths.put(").a(Utils.braque(entry.getKey())).a(".class").cs().hashset("String").p().cn();
            Set<String> usedAlready = new HashSet<>();
            for (Pair<String, Operation> pair : entry.getValue().paths) {
                if (!usedAlready.contains(pair.getKey())) {
                    codeBuilder.sp().ma("PropertyToPaths.get(").a(Utils.braque(entry.getKey())).a(".class").p().d().add().stringProvPath().a(pair.getKey()).Ppp().cn();
                    usedAlready.add(pair.getKey());
                }
            }
        }
        codeBuilder.n();
        for (Map.Entry<String, PropertyInfo> entry : propertyToInfoMap.entrySet()) {
            Set<String> usedAlready = new HashSet<>();
            for (Pair<String, Operation> pair : entry.getValue().paths) {
                if (!usedAlready.contains(pair.getKey())) {
                    codeBuilder.sp().ma("PathToProperty.put(").stringProvPath().a(pair.getKey()).Pp().cs().a(Utils.braque(entry.getKey())).a(".class").p().cn();
                    usedAlready.add(pair.getKey());
                }
            }
        }
        codeBuilder.n();
        for (Map.Entry<String, PropertyInfo> entry : propertyToInfoMap.entrySet()) {
            Set<String> usedAlready = new HashSet<>();
            if (entry.getValue().isUID && !usedAlready.contains(entry.getKey())) {
                codeBuilder.sp().ma("UIDProperties").d().add().a(Utils.braque(entry.getKey())).a(".class").p().cn();
                usedAlready.add(entry.getKey());
            }
        }
        codeBuilder.n();
        Map<String, Set<Operation>> pathsToOperations = new HashMap<>();
        Map<Operation, Set<String>> operationToPaths = new HashMap<>();
        for (Map.Entry<String, PropertyInfo> entry : propertyToInfoMap.entrySet()) {
            for (Pair<String, Operation> subEntry : entry.getValue().paths) {
                if (!pathsToOperations.containsKey(subEntry.getKey())) {
                    pathsToOperations.put(subEntry.getKey(), new HashSet<Operation>());
                }
                pathsToOperations.get(subEntry.getKey()).add(subEntry.getValue());
                if (!operationToPaths.containsKey(subEntry.getValue())) {
                    operationToPaths.put(subEntry.getValue(), new HashSet<String>());
                }
                operationToPaths.get(subEntry.getValue()).add(subEntry.getKey());

            }
        }
        for (Map.Entry<Class<? extends Annotation>, Element> entry : elementsByAnnotation.entries()) {
            if (Utils.hasTypeAnnotation(entry.getValue())) {
                continue;
            }
            Operation operation = entry.getKey().getSimpleName().equals(SHOW)
                    ? Operation.SHOW
                    : entry.getKey().getSimpleName().equals(UPDATE)
                    ? Operation.UPDATE
                    : entry.getKey().getSimpleName().equals(DESTROY)
                    ? Operation.DESTROY
                    : Operation.CREATE;
            String path = entry.getValue().getSimpleName().toString();
            if (!pathsToOperations.containsKey(path)) {
                pathsToOperations.put(path, new HashSet<Operation>());
            }
            pathsToOperations.get(path).add(operation);
            if (!operationToPaths.containsKey(operation)) {
                operationToPaths.put(operation, new HashSet<String>());
            }
            operationToPaths.get(operation).add(path);
        }
        for (Map.Entry<String, Set<Operation>> entry : pathsToOperations.entrySet()) {
            codeBuilder.sp().ma("PathToOperations.put(").stringProvPath().a(entry.getKey())
                    .Pp().cs().hashset(Utils.BRAQUE + DOT + "Operation").p().cn();
            for (Operation operation : entry.getValue()) {
                codeBuilder.sp().ma("PathToOperations.get(").stringProvPath().a(entry.getKey()).Ppp()
                        .d().add().processing().a("Operation.").a(OperationUtils.toUcaseString(operation)).p().cn();
            }
        }
        codeBuilder.n();
        for (Map.Entry<Operation, Set<String>> entry : operationToPaths.entrySet()) {
            codeBuilder.sp().ma("OperationToPaths.put(").processing().a("Operation")
                    .d().a(OperationUtils.toUcaseString(entry.getKey())).cs().hashset("String").p().cn();
            for (String path : entry.getValue()) {
                codeBuilder.sp().ma("OperationToPaths.get(").processing().a("Operation").d().a(OperationUtils.toUcaseString(entry.getKey())).p()
                        .d().add().stringProvPath().a(path).Ppp().cn();
            }
        }
        codeBuilder.n();
        for (Map.Entry<String, PropertyInfo> entry : propertyToInfoMap.entrySet()) {
            codeBuilder.sp().ma("PropertyToPropertyManyness.put(").a(Utils.braque(entry.getKey())).a(".class").cs()
                    .processing().a("PropertyManyness").d().a(PropertyManynessUtils.toString(entry.getValue().propertyManyness)).p().cn();
        }
        codeBuilder.n();
        for (Map.Entry<String, PropertyInfo> entry : propertyToInfoMap.entrySet()) {
            codeBuilder.sp().ma("PropertyToPropertyName.put(").a(Utils.braque(entry.getKey())).a(".class").cs()
                    .stringProv().a("prop").a(entry.getValue().propertyName).Ppp().cn();
        }
        codeBuilder.n();
        for (Map.Entry<String, PropertyInfo> entry : propertyToInfoMap.entrySet()) {
            codeBuilder.sp().ma("PropertyNameToProperty.put(").stringProv().a("prop").a(entry.getValue().propertyName).Pp().cs()
                    .a(Utils.braque(entry.getKey())).a(".class").p().cn();
        }
        codeBuilder.n();
        for (Map.Entry<String, TypeTree> entry : typeTreeMap.entrySet()) {
            codeBuilder.sp().ma("TypeToTypeName.put(").a(Utils.braque(entry.getKey())).a(".class").cs()
                    .stringProv().a("type").a(Utils.last(entry.getKey())).Ppp().cn();
        }
        codeBuilder.n();
        for (Map.Entry<String, TypeTree> entry : typeTreeMap.entrySet()) {
            codeBuilder.sp().ma("TypeNameToType.put(").stringProv().a("type").a(Utils.last(entry.getKey())).Pp().cs()
                    .a(Utils.braque(entry.getKey())).a(".class").p().cn();
        }
        codeBuilder.n();
        for (Map.Entry<String, Set<String>> entry : pathInfoMap.entrySet()) {
            codeBuilder.sp().ma("PathToTypes.put(").stringProv().a("path").a(entry.getKey()).Pp().cs()
                    .hashset("Class<?>").p().cn();
            for (String type : entry.getValue()) {
                codeBuilder.sp().ma("PathToTypes.get(").stringProv().a("path").a(entry.getKey()).Ppp().d().add()
                        .a(type).a(".class").p().cn();
            }
        }
        PathInfoMap reversePathInfoMap = new PathInfoMap();
        for (Map.Entry<String, Set<String>> entry : pathInfoMap.entrySet()) {
            for (String val : entry.getValue()) {
                if (!reversePathInfoMap.containsKey(val)) {
                    reversePathInfoMap.put(val, new HashSet<String>());
                }
                reversePathInfoMap.get(val).add(entry.getKey());
            }
        }
        codeBuilder.n();
        for (Map.Entry<String, Set<String>> entry : reversePathInfoMap.entrySet()) {
            codeBuilder.sp().ma("TypeToPaths.put(").a(entry.getKey()).a(".class").cs()
                    .hashset("String").p().cn();
            for (String path : entry.getValue()) {
                codeBuilder.sp().ma("TypeToPaths.get(").a(entry.getKey()).a(".class").p().d()
                        .add().stringProv().a("path").a(path).Ppp().cn();
            }
        }
        codeBuilder.n();
        for (Map.Entry<String, TypeTree> entry : typeTreeMap.entrySet()) {
            if (!entry.getValue().mSubs.isEmpty()) {
                codeBuilder.sp().ma("TypeToSubTypes.put(").a(Utils.braque(entry.getKey())).d().a("class").cs()
                        .arraylist("Class<? extends " + Utils.BRAQUE + ".BraqueObject>").p().cn();
            }
            for (TypeTree subTypeTree : entry.getValue().mSubs) {
                codeBuilder.sp().ma("TypeToSubTypes.get(").a(Utils.braque(entry.getKey())).d().a("class").p()
                        .d().add().a(Utils.braque(subTypeTree.mBase.getQualifiedName().toString())).d().a("class").p().cn();
            }
        }
        codeBuilder.n();
        for (Map.Entry<String, TypeTree> entry : typeTreeMap.entrySet()) {
            for (TypeTree subTypeTree : entry.getValue().mSubs) {
                codeBuilder.sp().ma("TypeToSuperType.put(").a(Utils.braque(subTypeTree.mBase.getQualifiedName().toString())).d().a("class")
                        .cs().a(Utils.braque(entry.getKey())).d().a("class").p().cn();
            }
        }
        codeBuilder.spend().spend();
        Utils.write(Utils.STATIC_METHOD_CLASSES_PACKAGE + DOT + "Fanner", codeBuilder, processingEnvironment);
    }
}
