package braque.internal.codegen;

import braque.Operation;
import braque.PropertyManyness;

import com.google.auto.common.MoreElements;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import static braque.internal.codegen.Utils.*;

/**
 * Created by mikesolomon on 11/09/16.
 * Current difficulty is that if we have avatar as a subclass, we won't know how to pick it up
 * this comes from the fact that it will consider it as belonging to the super
 * one way to rule this out is to make sure that the super has not processed this yet
 */

abstract class AbstractRestHandler {

    final private RandGen randGen = new RandGen();

    final private String mOp;

    AbstractRestHandler(String op) {
        mOp = op;
    }

    private boolean shouldInludeSetter() {
        switch (mOp) {
            case Utils.CREATE:
            case Utils.UPDATE:
                return true;
            case Utils.DESTROY:
            case Utils.SHOW:
            default:
                return false;
        }
    }

    public abstract boolean isVisitable(AnnotationMirror annotationMirror);

    public abstract String getRestInterface();

    static private String typeFromStringAndAnnotationMirror(String s, AnnotationMirror a) {
        if (Utils.isProperty(a)) {
            return s;
        } else if (Utils.isPropertyList(a)) {
            return "java.util.List<"+s+">";
        } else if (Utils.isPropertySet(a)) {
            return "java.util.Set<"+s+">";
        }
        throw new IllegalArgumentException("unknown annotation value - neither property, list nor set");
    }

    private String typeFromPair(TypeElementAnnotationMirrorPair pair) {
        return typeFromStringAndAnnotationMirror(pair.getKey(), pair.getValue());
    }

    static private boolean isOp(String s, String op) {
        String base = s.split("<")[0];
        return op.equals(base.substring(base.length() - op.length(), base.length()));
    }

    static private boolean isGet(String s) {
        return isOp(s, Utils.GET);
    }

    static private boolean isSet(String s) {
        return isOp(s, Utils.SET);
    }

    static private boolean isAdd(String s) {
        return isOp(s, Utils.ADD);
    }

    static private boolean isRemove(String s) {
        return isOp(s, Utils.REMOVE);
    }

    static private boolean isCtor(String s) {
        return isOp(s, Utils.CTOR);
    }

    /**
     * Reverse engineers a get, set, add, remove or ctor interface.
     * For example, if we have getFoo, this will return Foo
     * @param s
     * @return
     */
    static private String getBase(String s) {
        String base = s.split("<")[0];
        if (isGet(s)) {
            return base.substring(0, base.length() - Utils.GET.length());
        } else if (isSet(s)) {
            return base.substring(0, base.length() - Utils.SET.length());
        } else if (isAdd(s)) {
            return base.substring(0, base.length() - Utils.ADD.length());
        } else if (isRemove(s)) {
            return base.substring(0, base.length() - Utils.REMOVE.length());
        } else if (isCtor(s)) {
            return base.substring(0, base.length() - Utils.CTOR.length());
        }

        throw new IllegalStateException(" cannot parse base of "+s);
    }

    static private String packagize(String toPackagize, String pkg) {
        return pkg+ Utils.DOT+toPackagize.replace(" ","").replace("<","<"+pkg+ Utils.DOT).replace(",",","+pkg+ Utils.DOT);
    }


    /**
     * Makes a single property in the serializer
     * @param serializer A serializer code builder
     * @param entry Maps the property to its type
     * @param pkg The package of the element holding this property.
     * @param functionSuffix If we are dealing with a complex property, the suffix of the function that we will
     *                       call to treat the property.
     * @param referenceName A path for the string provisioner.
     */
    static private void makeSerializerProperty(CodeBuilder serializer,
                                               Map.Entry<String, TypeElementAnnotationMirrorPair> entry,
                                               String pkg, String functionSuffix, String referenceName) {
        String left = entry.getValue().getLeft();
        String typeName = Utils.isComplexProperty(left) ? packagize(left, pkg) : left;
        String unbraquedBaseTypeName = Utils.getAnnotationMirrorValueAsQualifiedName(entry.getValue().getRight());
        String propName = Utils.last(entry.getKey());
        String braquedPropName = Utils.braque(entry.getKey());
        AnnotationMirror annotationMirror = entry.getValue().getRight();
        String typeBraqued = Utils.braque(unbraquedBaseTypeName);
        serializer.sp().iff().as(DESERIALIZED).instof().a(braquedPropName).a(Utils.GET).psbeg();
        if (!Utils.isProperty(annotationMirror)) {
            if (Utils.isPropertyList(annotationMirror)) {
                serializer.sp().as("int")._a("idx").as(propName).eq().a("0").cn();
            }
            serializer.sp().four().as(isComplexProperty(unbraquedBaseTypeName)
                    ? typeBraqued
                    : unbraquedBaseTypeName)
                    ._a_(propName).as("elt").col().PP().a(braquedPropName).a(Utils.GET);
            if (Utils.isComplexProperty(left)) {
                serializer.Gg(typeBraqued);
            }
            serializer.p().a(DESERIALIZED).pd().get(propName).psbeg();
            if (Utils.isComplexProperty(left)) {
                serializer.sp().iff()._a_(propName).as("elt").instof().a(typeName).psbeg();
                if (Utils.isPropertySet(annotationMirror)) {
                    TypeElement uid = Utils.getUIDofProperty(annotationMirror);
                    serializer.sp().str()._as("id").eq().PP().a(typeName).p()._a_(propName).a("elt").pd().get(uid).cn();
                }
                serializer.sp().sojmap()._as(propName).eq().a(SERIALIZE).a(functionSuffix).a(propName)
                        .PPP().a(typeName).p()._a_(propName).a("elt").p().cs().a("HelperMethods.append")
                        .P().a(PREVIOUSUIDS).cs()._a(Utils.isPropertySet(annotationMirror) ? "id": "idx"+propName+"++").pp().cn()
                        .sp().a("out").d().a("putAll").P()._a(propName).pcn()
                        .spend();
            } else {
                // under current system, anything that's not a complex property can only be in a list
                //          out.put(HelperMethods.addUIDs(braque.braqued.StringProvisioner.pathPlayerInfoNumbersWorn(), append(previousUIDs, _idxNumbersWorn++)), _NumbersWorn_elt);

                serializer.sp().a("out").d().a("put")
                        .P().a(ADDUIDS).P().stringProvPath().a(referenceName).a(propName)
                        .Pp().cs().a(PREVIOUSUIDS).p().a("+\"/\"+(")._a("idx").a(propName).a("++").p().cs()._a_(propName).a("elt").p().cn();
            }
            serializer.spend();
        } else {
            if (Utils.isComplexProperty(left)) {
                serializer.sp().as(typeBraqued).__as(propName).eq()
                        .PP().a(braquedPropName).a(Utils.GET).p().a(DESERIALIZED).pd().get(propName).cn()
                        .sp().iff().__as(propName).instof().a(typeName).psbeg()
                        .sp().sojmap()._as(propName).eq().a(SERIALIZE).a(functionSuffix).a(propName)
                        .PPP().a(typeName).p().__a(propName).p().cs().a(PREVIOUSUIDS).pcn()
                        .sp().a("out").d().a("putAll").P()._a(propName).pcn().spend();
            } else {
                serializer.sp().a("out").d().a("put")
                        .P().a(ADDUIDS).P().stringProvPath().a(referenceName).a(propName)
                        .Pp().cs().a(PREVIOUSUIDS).p().cs()
                        .PP().a(braquedPropName).a(Utils.GET).p().a(DESERIALIZED).pd().get(propName).pcn();
            }
        }
        serializer.spend();
    }

    /**
     * Adds a property to the deserializer.
     * @param deserializer The deserializer code builder.
     * @param entry Maps a property to its type.
     * @param pkg The package of the element we will be returning.
     * @param functionSuffix If we are treating a compelx object, the suffix of the function we will
     *                       need to call to deserialize it.
     * @param pathReferenceName Used to call up the right path by the string provisioner.
     * @param deserializerPropNames Collects property names passed to the deserializer for future use.
     */
    static private void makeDeserializerProperty(CodeBuilder deserializer,
                                                 Map.Entry<String, TypeElementAnnotationMirrorPair> entry,
                                                 String pkg, String functionSuffix, String pathReferenceName,
                                                 List<String> deserializerPropNames) {
        String left = entry.getValue().getLeft();
        String typeName = Utils.isComplexProperty(left) ? packagize(left, pkg) : left;
        String propName = Utils.last(entry.getKey());
        AnnotationMirror annotationMirror = entry.getValue().getRight();
        deserializerPropNames.add("_"+propName);
        if (!Utils.isProperty(annotationMirror)) {
            deserializer.sp().jlist(typeName).__as(propName).eq().arraylist().cn();
            String type = typeFromStringAndAnnotationMirror(typeName, entry.getValue().getRight());
            if (Utils.isPropertySet(annotationMirror)) {
                // first one has to be a list so that we can iterate over it with an index
                deserializer.sp().as(type)._as(propName).eq().hashset().cn();
            } else if (Utils.isPropertyList(annotationMirror)) {
                deserializer.sp().as(type)._as(propName).eq().arraylist().cn();
            }
            deserializer.sp().sjlist()._a("valid").as(propName).eq().arraylist().cn()
                    .sp().str()._a("base").as(propName).eq().a(ADDUIDS)
                    .P().stringProvPath().a(pathReferenceName).a(propName).Pp().cs().a(PREVIOUSUIDS).pcn()
                    .sp().a("for (String path : ").a(SERIALIZED).a(".keySet())").sbeg()
                    .sp().iff()._a("base").a(propName).a(".length() <= path.length() ")
                    .and()._a("base").a(propName).a(".equals(path.substring(0, _base").a(propName).d().a("length")
                    .Ppppp().sbeg()
                    .sp()._a("valid").a(propName).dadd().a("path").pcn().spend().spend()
                    .sp().a("HelperMethods.sortIfIntegersElseLeaveInRandomOrder").P()._a("valid").a(propName).pcn()
                    .sp().a("for (String path : _valid").a(propName).psbeg();
            if (Utils.isComplexProperty(left)) {
                deserializer.sp().a("String[] split = path.substring(_base").a(propName)
                        .a(".length() + 1).split(\"/\")").cn()
                        .sp().a("String maybeUID = split.length > 0 ? split[0] : null").cn()
                        .sp().a("if (maybeUID != null)").sbeg()
                        .sp().__a(propName).dadd().a(DESERIALIZE).a(functionSuffix).a(propName)
                        .a("(serialized, HelperMethods.append(previousUIDs, maybeUID), false))").cn()
                        .spend().spend();

            } else {
                deserializer.sp().as(typeName).as("__elt").eq().a("HelperMethods.safeCast").a(Utils.last(left)).a("(serialized.get(path))").cn()
                        .sp().iff().as("__elt").neq().nul().psbeg()
                        .sp().a("serialized.remove(path)").cn()
                        .sp().__a(propName).a(".add(__elt)").cn().spend().spend();
            }
            deserializer.sp().a("for (int i = 0; i < __").a(propName).a(".size(); i++)").sbeg()
                    .sp().iff().__a(propName).a(".get(i) != null)").sbeg()
                    .sp()._a(propName).dadd().__a(propName).a(".get(i))").cn().spend()
                    .spend().sp().iff()._a(propName).a(".isEmpty())").sbeg()
                    .sp()._as(propName).eq().nul().cn().spend();
        } else {
            deserializer.sp().as(typeFromStringAndAnnotationMirror(typeName, annotationMirror))._as(propName).eq();
            if (Utils.isComplexProperty(left)) {
                deserializer.a("deserialize").a(functionSuffix).a(propName).a("(serialized, previousUIDs, false)").cn();
            } else {
                deserializer.a("HelperMethods.safeCast").a(Utils.last(left)).a("(serialized.get(HelperMethods.addUIDs(")
                        .stringProvPath().a(pathReferenceName).a(propName)
                        .Pp().cs().a(PREVIOUSUIDS).ppp().cn()
                        .sp().iff()._as(propName).neq().nul().psbeg()
                        .sp().a("serialized.remove(").a("HelperMethods.addUIDs(").stringProvPath().a(pathReferenceName).a(propName)
                        .Pp().cs().a(PREVIOUSUIDS).pp().cn().spend();
            }
        }
    }

