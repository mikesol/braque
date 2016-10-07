package braque.internal.codegen;

import com.google.auto.common.BasicAnnotationProcessor;
import com.google.auto.common.MoreElements;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;
import braque.Create;
import braque.Destroy;
import braque.Property;
import braque.PropertyList;
import braque.PropertySet;
import braque.Show;
import braque.Type;
import braque.Update;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

/**
 * Annotation processor for all braque annotations.
 * Created by mikesolomon on 09/09/16.
 */

@SupportedAnnotationTypes({
        Utils.BRAQUE + Utils.DOT + "Type",
        Utils.BRAQUE + Utils.DOT + "Property",
        Utils.BRAQUE + Utils.DOT + "PropertyList",
        Utils.BRAQUE + Utils.DOT + "PropertySet",
        Utils.BRAQUE + Utils.DOT + "Show",
        Utils.BRAQUE + Utils.DOT + "Create",
        Utils.BRAQUE + Utils.DOT + "Update",
        Utils.BRAQUE + Utils.DOT + "Destroy",
        Utils.BRAQUE + Utils.DOT + "Clojure"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
@SupportedOptions({"makeObjectFieldsPublic",
        "braqueGeneratedObjectsPackageName",
        "braqueStaticMethodClassesFullyQualifiedPackageName"})
public class BraqueAnnotationProcessor extends BasicAnnotationProcessor {

    public BraqueAnnotationProcessor() {
        super();
    }


    @Override
    protected Iterable<? extends ProcessingStep> initSteps() {
        Utils.setProcessingEnvironment(processingEnv);
        if (!processingEnv.getOptions().containsKey("braqueGeneratedObjectsPackageName")) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "using default generated objects package name: "+Utils.GENERATED_OBJECTS_PACKAGE_NAME
            +". Usually this is fairly harmless, but to change this, set the \"braqueGeneratedObjectsPackageName\" argument to something sensible like \"happiness\" or \"glee\".");
        } else {
            Utils.GENERATED_OBJECTS_PACKAGE_NAME = processingEnv.getOptions().get("braqueGeneratedObjectsPackageName");
        }
        if (!processingEnv.getOptions().containsKey("braqueStaticMethodClassesFullyQualifiedPackageName")) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "using default static method classes fully qualified package name: "+
                    Utils.STATIC_METHOD_CLASSES_PACKAGE+".  This is generally a bad idea, as it may lead to namespace conflicts.  Please change "+
                    "this by setting the annotation processor the property \"braqueStaticMethodClassesFullyQualifiedPackageName\" to something "+
            "sensible like \"my.fully.qualified.package.name\" or \"braque.rocks.my.socks\".");
        } else {
            Utils.STATIC_METHOD_CLASSES_PACKAGE = processingEnv.getOptions().get("braqueGeneratedObjectsPackageName");
        }
        List<ProcessingStep> processingSteps = new ArrayList<>();
        // TYPE VALIDATION STEP
        processingSteps.add(new ProcessingStep() {
            @Override
            public Set<? extends Class<? extends Annotation>> annotations() {
                return ImmutableSet.<Class<? extends Annotation>>of(Type.class);
            }

            @Override
            public Set<Element> process(SetMultimap<Class<? extends Annotation>, Element> elementsByAnnotation) {

                TypeValidator.validateTypes(elementsByAnnotation.values(), processingEnv);
                return ImmutableSet.of();
            }
        });
        // TYPE STEP
        processingSteps.add(new ProcessingStep() {
            @Override
            public Set<? extends Class<? extends Annotation>> annotations() {
                return ImmutableSet.<Class<? extends Annotation>>of(Type.class);
            }

            @Override
            public Set<Element> process(SetMultimap<Class<? extends Annotation>, Element> elementsByAnnotation) {
                for (Element element : elementsByAnnotation.values()) {
                    TypeHandler.handleType(MoreElements.asType(element), processingEnv);
                }
                return ImmutableSet.of();
            }
        });
        // PROPERTY STEP
        processingSteps.add(new ProcessingStep() {
            @Override
            public Set<? extends Class<? extends Annotation>> annotations() {
                return ImmutableSet.of(Property.class, PropertyList.class, PropertySet.class);
            }

            @Override
            public Set<Element> process(SetMultimap<Class<? extends Annotation>, Element> elementsByAnnotation) {
                for (Element element : elementsByAnnotation.values()) {
                    new PropertyHandler(PropertyHandler.PropertyType.SINGLE).handleProperty(MoreElements.asType(element), processingEnv);
                    new PropertyHandler(PropertyHandler.PropertyType.SET).handleProperty(MoreElements.asType(element), processingEnv);
                    new PropertyHandler(PropertyHandler.PropertyType.LIST).handleProperty(MoreElements.asType(element), processingEnv);
                }
                return ImmutableSet.of();
            }
        });

        // String provisioner step
        processingSteps.add(new ProcessingStep() {
            @Override
            public Set<? extends Class<? extends Annotation>> annotations() {
                return ImmutableSet.of(Type.class, Show.class, Update.class, Create.class, Destroy.class);
            }

            @Override
            public Set<Element> process(SetMultimap<Class<? extends Annotation>, Element> elementsByAnnotation) {
                StringHandler.handleElements(elementsByAnnotation, processingEnv);
                return ImmutableSet.of();
            }
        });

        // REST interface step
        processingSteps.add(new ProcessingStep() {
            @Override
            public Set<? extends Class<? extends Annotation>> annotations() {
                return ImmutableSet.of(Type.class, Show.class, Update.class, Create.class);
            }

            @Override
            public Set<Element> process(SetMultimap<Class<? extends Annotation>, Element> elementsByAnnotation) {
                List<TypeElement> typedElements = new ArrayList<>();
                for (Map.Entry<Class<? extends Annotation>, Element> entry : elementsByAnnotation.entries()) {
                    if (entry.getKey().getSimpleName().equals("Type")) {
                        typedElements.add(MoreElements.asType(entry.getValue()));
                    }
                }
                CodeBuilder serializer = new CodeBuilder();
                CodeBuilder deserializer = new CodeBuilder();
                CodeBuilder transformer = new CodeBuilder();
                String newPkg = Utils.STATIC_METHOD_CLASSES_PACKAGE;
                serializer.pkg(newPkg).pubic().klass().as("Serializer").impl().processing().a("Serializer").sbeg();
                deserializer.pkg(newPkg).pubic().klass().as("Deserializer").impl().processing().a("Deserializer").sbeg();
                transformer.pkg(newPkg).pubic().klass().as("Transformer").beg();
                GenerateHelperMethods.makeHelperMethods(processingEnv);
                Map<String, TypeTree> typeTreeMap = Utils.buildTypeTree(typedElements);
                Set<String> alreadyProcessed = new HashSet<>();
                //Utils.processingEnvironment = processingEnv;
                PropertyToInfoMap propertyToInfoMap = new PropertyToInfoMap();
                PathInfoMap pathInfoMap = new PathInfoMap();
                for (Element element : elementsByAnnotation.values()) {
                    if (alreadyProcessed.contains(MoreElements.asType(element).getQualifiedName().toString())) {
                        continue;
                    }
                    alreadyProcessed.add(MoreElements.asType(element).getQualifiedName().toString());
                    new ShowHandler().handleRestObject(MoreElements.asType(element), typeTreeMap, serializer, deserializer,
                            transformer, propertyToInfoMap, pathInfoMap, processingEnv);
                    new UpdateHandler().handleRestObject(MoreElements.asType(element), typeTreeMap, serializer, deserializer,
                            transformer, propertyToInfoMap, pathInfoMap, processingEnv);
                    new CreateHandler().handleRestObject(MoreElements.asType(element), typeTreeMap, serializer, deserializer,
                            transformer, propertyToInfoMap, pathInfoMap, processingEnv);
                    new DestroyHandler().handleRestObject(MoreElements.asType(element), typeTreeMap, serializer, deserializer,
                            transformer, propertyToInfoMap, pathInfoMap, processingEnv);
                }

                SerializerHelper.addStaticFooterFunctions(serializer, elementsByAnnotation);
                DeserializerHelper.addStaticFooterFunctions(deserializer, elementsByAnnotation);
                serializer.spend();
                transformer.spend();
                deserializer.spend();
                Utils.write(newPkg+".Serializer", serializer, processingEnv);
                Utils.write(newPkg+".Deserializer", deserializer, processingEnv);
                Utils.write(newPkg+".Transformer", transformer, processingEnv);

                FannerHandler.fan(propertyToInfoMap, pathInfoMap, typeTreeMap, elementsByAnnotation, processingEnv);
                return ImmutableSet.of();
            }
        });

        return processingSteps;
    }
}
