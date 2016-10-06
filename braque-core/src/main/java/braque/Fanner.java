package braque;

/**
 * Created by mikesolomon on 06/10/16.
 */
public interface Fanner {
    java.util.Map<Class<? extends braque.Prop>, java.util.Set<String> > propertyToPaths();
    java.util.Map<String, Class<? extends braque.Prop>> pathToProperty();
    java.util.Map<String, java.util.Set<braque.Operation> > pathToOperations();
    java.util.Map<braque.Operation, java.util.Set<String> > operationToPaths();
    java.util.Map<Class<? extends braque.Prop>, braque.PropertyManyness> propertyToPropertyManyness();
    java.util.Map<Class<? extends braque.Prop>, String> propertyToPropertyName();
    java.util.Map<String, Class<? extends braque.Prop>> propertyNameToProperty();
    java.util.Map<Class<? extends braque.BraqueObject>, String> typeToTypeName();
    java.util.Map<String, Class<? extends braque.BraqueObject>> typeNameToType();
    java.util.Map<Class<?>, java.util.Set<String>> typeToPaths();
    java.util.Map<String, java.util.Set<Class<?>>> pathToTypes();
    java.util.Set<Class<? extends braque.Prop>> uidProperties();
    java.util.Map<Class<? extends braque.BraqueObject>, java.util.List<Class<? extends braque.BraqueObject>>> typeToSubTypes();
    java.util.Map<Class<? extends braque.BraqueObject>, Class<? extends braque.BraqueObject>>  typeToSuperTypes();
}