    static private List<String> append(List<String> list, String s) {
        List<String> out = new ArrayList<String>(list);
        out.add(s);
        return out;
    }

    /**
     * Actual function recursed over, called by itself and by the entry version of
     * recurseOverUseablePropertyNames.
     * @param baseIfaceName The base interface name that we will be returning.
     * @param trailingName Anything trailing this name.
     * @param necessaryPropertyNames Necessary properties to feed to the constructor
     * @param useablePropertyNames Properties that will be part of the power set.
     * @param usedPropertyNames Property names that we have actually used, picked up
     *                          in the recursion.
     * @return
     */
    static private String recurseOverUseablePropertyNames(String baseIfaceName,
                                                          String trailingName,
                                                          List<String> necessaryPropertyNames,
                                                          List<String> useablePropertyNames,
                                                          List<String> usedPropertyNames) {
        if (useablePropertyNames.isEmpty()) {
            List<String> usedPropertyNamesWithUnderscore = new ArrayList<String>();
            for (String s : necessaryPropertyNames) {
                usedPropertyNamesWithUnderscore.add(s);
            }
            for (String s : usedPropertyNames) {
                usedPropertyNamesWithUnderscore.add("_"+s);
            }
            Collections.sort(usedPropertyNames);
            Collections.sort(usedPropertyNamesWithUnderscore);
            String objName = new CB().a(trailingName).a_(usedPropertyNames)._a(Utils.IMPLEMENTATION).toS();
            return new CB().knew().braqued().a("HelperMethods.ScoredReturnType").Gg(baseIfaceName)
                    .P().knew().a(objName)
                    .P().acs(usedPropertyNamesWithUnderscore).p().cs().a(""+usedPropertyNames.size()).p().toS();
        }
        String out = "";
        int i = 1;
        for (String useablePropertyName : useablePropertyNames) {
            out += new CB(2,6)._as(useablePropertyName).neq().nul().nsp().a("? ")
                    .a(recurseOverUseablePropertyNames(baseIfaceName, trailingName, necessaryPropertyNames,
                            useablePropertyNames.subList(i++, useablePropertyNames.size()),
                            append(usedPropertyNames, useablePropertyName)))
                    .nsp().a(": ").toS();
        }
        out += recurseOverUseablePropertyNames(baseIfaceName, trailingName,
                necessaryPropertyNames, new ArrayList<String>(), usedPropertyNames);
        return out;
    }

    /**
     * Entry method to recurse over useable properties by the deserializer scored
     * return types function.
     * @param baseIfaceName The base interface name that we will be returning.
     * @param trailingName Anything trailing this name.
     * @param necessaryPropertyNames Necessary properties to feed to the constructor
     * @param useablePropertyNames Properties that will be part of the power set.
     * @return
     */
    static private String recurseOverUseablePropertyNames(String baseIfaceName,
                                                          String trailingName,
                                                          List<String> necessaryPropertyNames,
                                                          List<String> useablePropertyNames) {
        return recurseOverUseablePropertyNames(baseIfaceName, trailingName,
                necessaryPropertyNames, useablePropertyNames, new ArrayList<String>());
    }

    /**
     * Adds scored return types for the deserializer.
     * Scored return types score a potential return value with a best fit.
     * The scoring mechanism is used in case we need to infer the type, in which case
     * we try different return values and pick the one that has the most values.
     * Ties are broken arbitrarily.
     * @param deserializer
     * @param baseIfaceName
     * @param deserializerPropNames
     * @param trailingName
     * @param necessaries
     */
    static private void addDeserializerScoredReturnTypes(CodeBuilder deserializer,
                                                         String baseIfaceName,
                                                         List<String> deserializerPropNames,
                                                         String trailingName,
                                                         List<TypeElement> necessaries) {
        List<String> badPropertyNames = new ArrayList<String>();
        for (TypeElement necessary : necessaries) {
            deserializer.sp().iff()._as(necessary).eqq().nul().psbeg().spret().nul().cn().spend();
            badPropertyNames.add("_"+necessary.getSimpleName().toString());
        }
        Set<String> useablePropertyNames = new LinkedHashSet<String>();
        for (String s : deserializerPropNames) {
            if (!badPropertyNames.contains(s)) {
                useablePropertyNames.add(s.substring(1));
            }
        }
        deserializer.sp().__a(CANDIDATES).d().add()
                .a(recurseOverUseablePropertyNames(baseIfaceName, trailingName,
                        badPropertyNames, new ArrayList<String>(useablePropertyNames)))
                .p().cn();
    }

    /**
     * Makes a power set of a set
     * @param originalSet The original set
     * @param <T> Anything
     * @return The power set of the set
     */
    private static <T> Set<Set<T>> powerSet(Set<T> originalSet) {
        Set<Set<T>> sets = new LinkedHashSet<Set<T>>();
        if (originalSet.isEmpty()) {
            sets.add(new LinkedHashSet<T>());
            return sets;
        }
        List<T> list = new ArrayList<T>(originalSet);
        T head = list.get(0);
        Set<T> rest = new LinkedHashSet<T>(list.subList(1, list.size()));
        for (Set<T> set : powerSet(rest)) {
            Set<T> newSet = new LinkedHashSet<T>();
            newSet.add(head);
            newSet.addAll(set);
            sets.add(newSet);
            sets.add(set);
        }
        return sets;
    }

    /**
     * Makes the powerSet of a map
     * @param originalSet The original set
     * @param <T> anything
     * @param <Q> aything
     * @return Its power set
     */
    private static <T,Q> Set<Map<T,Q>> powerSet(Map<T,Q> originalSet) {
        Set<Set<T>> pwr = powerSet(originalSet.keySet());
        Set<Map<T,Q>> out = new LinkedHashSet<Map<T,Q>>();
        for (Set<T> set : pwr) {
            Map<T,Q> toAdd = new LinkedHashMap<T,Q>();
            for (T elt : set) {
                toAdd.put(elt, originalSet.get(elt));
            }
            out.add(toAdd);
        }
        return out;
    }

    /**
     * Adds a dollar sign in front of a string
     * @param s The string to add a dollar sign in front of
     * @return $s
     */
    static private String $(String s) {
        return "$"+s;
    }

    /**
     * Makes the interface of a braque object that will be implemented.
     * @param pkg The package to write the interface to.
     * @param comingFrom Interfaces of objects that contain this one, used for naming.
     * @param ifaceName The interface name.
     * @param isAbstract Will the interface be implemented by an abstract object.
     * @param prevIface Interface that we should implement.
     * @param attachmentType Base super type in hierarchy of object that will be implementing this.
     * @param necessaries Necessary elements that will need get() accessors.
     * @param processingEnvironment A processing environment to write the interface to.
     */
    private void makeInterface(String pkg, String comingFrom, String ifaceName, boolean isAbstract, String prevIface,
                               TypeElement attachmentType, List<TypeElement> necessaries,
                               ProcessingEnvironment processingEnvironment) {
        CodeBuilder interfaceBuilder = new CodeBuilder();
        interfaceBuilder.pkg(pkg).pubic().iface().a(ifaceName);
        if (isAbstract) {
            interfaceBuilder.G().as("T").ext().a(ifaceName).g();
        }
        interfaceBuilder.s().ext();
        if (comingFrom == null || "".equals(comingFrom)) {
            interfaceBuilder.processing().a("RESTEndpoint").cns();
        }
        if (prevIface != null) {
            interfaceBuilder.a(prevIface).Gg(isAbstract ? "T" : ifaceName);
        } else {
            interfaceBuilder.processing().a(getRestInterface());
        }
        interfaceBuilder.cns().abraque(attachmentType).Gg(isAbstract ? "T" : ifaceName);
        for (TypeElement necessary : necessaries) {
            interfaceBuilder.cns().abraque(necessary).a(Utils.GET);
        }
        interfaceBuilder.sbeg().spend();
        Utils.write(pkg + Utils.DOT + ifaceName, interfaceBuilder, processingEnvironment);
    }

