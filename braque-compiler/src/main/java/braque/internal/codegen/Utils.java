package braque.internal.codegen;

import braque.PropertyManyness;
import com.google.auto.common.MoreElements;
import com.google.auto.common.MoreTypes;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * Various utils for annotation processing.
 * Created by mikesolomon on 11/09/16.
 */

public class Utils {


    static final String SERIALIZED = "serialized";
    static final String SERIALIZE = "serialize";
    static final String DESERIALIZED = "deserialized";
    static final String DESERIALIZE = "deserialize";
    static final String PREVIOUSUIDS = "previousUIDs";
    static private ProcessingEnvironment processingEnvironment;
    static void setProcessingEnvironment (ProcessingEnvironment procEnv) {
        processingEnvironment = procEnv;
    }
    static void print(String m) {
        if (processingEnvironment != null) {
            processingEnvironment.getMessager().printMessage(Diagnostic.Kind.WARNING, m);
        }
    }
    static final String BASETYPE = "baseType";
    static final String IMPLEMENTATION = "Implementation";
    static final String ARGUMENT = "argument";
    static final String PROPERTIES = "properties";
    static private final String VALUE = "value";
    static private final String UID = "UID";
    static final String TYPE = "Type";
    static final String CLOJURE = "Clojure";
    static final String SHOW = "Show";
    static final String CREATE = "Create";
    static final String DESTROY = "Destroy";
    static final String UPDATE = "Update";
    static private final String PROPERTY = "Property";
    static private final String PROPERTYLIST = "PropertyList";
    static private final String PROPERTYSET = "PropertySet";
    static final String PROP = "Prop";
    static final String PROPLIST = "PropList";
    static final String PROPSET = "PropSet";
    static final String GET = "Get";
    static final String SET = "Set";
    static final String ADD = "Add";
    static final String REMOVE = "Remove";
    static final String CTOR = "Ctor";
    static final String BRAQUE = "braque";
    static final String DOT = ".";
    static final String DOLLAR = "$";
    static String STATIC_METHOD_CLASSES_PACKAGE = "braque.braqued";
    static String GENERATED_OBJECTS_PACKAGE_NAME = "braqued";

    static private void getSuperTypes(TypeTree tt, List<TypeElement> es) {
        if (tt.mSuper == null) {
            return;
        }
        es.add(tt.mSuper.mBase);
        getSuperTypes(tt.mSuper, es);
    }

    static TypeElement getSuperType(TypeTree tt) {
        if (tt.mSuper == null) {
            return tt.mBase;
        }
        return getSuperType(tt.mSuper);
    }

    static public List<TypeElement> getClassHierarchy(TypeTree tt) {
        if (tt.mBase == null) {
            return null;
        }
        List<TypeElement> out = new ArrayList<>();
        out.add(tt.mBase);
        getSuperTypes(tt, out);
        return out;
    }

    static void getSubTypes(TypeTree tt, Map<TypeElement, List<TypeElement>> es) {
        List<TypeElement> subelts = new ArrayList<>();
        for (TypeTree sub : tt.mSubs) {
            subelts.add(sub.mBase);
        }
        es.put(tt.mBase, new ArrayList<>(subelts));
        for (TypeTree sub : tt.mSubs) {
            getSubTypes(sub, es);
        }
    }

    static Set<TypeElement> getTerminalSubTypes(TypeTree tt) {
        Set<TypeElement> es = new HashSet<>();
        getTerminalSubTypes(tt, es);
        return es;
    }

    static void getTerminalSubTypes(TypeTree tt, Set<TypeElement> es) {
        if (tt.mSubs.isEmpty()) {
            es.add(tt.mBase);
        }
        for (TypeTree sub : tt.mSubs) {
            getTerminalSubTypes(sub, es);
        }
    }

    static List<TypeElement> getSuperAndSubTypes(TypeTree tt) {
        List<TypeElement> out = new ArrayList<>();
        out.add(tt.mBase);
        getSuperTypes(tt, out);
        Map<TypeElement, List<TypeElement>> es = new HashMap<>();
        getSubTypes(tt, es);
        out.addAll(es.keySet());
        return new ArrayList<>(new HashSet<>(out));
    }

