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
        codeBuilder.pkg(Utils.STATIC_METHOD_CLASSES_PACKAGE).pubic().klass().as("Fanner").impl().processing().as("Fanner").sbeg()
                .a("  static private final java.util.Map<Class<? extends braque.Prop>, java.util.Set<String> > mPropertyToPaths = new java.util.HashMap<>();").n()
                        .a("  static private final java.util.Map<String, Class<? extends braque.Prop>> mPathToProperty = new java.util.HashMap<>();").n()
                        .a("  static private final java.util.Map<String, java.util.Set<braque.Operation> > mPathToOperations = new java.util.HashMap<>();").n()
                        .a("  static private final java.util.Map<braque.Operation, java.util.Set<String> > mOperationToPaths = new java.util.HashMap<>();").n()
                        .a("  static private final java.util.Map<Class<? extends braque.Prop>, braque.PropertyManyness> mPropertyToPropertyManyness = new java.util.HashMap<>();").n()
                        .a("  static private final java.util.Map<Class<? extends braque.Prop>, String> mPropertyToPropertyName = new java.util.HashMap<>();").n()
                        .a("  static private final java.util.Map<String, Class<? extends braque.Prop>> mPropertyNameToProperty = new java.util.HashMap<>();").n()
                        .a("  static private final java.util.Map<Class<? extends braque.BraqueObject>, String> mTypeToTypeName = new java.util.HashMap<>();").n()
                        .a("  static private final java.util.Map<String, Class<? extends braque.BraqueObject>> mTypeNameToType = new java.util.HashMap<>();").n()
                        .a("  static private final java.util.Map<Class<?>, java.util.Set<String>> mTypeToPaths = new java.util.HashMap<>();").n()
                        .a("  static private final java.util.Map<String, java.util.Set<Class<?>>> mPathToTypes = new java.util.HashMap<>();").n()
                        .a("  static private final java.util.Set<Class<? extends braque.Prop>> mUIDProperties = new java.util.HashSet<>();").n()
                        .a("  static private final java.util.Map<Class<? extends braque.BraqueObject>, java.util.List<Class<? extends braque.BraqueObject>>> mTypeToSubTypes = new java.util.HashMap<>();").n()
                        .a("  static private final java.util.Map<Class<? extends braque.BraqueObject>, Class<? extends braque.BraqueObject>> mTypeToSuperType = new java.util.HashMap<>();").n()
                        .a("  static public final java.util.Map<Class<? extends braque.Prop>, java.util.Set<String> > _propertyToPaths() {").n()
                        .a("    return mPropertyToPaths;").n()
                        .a("  }").n()
                        .a("  static public final java.util.Map<String, Class<? extends braque.Prop>> _pathToProperty() {").n()
                        .a("    return mPathToProperty;").n()
                        .a("  }").n()
                        .a("  static public final java.util.Map<String, java.util.Set<braque.Operation> > _pathToOperations() {").n()
                        .a("    return mPathToOperations;").n()
                        .a("  }").n()
                        .a("  static public final java.util.Map<braque.Operation, java.util.Set<String> > _operationToPaths() {").n()
                        .a("    return mOperationToPaths;").n()
                        .a("  }").n()
                        .a("  static public final java.util.Map<Class<? extends braque.Prop>, braque.PropertyManyness> _propertyToPropertyManyness() {").n()
                        .a("    return mPropertyToPropertyManyness;").n()
                        .a("  }").n()
                        .a("  static public final java.util.Map<Class<? extends braque.Prop>, String> _propertyToPropertyName() {").n()
                        .a("    return mPropertyToPropertyName;").n()
                        .a("  }").n()
                        .a("  static public final java.util.Map<String, Class<? extends braque.Prop>> _propertyNameToProperty() {").n()
                        .a("    return mPropertyNameToProperty;").n()
                        .a("  }").n()
                        .a("  static public final java.util.Map<Class<? extends braque.BraqueObject>, String> _typeToTypeName() {").n()
                        .a("    return mTypeToTypeName;").n()
                        .a("  }").n()
                        .a("  static public final java.util.Map<String, Class<? extends braque.BraqueObject>> _typeNameToType() {").n()
                        .a("    return mTypeNameToType;").n()
                        .a("  }").n()
                        .a("  static public final java.util.Map<Class<?>, java.util.Set<String>> _typeToPaths() {").n()
                        .a("    return mTypeToPaths;").n()
                        .a("  }").n()
                        .a("  static public final java.util.Map<String, java.util.Set<Class<?>>> _pathToTypes() {").n()
                        .a("    return mPathToTypes;").n()
                        .a("  }").n()
                        .a("  static public final java.util.Set<Class<? extends braque.Prop>> _uidProperties() {").n()
                        .a("    return mUIDProperties;").n()
                        .a("  }").n()
                        .a("  static public final java.util.Map<Class<? extends braque.BraqueObject>, java.util.List<Class<? extends braque.BraqueObject>>> _typeToSubTypes() {").n()
                        .a("    return mTypeToSubTypes;").n()
                        .a("  }").n()
                        .a("  static public final java.util.Map<Class<? extends braque.BraqueObject>, Class<? extends braque.BraqueObject>>  _typeToSuperTypes() {").n()
                        .a("    return mTypeToSuperType;").n()
                        .a("  }").n()
                        .a("  @Override").n().a("  public final java.util.Map<Class<? extends braque.Prop>, java.util.Set<String> > propertyToPaths() {").n()
                        .a("    return _propertyToPaths();").n()
                        .a("  }").n()
                        .a("  @Override").n().a("  public final java.util.Map<String, Class<? extends braque.Prop>> pathToProperty() {").n()
                        .a("    return _pathToProperty();").n()
                        .a("  }").n()
                        .a("  @Override").n().a("  public final java.util.Map<String, java.util.Set<braque.Operation> > pathToOperations() {").n()
                        .a("    return _pathToOperations();").n()
                        .a("  }").n()
                        .a("  @Override").n().a("  public final java.util.Map<braque.Operation, java.util.Set<String> > operationToPaths() {").n()
                        .a("    return _operationToPaths();").n()
                        .a("  }").n()
                        .a("  @Override").n().a("  public final java.util.Map<Class<? extends braque.Prop>, braque.PropertyManyness> propertyToPropertyManyness() {").n()
                        .a("    return _propertyToPropertyManyness();").n()
                        .a("  }").n()
                        .a("  @Override").n().a("  public final java.util.Map<Class<? extends braque.Prop>, String> propertyToPropertyName() {").n()
                        .a("    return _propertyToPropertyName();").n()
                        .a("  }").n()
                        .a("  @Override").n().a("  public final java.util.Map<String, Class<? extends braque.Prop>> propertyNameToProperty() {").n()
                        .a("    return _propertyNameToProperty();").n()
                        .a("  }").n()
                        .a("  @Override").n().a("  public final java.util.Map<Class<? extends braque.BraqueObject>, String> typeToTypeName() {").n()
                        .a("    return _typeToTypeName();").n()
                        .a("  }").n()
                        .a("  @Override").n().a("  public final java.util.Map<String, Class<? extends braque.BraqueObject>> typeNameToType() {").n()
                        .a("    return _typeNameToType();").n()
                        .a("  }").n()
                        .a("  @Override").n().a("  public final java.util.Map<Class<?>, java.util.Set<String>> typeToPaths() {").n()
                        .a("    return _typeToPaths();").n()
                        .a("  }").n()
                        .a("  @Override").n().a("  public final java.util.Map<String, java.util.Set<Class<?>>> pathToTypes() {").n()
                        .a("    return _pathToTypes();").n()
                        .a("  }").n()
                        .a("  @Override").n().a("  public final java.util.Set<Class<? extends braque.Prop>> uidProperties() {").n()
                        .a("    return _uidProperties();").n()
                        .a("  }").n()
                        .a("  @Override").n().a("  public final java.util.Map<Class<? extends braque.BraqueObject>, java.util.List<Class<? extends braque.BraqueObject>>> typeToSubTypes() {").n()
                        .a("    return _typeToSubTypes();").n()
                        .a("  }").n()
                        .a("  @Override").n().a("  public final java.util.Map<Class<? extends braque.BraqueObject>, Class<? extends braque.BraqueObject>>  typeToSuperTypes() {").n()
                        .a("    return _typeToSuperTypes();").n()
                        .a("  }").n()
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