    /**
     * Makes the shadow interface that shadow implementations will implement.
     * This interface allows us to chain together calls to shadows because shadow implementations always return something
     * with their interface. So in {@code Transformer.makeUser(id).addName(bar).addBlurb(hello).removeName().commit()},
     * @{code addName} et al return shadow interfaces.
     * @param pkg The package of the shadow interface.
     * @param ifaceName The name of the shadow interface.
     * @param prevIface A previous interface it is inheriting from.
     * @param isAbstract Is the interface going to be implemented only by other interfaces?
     * @param useableElementToInterfaceMap Elements that we will be creating add and remove functions for.
     * @param propertyToTypeMap Properties mapped to their types so that we can create add and remove function parameters.
     *                          Meaning if we have {@code addName(String val)}, this lets us know that val should be String.
     * @param processingEnvironment An environment to write to.
     */
    private void makeShadowInterface(String pkg, String ifaceName, String prevIface, boolean isAbstract,
                                     Map<TypeElement, List<String>> useableElementToInterfaceMap,
                                     PropertyToTypeMap propertyToTypeMap,
                                     ProcessingEnvironment processingEnvironment) {
        CodeBuilder shadowInterfaceBuilder = new CodeBuilder();
        shadowInterfaceBuilder.pkg(pkg).pubic().iface().$a(ifaceName).G();
        if (isAbstract) {
            shadowInterfaceBuilder.as("T").ext().$a(ifaceName).cs();
        }
        shadowInterfaceBuilder.as("Q").ext().a(ifaceName).g();
        if (prevIface != null) {
            shadowInterfaceBuilder.s().ext().$a(prevIface).G().$a(ifaceName).cs().a("Q").gs();
        } else {
            shadowInterfaceBuilder.s().ext().processing().$a("hadow").Gg("Q").s();
        }
        shadowInterfaceBuilder.beg();
        for (Map.Entry<TypeElement, List<String>> entry : useableElementToInterfaceMap.entrySet()) {
            String simpleName = entry.getKey().getSimpleName().toString();
            TypeElementAnnotationMirrorPair typeAndMirror = propertyToTypeMap.get(entry.getKey().getQualifiedName().toString());
            AnnotationMirror annotationMirror = typeAndMirror.getRight();
            shadowInterfaceBuilder.sp().as(isAbstract ? "T" : $(ifaceName))
                    .add(simpleName).as(typeFromPair(typeAndMirror)).val().pcn()
                    .sp().as(isAbstract ? "T" : $(ifaceName)).remove(simpleName).pcn();
            if (!Utils.isProperty(annotationMirror)) {
                shadowInterfaceBuilder.sp().as(isAbstract ? "T" : $(ifaceName))
                        .add(simpleName).as(typeAndMirror.getLeft()).val().pcn()
                        .sp().as(isAbstract ? "T" : $(ifaceName)).remove(simpleName)
                        .as(typeFromPair(typeAndMirror)).val().pcn()
                        .sp().as(isAbstract ? "T" : $(ifaceName)).remove(simpleName)
                        .as(typeAndMirror.getLeft()).val().pcn();
            }
        }

        shadowInterfaceBuilder.spend();
        Utils.write(pkg + Utils.DOT + Utils.DOLLAR + ifaceName, shadowInterfaceBuilder, processingEnvironment);
    }

    /**
     * kludgy method used to make a parameter equal to "val" which is a function argument.
     * @param s The parameter we want to equal to val
     * @return A map mapping that parameter to val
     */
    static private Map<String, String> valMapize(String s) {
        Map<String, String> out = new HashMap<String, String>();
        out.put(s,"val");
        return out;
    }

    /**
     * Used in build shadow implementation if we need the shadow to add or remove fields from an implementation.
     * @param add Are we adding or removing?
     * @param fullNonNecessaryPropertyToTypeMap All properties that have add or remove methods
     * @param dashedProperties Properties going to the constructor prepended with dashes
     * @param simpleName The name of the property being added or removed
     * @param ifaceName The interface of the shadow object.
     * @param shadowImplementationBuilder The code builder for the shadow implementation.
     */
    private void buildTransformingShadowImplementation(boolean add, PropertyToTypeMap fullNonNecessaryPropertyToTypeMap,
                                                       List<String> dashedProperties, String simpleName, String ifaceName,
                                                       CodeBuilder shadowImplementationBuilder) {
        List<String> undashedProperties = new ArrayList<String>();
        List<String> propertiesToPassToConstructor1 = new ArrayList<String>();
        for (Map.Entry<String, TypeElementAnnotationMirrorPair> innerEntry : fullNonNecessaryPropertyToTypeMap.entrySet()) {
            if (dashedProperties.contains("_"+ Utils.last(innerEntry.getKey()))) {
                propertiesToPassToConstructor1.add("_"+ Utils.last(innerEntry.getKey()));
                undashedProperties.add(Utils.last(innerEntry.getKey()));
            } else if (add && simpleName.equals(Utils.last(innerEntry.getKey()))) {
                propertiesToPassToConstructor1.add("_"+simpleName);
                undashedProperties.add(simpleName);
            }
        }
        List<String> propertiesToPassToConstructor2 = new ArrayList<String>();
        for (String dashed : dashedProperties) {
            if (!propertiesToPassToConstructor1.contains(dashed)) {
                propertiesToPassToConstructor2.add(dashed);
            }
        }
        Collections.sort(undashedProperties);
        propertiesToPassToConstructor2.addAll(propertiesToPassToConstructor1);
        Collections.sort(propertiesToPassToConstructor2);
        if (add) {
            propertiesToPassToConstructor2.set(propertiesToPassToConstructor2.indexOf("_"+simpleName), "val");
        }
        String newEltName = new CB().a(ifaceName).a_(undashedProperties)._a(Utils.IMPLEMENTATION).toS();
        shadowImplementationBuilder.spret().knew().$a(newEltName)
                .P().knew().atag(newEltName, randGen.current())
                .P().acs(propertiesToPassToConstructor2).pp().cn();
    }

    /**
     * Adds a simple NullPointerException in the code
     * @param builder the code builder to add the npe to
     * @param name the entity that is null
     */
    private static void genericNpe(CodeBuilder builder, String name) {
        builder.sp().npe().q("value of ").a("+").stringProv().a("prop")
                .a(name).a("()+").q(" cannot be null")
                .pcn();
    }

    /**
     * Makes shadow implementations. Shadows are intermediary states created by the transformer.
     * For example, when one does {@code Transformer.makeUser(id).addName(bar).addBlurb(hello).removeName().commit()},
     *
     * @param transformer The code builder for the transformer
     * @param pkg The package of the transformed element
     * @param elementName The name of the shadow implementation
     * @param ifaceName The name of the interface that the shadow implementation implements
     * @param typeTrees A representation of the type hierarchy
     * @param ctorPropertyToTypeMap The properties being passed into the constructor of a model object
     * @param fullNonNecessaryPropertyToTypeMap The properties accessible to the shadown implementation
     *                                          via add and remove.
     * @param processingEnvironment A processing environment to write the file
     */
    private void makeShadowImplementation(CodeBuilder transformer, String pkg, String elementName,
                                          String ifaceName, Map<String, TypeTree> typeTrees,
                                          PropertyToTypeMap ctorPropertyToTypeMap,
                                          PropertyToTypeMap fullNonNecessaryPropertyToTypeMap,
                                          ProcessingEnvironment processingEnvironment) {
        CodeBuilder shadowImplementationBuilder = new CodeBuilder();
        transformer.sp().statik().pubic().a(pkg).d().$as(elementName).a("take").P().a(pkg).d().as(elementName).val().psbeg()
                .spret().knew().a(pkg).d().$a(elementName).Pp("val").cn().spend();
        shadowImplementationBuilder.pkg(pkg).pubic().klass().$as(elementName).impl().$as(ifaceName).Gg(elementName).beg()
                .nsp().phinal().priv().as(elementName).ma(elementName).cn()
                .sp().pubic().$as(elementName).P().phinal().as(elementName)._a(elementName).psbeg()
                .sp().mas(elementName).eq()._as(elementName).cn()
                .spend().n();
        for (Map.Entry<String, TypeElementAnnotationMirrorPair> entry : fullNonNecessaryPropertyToTypeMap.entrySet()) {
            String simpleName = Utils.last(entry.getKey());
            String type = entry.getValue().getLeft();
            AnnotationMirror annotationMirror = entry.getValue().getRight();
            ////// add
            String addRetval = randGen.next();
            shadowImplementationBuilder.nsp().pubic().$as(addRetval).add(simpleName).as(typeFromPair(entry.getValue())).val().psbeg()
                    .sp().iff().vals().eqq().nul().psbeg();
            genericNpe(shadowImplementationBuilder, simpleName);
            shadowImplementationBuilder.spend();
            if (ctorPropertyToTypeMap.keySet().contains(entry.getKey())) {
                List<String> dashedProperties = makeKlonedObject(shadowImplementationBuilder,
                        ctorPropertyToTypeMap, typeTrees, "m"+elementName+".",
                        valMapize(simpleName), new HashSet<String>(), true);
                Collections.sort(dashedProperties);
                shadowImplementationBuilder.spret().knew().$a(elementName)
                        .P().knew().atag(elementName, randGen.current())
                        .P().acs(dashedProperties).pp().cn();
            } else {
                List<String> dashedProperties = makeKlonedObject(shadowImplementationBuilder,
                        ctorPropertyToTypeMap, typeTrees, "m"+elementName+".",
                        new HashMap<String, String>(), new HashSet<String>(), true);
                buildTransformingShadowImplementation(true, fullNonNecessaryPropertyToTypeMap,
                        dashedProperties, simpleName, ifaceName, shadowImplementationBuilder);
            }
            shadowImplementationBuilder.spend();
            ///// remove
            shadowImplementationBuilder.nsp().pubic().$as(randGen.next()).remove(simpleName).psbeg();
            if (!ctorPropertyToTypeMap.keySet().contains(entry.getKey())) {
                List<String> dashedProperties = makeKlonedObject(shadowImplementationBuilder,
                        ctorPropertyToTypeMap, typeTrees, "m"+elementName+".",
                        new HashMap<String, String>(), new HashSet<String>(), true);
                Collections.sort(dashedProperties);
                shadowImplementationBuilder.spret().knew().$a(elementName)
                        .P().knew().atag(elementName, randGen.current())
                        .P().acs(dashedProperties).pp().cn();
            } else {
                // try to merge with above...
                List<String> dashedProperties = makeKlonedObject(shadowImplementationBuilder,
                        ctorPropertyToTypeMap, typeTrees, "m"+elementName+".",
                        new HashMap<String, String>(), new HashSet<>(Arrays.asList(new String[]{simpleName})), true);
                buildTransformingShadowImplementation(false, fullNonNecessaryPropertyToTypeMap,
                        dashedProperties, simpleName, ifaceName, shadowImplementationBuilder);
            }
            shadowImplementationBuilder.spend();
            if (!Utils.isProperty(annotationMirror)) {
                ////// single value adder for lists and sets
                shadowImplementationBuilder.nsp().pubic().$as(addRetval).add(simpleName).as(type)._val().psbeg()
                        .sp().iff()._vals().eqq().nul().psbeg();
                genericNpe(shadowImplementationBuilder, simpleName);
                shadowImplementationBuilder.spend();
                if (Utils.isPropertySet(annotationMirror)) {
                    shadowImplementationBuilder.sp().jset(type).vals().eq().hashset().cn();
                } else {
                    shadowImplementationBuilder.sp().jlist(type).vals().eq().arraylist().cn();
                }
                shadowImplementationBuilder.sp().val().dadd()._val().pcn()
                        .spret().add(simpleName).val().pcn()
                        .spend();
                //// remove function for lists and sets
                shadowImplementationBuilder.nsp().pubic().$as(elementName).remove(simpleName)
                        .as(typeFromPair(entry.getValue())).val().psbeg()
                        .sp().iff().vals().eqq().nul().psbeg();
                genericNpe(shadowImplementationBuilder, simpleName);
                shadowImplementationBuilder.spend();
                boolean notInMap = !ctorPropertyToTypeMap.keySet().contains(entry.getKey());
                List<String> dashedProperties = makeKlonedObject(shadowImplementationBuilder,
                        ctorPropertyToTypeMap, typeTrees, "m"+elementName+".",
                        notInMap ? new HashMap<String, String>() : valMapize(simpleName),
                        new HashSet<String>(), notInMap);
                Collections.sort(dashedProperties);
                shadowImplementationBuilder.spret().knew().$a(elementName)
                        .P().knew().a(elementName).P().acs(dashedProperties).pp().cn().spend();
                ///// remove function for singleton
                shadowImplementationBuilder.nsp().pubic().$as(elementName).remove(simpleName).as(type)._val().psbeg()
                        .sp().iff()._vals().eqq().nul().psbeg();
                genericNpe(shadowImplementationBuilder, simpleName);
                shadowImplementationBuilder.spend();
                if (Utils.isPropertySet(annotationMirror)) {
                    shadowImplementationBuilder.sp().jset(type).vals().eq().hashset().cn();
                } else {
                    shadowImplementationBuilder.sp().jlist(type).vals().eq().arraylist().cn();
                }
                shadowImplementationBuilder.sp().val().dadd()._val().pcn()
                        .spret().remove(simpleName).val().pcn()
                        .spend();
            }
        }

        shadowImplementationBuilder.nsp().pubic().as(elementName).a("commit").Ppsbeg()
                .spret().ma(elementName).klone().cn()
                .spend()
                .spend();

        Utils.write(pkg + Utils.DOT + Utils.DOLLAR + elementName, shadowImplementationBuilder, processingEnvironment);
    }