    static boolean propertyImplementedBy(TypeTree tt, TypeElement property) {
        List<TypeElement> types = new ArrayList<>();
        TypeElement baseElement = getPropertyAttachment(property);
        types.add(baseElement);
        getSuperTypes(tt, types);
        for (TypeElement typeElement : types) {
            if (tt.mBase.getQualifiedName().toString().equals((typeElement.getQualifiedName().toString()))) {
                return true;
            }
        }
        return false;
    }

    static void write(String qualifiedClass, CodeBuilder builder, ProcessingEnvironment processingEnvironment) {
        try {
            JavaFileObject source = processingEnvironment.getFiler().createSourceFile(qualifiedClass);
            Writer writer = source.openWriter();
            writer.write(builder.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new BraqueException(e);
        }
    }

    static String braque(String toBraque) {
        String[] returnTypeSplit = toBraque.split("\\.");
        String returnType = "";
        for (int i = 0; i < returnTypeSplit.length - 1; i++) {
            returnType += returnTypeSplit[i] + DOT;
        }
        returnType += GENERATED_OBJECTS_PACKAGE_NAME + DOT;
        returnType += returnTypeSplit[returnTypeSplit.length - 1];
        return returnType;
    }

    static String unbraque(String toUnbraque) {
        String[] returnTypeSplit = toUnbraque.split("\\.");
        String returnType = "";
        for (int i = 0; i < returnTypeSplit.length - 2; i++) {
            returnType += returnTypeSplit[i] + DOT;
        }
        returnType += returnTypeSplit[returnTypeSplit.length - 1];
        return returnType;
    }

    static String last(String s) {
        String[] split = s.split("\\.");
        return split[split.length - 1];
    }

    static boolean isType(AnnotationMirror annotationMirror) {
        return annotationMirrorIs(TYPE, BRAQUE, annotationMirror);
    }

    static boolean isProperty(AnnotationMirror annotationMirror) {
        return annotationMirrorIs(PROPERTY, BRAQUE, annotationMirror);
    }

    static boolean isPropertyList(AnnotationMirror annotationMirror) {
        return annotationMirrorIs(PROPERTYLIST, BRAQUE, annotationMirror);
    }

    static boolean isPropertySet(AnnotationMirror annotationMirror) {
        return annotationMirrorIs(PROPERTYSET, BRAQUE, annotationMirror);
    }

    static boolean isShow(AnnotationMirror annotationMirror) {
        return annotationMirrorIs(SHOW, BRAQUE, annotationMirror);
    }

    static boolean isCreate(AnnotationMirror annotationMirror) {
        return annotationMirrorIs(CREATE, BRAQUE, annotationMirror);
    }

    static boolean isUpdate(AnnotationMirror annotationMirror) {
        return annotationMirrorIs(UPDATE, BRAQUE, annotationMirror);
    }

    static boolean isDestroy(AnnotationMirror annotationMirror) {
        return annotationMirrorIs(DESTROY, BRAQUE, annotationMirror);
    }

    static private boolean annotationMirrorIs(String s, String pkg, AnnotationMirror annotationMirror) {
        return s.equals(MoreTypes.asElement(annotationMirror.getAnnotationType()).getSimpleName().toString())
                && pkg.equals(getPackage(MoreTypes.asElement(annotationMirror.getAnnotationType())));
    }

    static String getAnnotationMirrorValueAsQualifiedName(AnnotationMirror annotationMirror) {
        return getAnnotationMirrorValueAsTypeElement(annotationMirror).getQualifiedName().toString();
    }

    static TypeElement getUIDofProperty(AnnotationMirror annotationMirror) {
        return getUID(getAnnotationMirrorValueAsTypeElement(annotationMirror));
    }

    static TypeElement getUID(TypeElement typeElement) {
        PackageElement pkg = MoreElements.getPackage(typeElement);
        TypeElement uidElement = getUIDFromPackage(pkg);
        if (uidElement == null) {
            TypeElement type = getTypeFromPackage(pkg);
            for (TypeElement inAnnotation : getAnnotationMirrorValueArrayAsTypeElementList(getTypeAnnotation(type))) {
                if (hasTypeAnnotation(inAnnotation)) {
                    return getUID(inAnnotation);
                }
            }
            return null;
        } else {
            return uidElement;
        }
    }

    static private AnnotationValue getAnnotationValue(AnnotationMirror annotationMirror, String methodName) {
        AnnotationValue value = null;
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()) {
            if (methodName.equals(entry.getKey().getSimpleName().toString())) {
                value = entry.getValue();
                break;
            }
        }
        return value;
    }