    /**
     * Gateway function to add code to the transformer
     * @param transformer The code builder for the transformer
     * @param pkg The package in which the object being transformed resides.
     * @param ifaceName The interface of the object being transformed.
     * @param ctorPropertyToTypeMap All of the properties being passed to the constructor.
     * @param fullNonNecessaryPropertyToTypeMap Those properties that are pased to the constructor but not necessary,
     *                                          meaning that they can be the subject of "add" and "remove" calls to a
     *                                          shadow implementation.
     */
    private void addToTransformer(CodeBuilder transformer,
                                  String pkg,
                                  String ifaceName,
                                  PropertyToTypeMap ctorPropertyToTypeMap,
                                  PropertyToTypeMap fullNonNecessaryPropertyToTypeMap) {

        List<String> ctorPropertiesAsConstructorArguments = new ArrayList<String>();
        List<String> ctorPropertiesAsArgumentsToConstructor = new ArrayList<String>();
        for (Map.Entry<String, TypeElementAnnotationMirrorPair> entry : ctorPropertyToTypeMap.entrySet()) {
            String left = entry.getValue().getLeft();
            if (fullNonNecessaryPropertyToTypeMap.keySet().contains(entry.getKey())) {
                break;
            }
            ctorPropertiesAsConstructorArguments.add("final " + typeFromStringAndAnnotationMirror(Utils.isComplexProperty(left) ? pkg+ Utils.DOT+left : left, entry.getValue().getRight()) + " _" + Utils.last(entry.getKey()));
            ctorPropertiesAsArgumentsToConstructor.add(" _" + Utils.last(entry.getKey()));
        }

        transformer.nsp().statik().pubic().a(pkg).d().$a(ifaceName)._as(Utils.IMPLEMENTATION).a("make").a(ifaceName)
                .P().acns(ctorPropertiesAsConstructorArguments).psbeg()
                .sp().a(pkg).d().a(ifaceName)._as(Utils.IMPLEMENTATION).as("shadowMe")
                .eq().knew().a(pkg).d().a(ifaceName)._as(Utils.IMPLEMENTATION)
                .P().acs(ctorPropertiesAsArgumentsToConstructor).pcn()
                .spret().knew().a(pkg).d().$a(ifaceName)._a(Utils.IMPLEMENTATION).a("(shadowMe)").cn().spend();

    }

    /**
     * Gateway function to add code to the deserializer.
     * @param deserializer The deserializer code builder.
     * @param baseName The base name of the object being deserialized.
     * @param pkg The package of the object being deserialized.
     * @param ifaceName The interface implemented by the object being deserialized.
     * @param attachmentType The base supertype of the object being deserialized
     * @param functionSuffix Similar to the serializer functionSuffix.
     * @param pathReferenceName Similar to the serializer pathReferenceName
     * @param ctorPropertyToTypeMap Similar to the serializer ctorPropertyToTypeMap
     * @param typeTrees Information about the class hierarchy.
     */
    private void addToDeserializer(CodeBuilder deserializer, String baseName, String pkg,
                                  String ifaceName, TypeElement attachmentType, String functionSuffix,
                                  String pathReferenceName, PropertyToTypeMap ctorPropertyToTypeMap,
                                  Map<String, TypeTree> typeTrees) {
        List<String> deserializerPropNames = new ArrayList<String>();
        deserializer.sp().iff().P()._as(TYPE).neq().nul().s()
                .and()._a(TYPE).oeqq().stringProv().a(TYPE).a(attachmentType).Pppp().or().a("inferType").p().sbeg()
                .sp().a("serialized.").remove().a(ADDUIDS).P().stringProvPath().a(pathReferenceName)._a(TYPE)
                .Pp().cs().a(PREVIOUSUIDS).pp().cn();
        for (Map.Entry<String, TypeElementAnnotationMirrorPair> entry : ctorPropertyToTypeMap.entrySet()) {
            makeDeserializerProperty(deserializer, entry, pkg, functionSuffix, pathReferenceName, deserializerPropNames);
        }
        addDeserializerScoredReturnTypes(deserializer, pkg + DOT + baseName, deserializerPropNames,
                pkg+ Utils.DOT+ifaceName,
                Utils.getNecessaryElementsAtThisLevelAndSuper(typeTrees.get(attachmentType.getQualifiedName().toString())));
        deserializer.spend();
    }

    /**
     * Gateway function to add code to the serializer.
     * @param serializer The code builder for the serializer.
     * @param pkg The package of the objects being serialized.
     * @param attachmentType The base supertype of the object being serialized.
     * @param functionSuffix Serializer functions are called `serializeFoo'. This is `Foo'
     * @param pathReferenceName Used so that we can correctly invote the string propvisioner from the serializer.
     * @param ctorPropertyToTypeMap Properties we are serializing.
     */
    private void addToSerializer(CodeBuilder serializer, String pkg,
                                 TypeElement attachmentType, String functionSuffix,
                                 String pathReferenceName, PropertyToTypeMap ctorPropertyToTypeMap) {
        serializer.sp().iff().as(DESERIALIZED).neq().nul().and().as(DESERIALIZED).instof().abraque(attachmentType).psbeg();
        if (OperationUtils.fromString(mOp).equals(Operation.CREATE)) {
            serializer.sp().a("out").d().a("put").P().a(ADDUIDS).P().stringProvPath().a(pathReferenceName)._a(TYPE)
                    .Pp().cs().a(PREVIOUSUIDS).p().cs().stringProv().a(TYPE).a(attachmentType).Ppp().cn();
        }
        for (Map.Entry<String, TypeElementAnnotationMirrorPair> entry : ctorPropertyToTypeMap.entrySet()) {
            makeSerializerProperty(serializer, entry, pkg, functionSuffix, pathReferenceName);
        }
        serializer.spend();
    }

    private List<String> makeKlonedObject(CodeBuilder builder,
                                          PropertyToTypeMap ctorPropertyToTypeMap, Map<String, TypeTree> typeTrees) {
        return makeKlonedObject(builder, ctorPropertyToTypeMap, typeTrees, "", new HashMap<String,String>(), new HashSet<String>(), true);
    }

    /**
     * Generic method to clone an object.
     * @param builder The builder we are writing the klone to
     * @param ctorPropertyToTypeMap properties that can be passed to the constructor of the klone.
     *                              Note that this method does not actually generate a constructor - it
     *                              just generates definitions of variables that would be passed to a
     *                              constructor.
     * @param typeTrees A map describing type hierarchies of the braque scheme
     * @param prefix A previx to append for the object being kloned
     * @param replaceMap Used to replace certain properties with others.  Useful if we
     *                   want all of the properties but one or two to be kloned.
     * @param excludeSet Properties from the original object that should not be kloned.
     * @param add If the klone should remove or add items from a list or set.
     * @return A list of properties that are used in the method that can be fed to a constructor.
     */

    private List<String> makeKlonedObject(CodeBuilder builder, PropertyToTypeMap ctorPropertyToTypeMap,
                                          Map<String, TypeTree> typeTrees, String prefix, Map<String, String> replaceMap,
                                          Set<String> excludeSet, boolean add) {
        List<String> dashedProperties = new ArrayList<String>();
        for (Map.Entry<String, TypeElementAnnotationMirrorPair> entry : ctorPropertyToTypeMap.entrySet()) {
            AnnotationMirror annotationMirror = entry.getValue().getRight();
            String baseType = entry.getValue().getLeft();
            String propName = Utils.last(entry.getKey());
            if (excludeSet.contains(propName)) {
                continue;
            }
            builder.sp().as(typeFromPair(entry.getValue()));
            if (!Utils.isProperty(annotationMirror)) {
                builder.__as(propName);
            } else {
                builder._as(propName);
            }
            builder.eq();
            if (replaceMap.keySet().contains(propName)) {
                builder.a(replaceMap.get(propName));
            } else {
                builder.a(prefix).get(propName);
                if (Utils.isComplexProperty(baseType) && Utils.isProperty(annotationMirror)) {
                    builder.klone();
                }
            }
            builder.cn();
            if (!Utils.isProperty(annotationMirror)) {
                if (Utils.isPropertySet(annotationMirror)) {
                    builder.sp().jset(baseType);
                } else {
                    builder.sp().jlist(baseType);
                }
                if (replaceMap.keySet().contains(propName)) {
                    builder._a_s(propName);
                } else {
                    builder._as(propName);
                }
                builder.eq();
                if (replaceMap.keySet().contains(propName)) {
                    builder.a(prefix).get(propName);
                } else {
                    if (Utils.isPropertySet(annotationMirror)) {
                        builder.hashset();
                    } else {
                        builder.arraylist();
                    }
                }
                builder.cn();
                boolean hasSubs = isComplexProperty(Utils.getAnnotationMirrorValueAsTypeElement(annotationMirror)
                        .getQualifiedName().toString());
                if (hasSubs) {
                    List<TypeTree> subs = typeTrees.get(Utils.getAnnotationMirrorValueAsTypeElement(annotationMirror)
                            .getQualifiedName().toString()).mSubs;
                    hasSubs = subs != null && !subs.isEmpty();
                }
                if (replaceMap.keySet().contains(propName)) {
                    if (Utils.isPropertySet(annotationMirror)) {
                        builder.sp().jset(baseType)._as(propName).eq().hashset().cn();
                    } else {
                        builder.sp().jlist(baseType)._as(propName).eq().arraylist().cn();
                    }
                    builder.sp().four().a(baseType);
                    if (Utils.isComplexProperty(baseType) && hasSubs) {
                        builder.Gg(baseType);
                    }
                    builder.sas("elt").col()._a_(propName).psbeg();
                    builder.sp()._a(propName).dadd().a("elt");
                    if (Utils.isComplexProperty(baseType)) {
                        builder.klone();
                    }
                    builder.pcn().spend();
                }
                builder.sp().four().a(baseType);
                if (Utils.isComplexProperty(baseType) && hasSubs) {
                    builder.Gg(baseType);
                }
                builder.sas("elt").col().__a(propName).psbeg();
                builder.sp()._a(propName).d();
                if (add) {
                    builder.add();
                } else {
                    builder.remove();
                }
                builder.a("elt");
                if (Utils.isComplexProperty(baseType)) {
                    builder.klone();
                }
                builder.pcn().spend();
            }
            dashedProperties.add("_"+propName);
        }
        return dashedProperties;
    }

    /**
     * Builds a model implementation. For example, if we have a model MeShowUser, this would
     * (potentially) build MeShowUserName_Implementation, MeShowUSerBlurb_Name_Implementation, etc. - all of the Braque implementations.
     * @param pkg The package to generate in.
     * @param prevImplementation An implementation to inherit from
     * @param uid The UID type element.
     * @param ifaceName The interface to implement (i.e. MeShowUser)
     * @param elementName The name of the element (i.e. MeShowUserBlurb_Name_Implementation)
     * @param path The path to this element, used by Clojure
     * @param isAbstract Is this model implementation abstract?
     * @param restElement The API endpoint to which this element is linked
     * @param attachmentType The highest type in the elements hierarchy
     * @param typeTrees A map of types to their hierarchies.
     * @param elementToInterfaceMap A map of various properties to interfaces (getter, setter) that they implement
     * @param declarationPropertyMap Fields we are declaring for this model implementation
     * @param ctorPropertyToTypeMap Arguments we are passing to the constructor of this model implementation
     * @param superPropertyToType Arguments we are passing to the super() constructor of this model implementation
     * @param propertyToTypeMap Information to determine types of fields, parameters and return values
     * @param processingEnvironment Used to write the implementation file
     */
    private void makeImplementation(String pkg, String prevImplementation, TypeElement uid,
                                    String ifaceName, String elementName, String path,
                                    boolean isAbstract, TypeElement restElement,
                                    TypeElement attachmentType,
                                    Map<String, TypeTree> typeTrees,
                                    Map<TypeElement, List<String>> elementToInterfaceMap,
                                    PropertyToTypeMap declarationPropertyMap,
                                    PropertyToTypeMap ctorPropertyToTypeMap,
                                    PropertyToTypeMap superPropertyToType,
                                    PropertyToTypeMap propertyToTypeMap,
                                    ProcessingEnvironment processingEnvironment) {

        TypeTree currentTree = typeTrees.get(attachmentType.getQualifiedName().toString());

        ClojureHelper clojureHelper = new ClojureHelper(null, restElement, attachmentType, null, null,
                elementName, currentTree, ifaceName, attachmentType.getQualifiedName().toString(),
                null, null, declarationPropertyMap, superPropertyToType);

        CodeBuilder implementationBuilder = new CodeBuilder();
        implementationBuilder.pkg(pkg);
        clojureHelper.initVariableFieldsAndInject(null,
                ClojureHelper.Place.CLASSDEF,
                elementName,
                PropertyManyness.SIMPLE,
                path.toLowerCase(),
                implementationBuilder);
        implementationBuilder.a("@").processing().a("MadeWithLoveByBraque").n().pubic().abstr(isAbstract).klass().a(elementName);
        if (isAbstract) {
            implementationBuilder.G().as("T").ext().a(ifaceName).g();
        }
        if (prevImplementation != null) {
            implementationBuilder.s().ext().a(prevImplementation).Gg(isAbstract ? "T" : ifaceName);
        }
        implementationBuilder.n().s().impl();
        List<String> implementations = new ArrayList<String>();
        implementations.add(ifaceName+(isAbstract ? "<T>" : ""));
        for (List<String> ifaces : elementToInterfaceMap.values()) {
            for (String iface : ifaces) {
                implementations.add(iface);
            }
        }
        implementationBuilder.acns(implementations).sbeg();

        for (Map.Entry<String, TypeElementAnnotationMirrorPair> entry : declarationPropertyMap.entrySet()) {
            clojureHelper.initVariableFieldsAndInject(entry.getValue().getPropElement(),
                    ClojureHelper.Place.FIELDDEF,
                    last(entry.getKey()),
                    getPropertyType(entry.getValue().getRight()),
                    (path + "/" + last(entry.getKey())).toLowerCase(),
                    implementationBuilder);
            if (processingEnvironment.getOptions() != null &&
                    processingEnvironment.getOptions().containsKey("makeObjectFieldsPublic")
                    && (processingEnvironment.getOptions().get("makeObjectFieldsPublic").equals("true")
                    || processingEnvironment.getOptions().get("makeObjectFieldsPublic").equals("True")
                    || processingEnvironment.getOptions().get("makeObjectFieldsPublic").equals("yes")
                    || processingEnvironment.getOptions().get("makeObjectFieldsPublic").equals("Yes")
                    || processingEnvironment.getOptions().get("makeObjectFieldsPublic").equals("please")
                    || processingEnvironment.getOptions().get("makeObjectFieldsPublic").equals("hellYeah"))) {
                implementationBuilder.sp().pubic();
            } else {
                implementationBuilder.sp().protect();
            }
            implementationBuilder.as(typeFromPair(entry.getValue())).ma(Utils.last(entry.getKey())).cn();
        }

        if (!declarationPropertyMap.isEmpty()) {
            implementationBuilder.n();
        }

        List<String> ctorProperties = new ArrayList<String>();
        PropertyToTypeMap ctorPropertiesWithOnlyLast = new PropertyToTypeMap();
        List<String> ctorPropertiesAsConstructorArguments = new ArrayList<String>();
        for (Map.Entry<String, TypeElementAnnotationMirrorPair> entry : ctorPropertyToTypeMap.entrySet()) {
            ctorProperties.add(Utils.last(entry.getKey()));
            ctorPropertiesWithOnlyLast.put(Utils.last(entry.getKey()), entry.getValue());
        }
        Collections.sort(ctorProperties);
        for (String key : ctorProperties) {
            TypeElementAnnotationMirrorPair val = ctorPropertiesWithOnlyLast.get(key);
            ctorPropertiesAsConstructorArguments.add(
                    new CB().phinal().as(typeFromPair(val))._a(Utils.last(key)).toS());
        }

        clojureHelper.initVariableFieldsAndInject(null,
                ClojureHelper.Place.CTOR,
                elementName,
                PropertyManyness.SIMPLE,
                path.toLowerCase(),
                implementationBuilder);
        implementationBuilder.sp().pubic().a(elementName).P().acns(ctorPropertiesAsConstructorArguments).psbeg();

        if (!superPropertyToType.isEmpty()) {
            List<String> superProps = new ArrayList<String>();
            for (Map.Entry<String, TypeElementAnnotationMirrorPair> entry : superPropertyToType.entrySet()) {
                superProps.add("_" + Utils.last(entry.getKey()));
            }
            Collections.sort(superProps);
            implementationBuilder.sp().sup().acs(superProps).pcn();
        }

        for (Map.Entry<String, TypeElementAnnotationMirrorPair> entry : declarationPropertyMap.entrySet()) {
            String last = Utils.last(entry.getKey());
            implementationBuilder.sp().iff()._as(last).eqq().nul().psbeg();
            genericNpe(implementationBuilder, last);
            implementationBuilder.spend().sp().mas(last).eq()._a(last).cn();
        }

        implementationBuilder.spend();

        // for now, all declarations need getters
        // TODO: make this more generic in case there is ever the case where we declare something but cannot get it
        for (Map.Entry<String, TypeElementAnnotationMirrorPair> entry : declarationPropertyMap.entrySet()) {
            clojureHelper.initVariableFieldsAndInject(entry.getValue().getPropElement(),
                    ClojureHelper.Place.GET,
                    elementName,
                    getPropertyType(entry.getValue().getRight()),
                    (path + "/" + last(entry.getKey())).toLowerCase(),
                    implementationBuilder);
            implementationBuilder.sp().override()
                    .sp().pubic().as(typeFromPair(propertyToTypeMap.get(entry.getKey()))).get(Utils.last(entry.getKey())).sbeg()
                    .spret().ma(Utils.last(entry.getKey())).cn()
                    .spend();
        }

        for (TypeElement tocheck : elementToInterfaceMap.keySet()) {
            for (String iface : elementToInterfaceMap.get(tocheck)) {
                String base = Utils.unbraque(getBase(iface));
                if (isSet(iface)) {
                    clojureHelper.initVariableFieldsAndInject(tocheck,
                            ClojureHelper.Place.SET,
                            elementName,
                            getPropertyType(getPropertyAnnotation(tocheck)),
                            (path + "/" + tocheck.getSimpleName()).toLowerCase(),
                            implementationBuilder);
                    implementationBuilder.n().sp().override()
                            .sp().pubic().voi().set(Utils.last(base)).phinal()
                            .as(typeFromPair(propertyToTypeMap.get(base)))._a(Utils.last(base)).psbeg()
                            .sp().mas(Utils.last(base)).eq()._a(Utils.last(base)).cn()
                            .spend();
                } else if (isRemove(iface)) {
                    clojureHelper.initVariableFieldsAndInject(tocheck,
                            ClojureHelper.Place.REMOVE,
                            elementName,
                            getPropertyType(getPropertyAnnotation(tocheck)),
                            (path + "/" + tocheck.getSimpleName()).toLowerCase(),
                            implementationBuilder);
                    implementationBuilder.n().sp().override()
                            .sp().pubic().voi().removeFrom(Utils.last(base)).phinal()
                            .as(propertyToTypeMap.get(base).getLeft())._a(Utils.last(base)).psbeg()
                            .sp().ma(Utils.last(base)).d().remove()._a(Utils.last(base)).pcn()
                            .spend();
                } else if (isAdd(iface)) {
                    clojureHelper.initVariableFieldsAndInject(tocheck,
                            ClojureHelper.Place.ADD,
                            elementName,
                            getPropertyType(getPropertyAnnotation(tocheck)),
                            (path + "/" + tocheck.getSimpleName()).toLowerCase(),
                            implementationBuilder);
                    implementationBuilder.n().sp().override()
                            .sp().pubic().voi().addTo(Utils.last(base)).phinal()
                            .as(propertyToTypeMap.get(base).getLeft())._a(Utils.last(base)).psbeg()
                            .sp().ma(Utils.last(base)).dadd()._a(Utils.last(base)).pcn()
                            .spend();
                }
            }
        }

        if (!isAbstract) {
            // klone
            implementationBuilder.n().sp().override().sp().pubic().as(elementName).a("klone").Ppsbeg();
            List<String> dashedProperties = makeKlonedObject(implementationBuilder, ctorPropertyToTypeMap, typeTrees);
            Collections.sort(dashedProperties);
            implementationBuilder.spret().knew().a(elementName).P().acs(dashedProperties).pcn().spend();
            // equals
            implementationBuilder.n().sp().override().sp().pubic().bool().a("equals").P().a("Object o").psbeg()
                    .sp().iff().as("o").eqq().nul().psbeg()
                    .spret().phalse().cn().spend()
                    .sp().iff().as("o").instof().a(ifaceName).psbeg()
                    .spret().PPp(ifaceName).a("o").pd().get(uid).a(".equals(").get(uid).p().cn()
                    .spend().spret().phalse().cn().spend();
            // hashCode
            implementationBuilder.n().sp().override().sp().pubic().integ().a("hashCode").Pp().sbeg()
                    .sp().integ().as("hash").eq().a("1").cn()
                    .sp().as("hash").eq().as("hash * 17 + ").get(uid).d().a("hashCode()").cn()
                    .spret().a("hash").cn().spend();
        }

        implementationBuilder.spend();
        Utils.write(pkg + Utils.DOT + elementName, implementationBuilder, processingEnvironment);
    }