    static TypeElement getAnnotationMirrorValueAsTypeElement(AnnotationMirror annotationMirror, String methodName) {
        AnnotationValue value = getAnnotationValue(annotationMirror, methodName);
        return value == null ? null : MoreTypes.asTypeElement((TypeMirror) value.getValue());
    }

    static String getAnnotationMirrorValueAsString(AnnotationMirror annotationMirror, String methodName) {
        AnnotationValue value = getAnnotationValue(annotationMirror, methodName);
        return value == null ? null : (String) value.getValue();
    }

    static String getAnnotationMirrorValueAsString(AnnotationMirror annotationMirror) {
        return getAnnotationMirrorValueAsString(annotationMirror, VALUE);
    }

    static TypeElement getAnnotationMirrorValueAsTypeElement(AnnotationMirror annotationMirror) {
        return getAnnotationMirrorValueAsTypeElement(annotationMirror, VALUE);
    }

    static  <T extends Element> boolean hasTypeAnnotation(T element) {
        return genericGetAnnotation(element, TYPE) != null;
    }

    static boolean hasUIDAnnotation(TypeElement element) {
        return genericGetAnnotation(element, UID) != null;
    }

    static <T extends Element> AnnotationMirror genericGetAnnotation(T element, String ann) {
        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            if (annotationMirrorIs(ann, BRAQUE, annotationMirror)) {
                return annotationMirror;
            }
        }
        return null;
    }

    static TypeElement getPropertyTypeElement(TypeElement element) {
        return getAnnotationMirrorValueAsTypeElement(getPropertyAnnotation(element));
    }

    static PropertyManyness getPropertyType(AnnotationMirror annotationMirror) {
        if (isPropertySet(annotationMirror)) {
            return PropertyManyness.SET;
        }
        if (isPropertyList(annotationMirror)) {
            return PropertyManyness.LIST;
        }
        if (isProperty(annotationMirror)) {
            return PropertyManyness.SIMPLE;
        }
        return null;
    }

    static AnnotationMirror getPropertyAnnotation(TypeElement element) {
        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            if (isProperty(annotationMirror) || isPropertySet(annotationMirror) || isPropertyList(annotationMirror)) {
                return annotationMirror;
            }
        }
        throw new IllegalStateException("not annotated with a property");
    }

    static AnnotationMirror getShowAnnotation(TypeElement element) {
        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            if (isShow(annotationMirror)) {
                return annotationMirror;
            }
        }
        throw new IllegalStateException("not annotated with a property");
    }

    static AnnotationMirror getUpdateAnnotation(TypeElement element) {
        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            if (isUpdate(annotationMirror)) {
                return annotationMirror;
            }
        }
        throw new IllegalStateException("not annotated with a property");
    }
    static AnnotationMirror getCreateAnnotation(TypeElement element) {
        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            if (isCreate(annotationMirror)) {
                return annotationMirror;
            }
        }
        throw new IllegalStateException("not annotated with a property");
    }

    static AnnotationMirror getDestroyAnnotation(TypeElement element) {
        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            if (isDestroy(annotationMirror)) {
                return annotationMirror;
            }
        }
        throw new IllegalStateException("not annotated with a property");
    }

    static List<TypeElement> getAnnotationMirrorValueArrayAsTypeElementList(AnnotationMirror annotationMirror) {
        return getAnnotationMirrorValueArrayAsTypeElementList(annotationMirror, VALUE);
    }

    static List<TypeElement> getAnnotationMirrorValueArrayAsTypeElementList(AnnotationMirror annotationMirror, String methodName) {
        AnnotationValue value = null;
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()) {
            if (methodName.equals(entry.getKey().getSimpleName().toString())) {
                value = entry.getValue();
                break;
            }
        }
        List valueMirrored = (value == null) ? new ArrayList() : (List) value.getValue();
        List<TypeElement> out = new ArrayList<>();
        for (Object o : valueMirrored) {
            out.add(MoreTypes.asTypeElement((TypeMirror)((AnnotationValue) o).getValue()));
        }
        return out;
    }

    static String getPackage(Element element) {
        return MoreElements.getPackage(element).getQualifiedName().toString();
    }

    static TypeElement getTypeFromPackage(PackageElement packageElement) {
        for (Element element : packageElement.getEnclosedElements()) {
            if (hasTypeAnnotation(MoreElements.asType(element))) {
                return MoreElements.asType(element);
            }
        }
        return null;
    }

    static private TypeElement getUIDFromPackage(PackageElement packageElement) {
        for (Element element : packageElement.getEnclosedElements()) {
            if (hasUIDAnnotation(MoreElements.asType(element))) {
                return MoreElements.asType(element);
            }
        }
        return null;
    }

    static TypeElement getPropertyAttachment(TypeElement typeElement) {
        PackageElement packageElement = MoreElements.getPackage(typeElement);
        return getTypeFromPackage(packageElement);
    }

    static boolean isComplexProperty(TypeElement typeElement) {
        return hasTypeAnnotation(getAnnotationMirrorValueAsTypeElement(getPropertyAnnotation(typeElement)));
    }

    static boolean isComplexProperty(String s) {
        String hack = "java.lang";
        return s != null && !s.substring(0,hack.length()).equals(hack);
    }

    static <T extends Element> AnnotationMirror getTypeAnnotation(T element) {
        return genericGetAnnotation(element, TYPE);
    }

    static public AnnotationMirror getClojureAnnotation(TypeElement typeElement) {
        return genericGetAnnotation(typeElement, CLOJURE);
    }

    static List<TypeElement> getNecessaryElementsAtThisLevel(TypeTree typeTree) {
        List<TypeElement> out = new ArrayList<>();
        for (TypeElement e : getAnnotationMirrorValueArrayAsTypeElementList(getTypeAnnotation(typeTree.mBase))) {
            // we only want properties, so we weed out anything with a type annotation
            if (!hasTypeAnnotation(e)) {
                out.add(e);
            }
        }
        return out;
    }

    static private void getSuperNecessaryElements(TypeTree typeTree, List<TypeElement> out) {
        if (typeTree.mSuper != null) {
            out.addAll(getNecessaryElementsAtThisLevel(typeTree.mSuper));
            getSuperNecessaryElements(typeTree.mSuper, out);
        }
    }

    static private void getSubNecessaryElements(TypeTree typeTree, List<TypeElement> out) {
        for (TypeTree typeTree1 : typeTree.mSubs) {
            out.addAll(getNecessaryElementsAtThisLevel(typeTree1));
            getSubNecessaryElements(typeTree1, out);
        }
    }

    static List<TypeElement> getAllNecessaryElements(TypeTree typeTree) {
        List<TypeElement> out = new ArrayList<>();
        out.addAll(getNecessaryElementsAtThisLevel(typeTree));
        getSuperNecessaryElements(typeTree, out);
        getSubNecessaryElements(typeTree, out);
        return out;
    }

    static List<TypeElement> getNecessaryElementsAtThisLevelAndSuper(TypeTree typeTree) {
        List<TypeElement> out = new ArrayList<>();
        out.addAll(getNecessaryElementsAtThisLevel(typeTree));
        getSuperNecessaryElements(typeTree, out);
        return out;
    }

    static Map<String, TypeTree> buildTypeTree(Collection<? extends Element> typeElements) {
        Map<String, TypeTree> typeTreeMap = new HashMap<>();
        for (Element element : typeElements) {
            typeTreeMap.put(MoreElements.asType(element).getQualifiedName().toString(), new TypeTree(MoreElements.asType(element)));
        }
        Map<String, List<TypeElement>> subMap = new HashMap<>();
        for (Element element : typeElements) {
            TypeElement typeElement = MoreElements.asType(element);
            List<TypeElement> superEltCandidates = getAnnotationMirrorValueArrayAsTypeElementList(getTypeAnnotation(typeElement));
            for (TypeElement superElt: superEltCandidates) {
                if (superElt != null && hasTypeAnnotation(superElt)) {
                    typeTreeMap.get(typeElement.getQualifiedName().toString()).mSuper = typeTreeMap.get(superElt.getQualifiedName().toString());
                    if (!subMap.containsKey(superElt.getQualifiedName().toString())) {
                        subMap.put(superElt.getQualifiedName().toString(), new ArrayList<TypeElement>());
                    }
                    subMap.get(superElt.getQualifiedName().toString()).add(typeElement);
                    break;
                }
            }
        }
        for (String superElt : subMap.keySet()) {
            for (TypeElement typeElement : subMap.get(superElt)) {
                typeTreeMap.get(superElt).mSubs.add(typeTreeMap.get(typeElement.getQualifiedName().toString()));
            }
        }
        return typeTreeMap;
    }
}