    /**
     * At the rest endpoint me/?/favoriteTeams we may have multiple objects that can go at this endpoint.
     * This function recurses down over the object hierarchy to build all implementations.
     * @param pkg The package into which we are building.
     * @param prevIface The interface that the super object is implementing.
     * @param prevImplementation The super object impelmenting the previous interface.
     * @param restElement The API endpoint element.
     * @param attachmentType The attachment type, same as in buildRestType
     * @param baseType The base type, same as in buildRestType
     * @param subtypeMap A map used to determine if a model implementation should be abstract or not.
     *                   In Braque, model implementations are abstract if any other model implementation
     *                   can inherit their behavior.
     * @param typeTrees Describes class hierarchies, same as in buildRestType
     * @param comingFrom Describes where in the parsing of properties we are, same as in buildRestType
     * @param path The current path, same as in buildRestType
     * @param protoPropertyToInterfaceMap A map that links properties (name, height, luckyNumbers) to
     *                                    interfaces that are implemented (NameGet, HeightSet, LuckyNumbersAdd)
     *                                    due to these properties.  The implementing of these interfaces is responsible
     *                                    for populating Braque objects with their methods
     * @param inputPropertyList Properties that have been consumed by implementations - used to make sure that a given
     *                          sub implementation implements its constructor correctly.
     * @param remoteFullNonNecessaryPropertyToTypeMap All non-necessary properties that have already been implemented in a super type.
     * @param remoteCtorPropertyToTypeMap All properties that have been passed to the constructor of a supertype.
     * @param propertyToTypeMap A map of properties to their types used for method and function declarations.
     * @param previousIsUseableBySerializingClasses Lets us know if an implementation in this hierarchy has been used by serializing classes,
     *                                              meaning that information from that implementation has been used to write code to the serializer.
     * @param serializer A Code Builder for the serializer.
     * @param deserializer A Code Builder for the deserializer.
     * @param transformer A Code Builder for the transformer.
     * @param processingEnvironment Used to write files.
     */
    private void recurseOverSubTypesAndBuildSourceFiles(String pkg, String prevIface, String prevImplementation,
                                                        TypeElement restElement,
                                                        TypeElement attachmentType, TypeElement baseType,
                                                        Map<TypeElement, List<TypeElement>> subtypeMap,
                                                        Map<String, TypeTree> typeTrees,
                                                        String comingFrom, String path,
                                                        PropertyToInterfaceMap protoPropertyToInterfaceMap,
                                                        List<String> inputPropertyList,
                                                        PropertyToTypeMap remoteFullNonNecessaryPropertyToTypeMap,
                                                        PropertyToTypeMap remoteCtorPropertyToTypeMap,
                                                        PropertyToTypeMap propertyToTypeMap,
                                                        boolean previousIsUseableBySerializingClasses,
                                                        CodeBuilder serializer,
                                                        CodeBuilder deserializer,
                                                        CodeBuilder transformer,
                                                        ProcessingEnvironment processingEnvironment) {

        TypeTree baseTypeTree = typeTrees.get(baseType.getQualifiedName().toString());
        List<TypeElement> validTypes = Utils.getSuperAndSubTypes(baseTypeTree);
        boolean isInPath = false;
        for (TypeElement typeElement : validTypes) {
            if (typeElement.getQualifiedName().toString().equals(attachmentType.getQualifiedName().toString())) {
                isInPath = true;
                break;
            }
        }
        if (!isInPath) {
            return;
        }


        PropertyToInterfaceMap useablePropertyToInterfaceMap = new PropertyToInterfaceMap(protoPropertyToInterfaceMap);
        PropertyToTypeMap fullNonNecessaryPropertyToTypeMap = new PropertyToTypeMap(remoteFullNonNecessaryPropertyToTypeMap);

        // first, we build the element to interface map
        // it will be comprised of mutable properties accessible only at this level
        TypeTree currentTree = typeTrees.get(attachmentType.getQualifiedName().toString());
        // guarantees that property is implemented by this tree only and not supers
        for (Iterator<Map.Entry<TypeElement, List<String>>> it = useablePropertyToInterfaceMap.entrySet().iterator();
             it.hasNext();) {
            Map.Entry<TypeElement, List<String>> entry = it.next();
            if (!Utils.propertyImplementedBy(currentTree, entry.getKey())
                    || (currentTree.mSuper != null && Utils.propertyImplementedBy(currentTree.mSuper, entry.getKey()))) {
                it.remove();
            } else {
                fullNonNecessaryPropertyToTypeMap.put(entry.getKey().getQualifiedName().toString(),
                        propertyToTypeMap.get(entry.getKey().getQualifiedName().toString()));
            }
        }

        String pathReferenceName = restElement.getSimpleName() + comingFrom;
        String functionSuffix = restElement.getSimpleName() + mOp + comingFrom;
        String baseIfaceName = functionSuffix + baseType.getSimpleName();
        String ifaceName = functionSuffix + attachmentType.getSimpleName();

        for (Map<TypeElement, List<String>> elementToInterfaceMap : powerSet(useablePropertyToInterfaceMap)) {

            PropertyToTypeMap ctorPropertyToTypeMap = new PropertyToTypeMap(remoteCtorPropertyToTypeMap);
            PropertyToTypeMap declarationPropertyMap = new PropertyToTypeMap();

            List<String> propertyList = new ArrayList<String>(inputPropertyList);
            for (TypeElement s : elementToInterfaceMap.keySet()) {
                propertyList.add(s.getSimpleName().toString());
            }

            Collections.sort(propertyList);

            String elementName = ifaceName + StringUtils.join(propertyList,"_")+"_"+ Utils.IMPLEMENTATION;
            boolean isAbstract = subtypeMap.containsKey(attachmentType)
                    && subtypeMap.get(attachmentType) != null
                    && !subtypeMap.get(attachmentType).isEmpty();
            boolean isUseableBySerializer = !isAbstract
                    && elementToInterfaceMap.size() == useablePropertyToInterfaceMap.size()
                    && previousIsUseableBySerializingClasses;
            boolean isUseableByDeserializer = isUseableBySerializer && Operation.SHOW.equals(OperationUtils.fromString(mOp));
            boolean shouldMakeInterfaces = elementToInterfaceMap.size() == useablePropertyToInterfaceMap.size()
                    && previousIsUseableBySerializingClasses;
            boolean shouldMakeShadowInterfaces = (Utils.CREATE.equals(mOp) || Utils.UPDATE.equals(mOp)) && shouldMakeInterfaces;
            boolean shouldMakeShadowImplementation = (Utils.CREATE.equals(mOp) || Utils.UPDATE.equals(mOp)) && !isAbstract;
            boolean isUseableByTransformer = /*(comingFrom == null || "".equals(comingFrom)) &&*/ (!isAbstract)
                    && previousIsUseableBySerializingClasses
                    && elementToInterfaceMap.size() == 0
                    && (Utils.CREATE.equals(mOp) || Utils.UPDATE.equals(mOp));

            List<TypeElement> necessaries = Utils.getNecessaryElementsAtThisLevel(typeTrees.get(attachmentType.getQualifiedName().toString()));
            for (TypeElement necessary : necessaries) {
                propertyToTypeMap.put(necessary.getQualifiedName().toString(),
                        new TypeElementAnnotationMirrorPair(Utils.getPropertyTypeElement(necessary).getQualifiedName().toString(),
                                Utils.getPropertyAnnotation(necessary), necessary));
                declarationPropertyMap.put(necessary.getQualifiedName().toString(),
                        new TypeElementAnnotationMirrorPair(Utils.getPropertyTypeElement(necessary).getQualifiedName().toString(),
                                Utils.getPropertyAnnotation(necessary), necessary));
                ctorPropertyToTypeMap.put(necessary.getQualifiedName().toString(),
                        new TypeElementAnnotationMirrorPair(Utils.getPropertyTypeElement(necessary).getQualifiedName().toString(),
                                Utils.getPropertyAnnotation(necessary), necessary));
            }

            for (TypeElement tocheck : elementToInterfaceMap.keySet()) {
                declarationPropertyMap.put(tocheck.getQualifiedName().toString(),
                        propertyToTypeMap.get(tocheck.getQualifiedName().toString()));
                ctorPropertyToTypeMap.put(tocheck.getQualifiedName().toString(),
                        propertyToTypeMap.get(tocheck.getQualifiedName().toString()));
            }

            PropertyToTypeMap superPropertyToType = new PropertyToTypeMap();
            for (Map.Entry<String, TypeElementAnnotationMirrorPair> entry : ctorPropertyToTypeMap.entrySet()) {
                if (!declarationPropertyMap.keySet().contains(entry.getKey())) {
                    superPropertyToType.put(entry.getKey(), entry.getValue());
                }
            }

            TypeElement uid = getUID(attachmentType);
            makeImplementation(pkg, prevImplementation, uid, ifaceName, elementName, path.toLowerCase(), isAbstract,
                    restElement, attachmentType, typeTrees,
                    elementToInterfaceMap, declarationPropertyMap, ctorPropertyToTypeMap,
                    superPropertyToType, propertyToTypeMap, processingEnvironment);

            if (isUseableBySerializer) {
                addToSerializer(serializer, pkg, attachmentType,
                        functionSuffix, pathReferenceName, ctorPropertyToTypeMap);
            }
            if (isUseableByDeserializer) {
                addToDeserializer(deserializer, baseIfaceName, pkg, ifaceName, attachmentType,
                        functionSuffix, pathReferenceName, ctorPropertyToTypeMap, typeTrees);
            }

            if (isUseableByTransformer) {
                addToTransformer(transformer, pkg, ifaceName, ctorPropertyToTypeMap, fullNonNecessaryPropertyToTypeMap);
            }

            if (shouldMakeShadowInterfaces) {
                makeShadowInterface(pkg, ifaceName, prevIface, isAbstract, useablePropertyToInterfaceMap,
                        propertyToTypeMap, processingEnvironment);
            }
            if (shouldMakeInterfaces) {
                makeInterface(pkg, comingFrom, ifaceName, isAbstract, prevIface, attachmentType, necessaries, processingEnvironment);
            }
            if (shouldMakeShadowImplementation) {
                makeShadowImplementation(transformer, pkg, elementName, ifaceName, typeTrees, ctorPropertyToTypeMap,
                        fullNonNecessaryPropertyToTypeMap, processingEnvironment);
            }

            if (isAbstract) {
                for (TypeElement tp : subtypeMap.get(attachmentType)) {
                    recurseOverSubTypesAndBuildSourceFiles(pkg, ifaceName, elementName, restElement,
                            tp, baseType, subtypeMap, typeTrees, comingFrom, path,
                            protoPropertyToInterfaceMap,
                            propertyList, fullNonNecessaryPropertyToTypeMap, ctorPropertyToTypeMap, propertyToTypeMap,
                            elementToInterfaceMap.size() == useablePropertyToInterfaceMap.size() && previousIsUseableBySerializingClasses,
                            serializer, deserializer, transformer, processingEnvironment);
                }
            }
        }
    }

    // TODO: move this to a dedicated validator
    private void validateThatSimpleDestroyedPropertiesAreOnlyTopLevel(String comingFrom, TypeElement restElement,
                                                                      TypeElement currentProperty, ProcessingEnvironment processingEnvironment) {
        if (comingFrom != null && !"".equals(comingFrom) && DESTROY.equals(OperationUtils.fromString(mOp))) {
            processingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, "The DESTROY operation can only be applied to "+
                    "top-level properties of an object or necessary properties of a nested object, which is equivocal to either removing " +
                    "the object or removing it from a list/set.  Here, you are trying to remove "+currentProperty.getQualifiedName().toString() +
                    ", which is not an object property nor is it a top level property for api call "+restElement.getQualifiedName().toString()+
                    "and operation "+mOp+".");
        }

    }

    // TODO: move this to a dedicated validator
    private void validateThatPropertyIsNotNecessary(TypeElement restElement, TypeElement baseElement, TypeElement currentProperty,
                                                    List<TypeElement> necessaries, ProcessingEnvironment processingEnvironment) {
        for (TypeElement typeElement : necessaries) {
            if (currentProperty.getQualifiedName().toString().equals(typeElement.getQualifiedName().toString())) {
                processingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, "The property "
                        +currentProperty.getQualifiedName().toString()+" for api call "+restElement.getQualifiedName().toString()+
                        "and operation "+mOp+" is a necessary property of the base type "+baseElement.getQualifiedName().toString()+
                        " and should not be included in the property list.");
                return;
            }
        }
    }

    // TODO: move this to a dedicated validator
    private void validateThatCurrentPropertyIsReachableFromTheBaseType(TypeElement restElement, TypeElement currentProperty, TypeElement baseType,
                                                                       Map<String, TypeTree> typeTrees,
                                                                       ProcessingEnvironment processingEnvironment) {
        List<TypeElement> superAndSub = Utils.getSuperAndSubTypes(typeTrees.get(baseType.getQualifiedName().toString()));
        for (TypeElement typeElement : superAndSub) {
            PackageElement packageElement = MoreElements.getPackage(typeElement);
            for (Element element : packageElement.getEnclosedElements()) {
                TypeElement asType = MoreElements.asType(element);
                if (asType.getQualifiedName().toString().equals(currentProperty.getQualifiedName().toString())) {
                    return;
                }
            }
        }
        processingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, "Properties must be reachable from their base type. " +
                "In the API endpoint "+restElement+ " for the method "+mOp+", the property "
                +currentProperty.getQualifiedName().toString()+" is not reachable from the (possibly inferred) base type "+
                baseType.getQualifiedName().toString()+".  If you're pretty sure that the property you're using makes sense in "+
                "the API call, then verify that $.class separators are correctly placed.  Remember: "+
                "$.class should be used to end an inner object in an API definition.  If multiple objects end at the same place," +
                " all levels must be closed with a $.class.  Think of $.class like closing curly brackets.");
    }

    // TODO: move this to a dedicated validator
    private void validateThatCurrentPropertyIsAProperty(TypeElement restElement, TypeElement currentProperty,
                                                                       ProcessingEnvironment processingEnvironment) {

            for (AnnotationMirror annotationMirror : currentProperty.getAnnotationMirrors()) {
                if (isProperty(annotationMirror) || isPropertySet(annotationMirror) || isPropertyList(annotationMirror)) {
                    return;
                }
            }
        processingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, "The property "+currentProperty+
        " in rest type "+restElement+" is not annotated with a property annotation.  It needs to be annotated with"+
        " @Property, @PropertySet or @PropertyList.");
    }

    private void addToPropertyToInfoMap(PropertyToInfoMap propertyToInfoMap, TypeElement currentProperty, String pathReference) {
        String currentPropertyName = currentProperty.getQualifiedName().toString();
        String pathReferenceForThisProperty = pathReference + currentProperty.getSimpleName().toString();
        if (!propertyToInfoMap.containsKey(currentPropertyName)) {
            propertyToInfoMap.put(currentPropertyName, new PropertyInfo());
        }
        propertyToInfoMap.get(currentPropertyName).paths
                .add(ImmutablePair.of(pathReferenceForThisProperty, OperationUtils.fromString(mOp)));
        propertyToInfoMap.get(currentPropertyName).isUID = hasUIDAnnotation(currentProperty);
        propertyToInfoMap.get(currentPropertyName).propertyName =
                currentProperty.getSimpleName().toString();
        propertyToInfoMap.get(currentPropertyName).propertyManyness =
                Utils.isPropertyList(Utils.getPropertyAnnotation(currentProperty))
                        ? PropertyManyness.LIST
                        : Utils.isPropertySet(Utils.getPropertyAnnotation(currentProperty))
                        ? PropertyManyness.SET
                        : PropertyManyness.SIMPLE;
    }

    private static final String TYPE = "type";
    private static final String ADDUIDS = "HelperMethods.addUIDs";
    private static final String CANDIDATES = "candidates";

    /**
     * Recursive method to build all objects used by braque.
     * For example, at the rest endpoint foo, we may have objects that need to reside at
     * foo/bar/*, foo/bar/baz, etc.  This builds all of those objects - their interfaces,
     * their implementations, and their shadow implementations used by the Transformer.
     * @param pkg The package of the braque objects.
     * @param restElement The rest element of this call (i.e. TeamInfo)
     * @param attachment The type that has the object's UID, usually at the top of a type hierarchy.
     * @param baseType The base type we are inheriting from.  Usually the same as attachment but not necessarily.
     *                 For example, if our hierarchy is A->B->C, the base type can be B and the attachment A.
     *                 This will mean that if subtype A->X->Y exists, Y is not a valid object candidate because it
     *                 does not inherit from the base type.
     * @param typeTrees Type trees from which we can glean hierarchy information.
     * @param properties A list of properties being assigned to this object or its sub objects.
     * @param comingFrom A string indicating what rest object we're coming from, used to build interface names.
     *                   For example, if we're at Me Show and then we recurse down to players, comingFrom changes from
     *                   MeShow to MeShowPlayers.
     * @param pos The position in the properties array.
     * @param path The braque path represented in the StringProvisioner.  Duplicated here
     *             to pass to the Clojure handler.  TODO: find a way to merge this duplicated logic.
     * @param genericCollector Collects names of interface generics.
     * @param propertyToInfoMap Collects property information for the fanner.
     * @param pathInfoMap Collects path information for the fanner.
     * @param serializer A code builder representing the serializer.
     * @param deserializer A code builder representing the deserializer.
     * @param transformer A code builder representing the transformer
     * @param processingEnvironment The processing environment to which we write *.java files.
     * @return
     */
    private int buildRestType(String pkg, TypeElement restElement, TypeElement attachment,
                              TypeElement baseType,  Map<String, TypeTree> typeTrees,
                              List<TypeElement> properties, String comingFrom,
                              int pos, String path, final String[] genericCollector,
                              PropertyToInfoMap propertyToInfoMap,
                              PathInfoMap pathInfoMap,
                              CodeBuilder serializer, CodeBuilder deserializer,
                              CodeBuilder transformer,
                              ProcessingEnvironment processingEnvironment) {
        Map<TypeElement, List<TypeElement>> subtypeMap = new LinkedHashMap<TypeElement, List<TypeElement>>();
        Utils.getSubTypes(typeTrees.get(attachment.getQualifiedName().toString()), subtypeMap);
        final int startPos = pos;

        PropertyToInterfaceMap propertyToInterfaceMap = new PropertyToInterfaceMap();
        PropertyToTypeMap propertyToTypeMap = new PropertyToTypeMap();

        String pathReference = restElement.getSimpleName() + comingFrom;
        String interfaceName = restElement.getSimpleName() + mOp + comingFrom;
        String eltType = interfaceName + baseType.getSimpleName();
        genericCollector[0] = eltType;

        List<TypeElement> necessaries = Utils.getAllNecessaryElements(typeTrees.get(baseType.getQualifiedName().toString()));

        for (TypeElement currentProperty : necessaries) {
            addToPropertyToInfoMap(propertyToInfoMap, currentProperty, pathReference);
            String pathReferenceForThisProperty = pathReference + currentProperty.getSimpleName().toString();
            String typeName = Utils.getPropertyTypeElement(currentProperty).getQualifiedName().toString();
            addToPathInfoMap(pathInfoMap, pathReferenceForThisProperty, typeName, typeTrees);
        }

        while (pos < properties.size()) {
            if (Utils.DOLLAR.equals(properties.get(pos).getSimpleName().toString())) {
                pos++;
                break;
            }
            TypeElement currentProperty = properties.get(pos);
            validateThatCurrentPropertyIsAProperty(restElement, currentProperty, processingEnvironment);
            String currentPropertyName = currentProperty.getQualifiedName().toString();
            String pathReferenceForThisProperty = pathReference + currentProperty.getSimpleName().toString();
            addToPropertyToInfoMap(propertyToInfoMap, currentProperty, pathReference);

            propertyToInterfaceMap.put(currentProperty, new ArrayList<String>());
            TypeElement typeElementOfProperty = Utils.getPropertyTypeElement(currentProperty);
            boolean isPropertyCollection = Utils.isPropertyList(Utils.getPropertyAnnotation(currentProperty))
                    || Utils.isPropertySet(Utils.getPropertyAnnotation(currentProperty));
            String[] iFaces = shouldInludeSetter()
                    ? isPropertyCollection
                    ? new String[]{Utils.CTOR, Utils.GET, Utils.ADD, Utils.REMOVE}
                    : new String[]{Utils.CTOR, Utils.GET, Utils.SET}
                    : new String[]{Utils.CTOR, Utils.GET};
            if (Utils.hasTypeAnnotation(typeElementOfProperty)) {
                // we put null because
                addToPathInfoMap(pathInfoMap, pathReferenceForThisProperty,
                        typeElementOfProperty.getQualifiedName().toString(), typeTrees);
                String[] internalGenericCollector = new String[1];
                pos += buildRestType(pkg, restElement,
                        getSuperType(typeTrees.get(typeElementOfProperty.getQualifiedName().toString())),
                        typeElementOfProperty, typeTrees,
                        properties, (comingFrom == null ? "" : comingFrom) + currentProperty.getSimpleName(),
                        pos + 1, (path+"/"+currentProperty.getSimpleName()+(isPropertyCollection + "/*")).toLowerCase(),
                        internalGenericCollector, propertyToInfoMap, pathInfoMap,
                        serializer, deserializer, transformer, processingEnvironment);
                String base = Utils.braque(currentPropertyName);
                for (String iFace : iFaces) {
                    propertyToInterfaceMap.get(currentProperty).add(base + iFace + "<" + internalGenericCollector[0] + ">");
                }
                propertyToTypeMap.put(currentPropertyName,
                        new TypeElementAnnotationMirrorPair(internalGenericCollector[0],
                                Utils.getPropertyAnnotation(currentProperty), currentProperty));
            } else {

                validateThatPropertyIsNotNecessary(restElement, baseType, currentProperty, necessaries, processingEnvironment);
                validateThatSimpleDestroyedPropertiesAreOnlyTopLevel(comingFrom, restElement, currentProperty, processingEnvironment);
                validateThatCurrentPropertyIsReachableFromTheBaseType(restElement, currentProperty, baseType, typeTrees, processingEnvironment);

                String typeName = Utils.getPropertyTypeElement(currentProperty).getQualifiedName().toString();
                addToPathInfoMap(pathInfoMap, pathReferenceForThisProperty, typeName, typeTrees);
                propertyToTypeMap.put(currentPropertyName,
                        new TypeElementAnnotationMirrorPair(typeName,
                                Utils.getPropertyAnnotation(currentProperty), currentProperty));
                String base = Utils.braque(currentPropertyName);
                for (String iFace : iFaces) {
                    propertyToInterfaceMap.get(currentProperty).add(base + iFace);
                }
            }
            pos++;
        }


        TypeElement uid = getUID(attachment);

        if (Operation.SHOW.equals(OperationUtils.fromString(mOp))) {
            String eltPackagized = packagize(eltType, pkg);
            deserializer.nsp().statik().priv().as(eltPackagized).a(DESERIALIZE).a(interfaceName)
                    .P().phinal().sojmap().a(SERIALIZED).cs().phinal().sjlist().a(PREVIOUSUIDS).cs().bool().a("inferType").psbeg()
                    .sp().str()._as(TYPE).eq().a("HelperMethods.safeCastString").P().a(SERIALIZED).d().a("get")
                    .P().a(ADDUIDS).P().stringProvPath().a(pathReference)._a(TYPE)
                    .Pp().cs().a(PREVIOUSUIDS).ppp().cn()
                    .sp().jlist(new CB().braqued().a("HelperMethods.ScoredReturnType").Gg(eltPackagized).toS()).__as(CANDIDATES)
                    .eq().arraylist().cn();
        }

        serializer.n();
        if (comingFrom == null || "".equals(comingFrom)) {
            serializer.sp().statik().pubic().sojmap().a(SERIALIZE)
                    .P().phinal().as(packagize(eltType, pkg)).a(DESERIALIZED).psbeg()
                    .sp().sjlist().as(PREVIOUSUIDS).eq().arraylist().cn()
                    .sp().a(PREVIOUSUIDS).dadd().a(DESERIALIZED).d().get(uid).p().cn()
                    .spret().a(SERIALIZE).a(interfaceName).P().a(DESERIALIZED).cs().a(PREVIOUSUIDS).pcn()
                    .spend().n();
        }
        serializer.sp().statik().priv().sojmap().a(SERIALIZE).a(interfaceName)
                .P().phinal().as(packagize(eltType, pkg)).a(DESERIALIZED).cs().phinal().sjlist().a(PREVIOUSUIDS).psbeg()
                .sp().sojmap().as("out").eq().hashmap().cn();

        recurseOverSubTypesAndBuildSourceFiles(pkg, null, null, restElement, attachment, baseType, subtypeMap,
                typeTrees, comingFrom, path.toLowerCase(), propertyToInterfaceMap, new ArrayList<String>(),
                new PropertyToTypeMap(), new PropertyToTypeMap(), propertyToTypeMap, true,
                serializer, deserializer, transformer, processingEnvironment);

        serializer.spret().a("out").cn().spend();
        if (Operation.SHOW.equals(OperationUtils.fromString(mOp))) {
            deserializer.sp().a("HelperMethods.sortScoredReturnTypes(__candidates)").cn()
                    .sp().a("if (!__candidates.isEmpty())").sbeg()
                    .spret().a("__candidates.get(__candidates.size() - 1).mBraqueObject").cn()
                    .spend()
                    .sp().a("if (!inferType)").sbeg()
                    .spret().a(DESERIALIZE).a(interfaceName).P().a(SERIALIZED).cs().a(PREVIOUSUIDS).cs().a("true)").cn()
                    .spend()
                    .spret().nul().cn().spend();
        }

        return pos - startPos;
    }

    /**
     * Adds path information to the path info map.  Path information maps
     * strings representing paths ("me/uid/favoriteplayers") to a collection of types that could reside at the path
     * (FootballPlayer, BaseballPlayer)
     * @param pathInfoMap the path info map
     * @param path the path that will act as a key
     * @param baseType the base type that the path points to
     * @param typeTreeMap a type tree map to help determine terminal subtypes (any subtypes that are not abstract
     *                    of the base type)
     */
    private void addToPathInfoMap(PathInfoMap pathInfoMap, String path, String baseType, Map<String, TypeTree> typeTreeMap) {
        if (!pathInfoMap.containsKey(path)) {
            pathInfoMap.put(path, new HashSet<String>());
        }
        if (!isComplexProperty(baseType)) {
            pathInfoMap.get(path).add(baseType);
        } else {
            for (TypeElement typeElement : Utils.getTerminalSubTypes(typeTreeMap.get(baseType))) {
                pathInfoMap.get(path).add(braque(typeElement.getQualifiedName().toString()));
            }
        }
    }

    /**
     * Entry into the handler
     * @param restElement the element representing the rest endpoint, for example "TeamInfo"
     * @param typeTreeMap a map linking type names to TypeTree objects that represent its hierarchy
     * @param serializer a CodeBuilder to build Serializer.java
     * @param deserializer a CodeBuilder to build Deserializer.java
     * @param transformer a CodeBuilder to build Transformer.java
     * @param propertyToInfoMap a map that is filled progressively with information about properties and used to build Fanner.java
     * @param pathInfoMap a map that is filled progressively with information about API endpoints and used to build Fanner.java
     * @param processingEnvironment the processing environment that we use to write files and issue warning messages
     */
    void handleRestObject(TypeElement restElement, Map<String, TypeTree> typeTreeMap,
                          CodeBuilder serializer, CodeBuilder deserializer, CodeBuilder transformer,
                          PropertyToInfoMap propertyToInfoMap,
                          PathInfoMap pathInfoMap,
                          ProcessingEnvironment processingEnvironment) {
        for (AnnotationMirror annotationMirror : restElement.getAnnotationMirrors()) {
            if (isVisitable(annotationMirror)) {
                String pkg = Utils.getPackage(restElement);
                String newPkg = pkg + Utils.DOT + Utils.GENERATED_OBJECTS_PACKAGE_NAME;
                TypeElement maybeBaseType = Utils.getAnnotationMirrorValueAsTypeElement(annotationMirror, Utils.BASETYPE);
                TypeElement attachment =
                        Utils.getPropertyAttachment(Utils.getAnnotationMirrorValueAsTypeElement(annotationMirror,Utils.ARGUMENT));
                if (maybeBaseType == null) {
                    maybeBaseType = attachment;
                }
                addToPathInfoMap(pathInfoMap, restElement.getSimpleName().toString(),
                        maybeBaseType.getQualifiedName().toString(), typeTreeMap);
                buildRestType(newPkg, restElement,
                        attachment, maybeBaseType,  typeTreeMap,
                        Utils.getAnnotationMirrorValueArrayAsTypeElementList(annotationMirror, Utils.PROPERTIES),
                        "", 0, ("/"+restElement.getSimpleName()+"/*").toLowerCase(),
                        new String[1], propertyToInfoMap, pathInfoMap, serializer, deserializer,
                        transformer, processingEnvironment);
            }
        }
    }
}
