package braque.internal.codegen;

import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;

import static braque.internal.codegen.Utils.getAnnotationMirrorValueArrayAsTypeElementList;

/**
 * Handles the @Type annotation
 * Created by mikesolomon on 11/09/16.
 */

class TypeHandler {

    private static void buildBaseType(String pkg, TypeElement typeElement, ProcessingEnvironment processingEnvironment) {
        CodeBuilder builder = new CodeBuilder();
        builder.pkg(pkg).pubic().iface().$as(typeElement).beg().spend();
        Utils.write(pkg+ Utils.DOT+ Utils.DOLLAR+typeElement.getSimpleName(), builder, processingEnvironment);
    }

    private static void buildType(String pkg, List<TypeElement> implementations, TypeElement typeElement, ProcessingEnvironment processingEnvironment) {
        CodeBuilder builder = new CodeBuilder();
        builder.pkg(pkg).pubic().iface().a(typeElement).G().as("T").ext().a(typeElement.getSimpleName().toString()).gs().ext().$a(typeElement);
        boolean isSubtype = false;
        for (TypeElement implementation : implementations) {
            boolean gen = false;
            if (Utils.hasTypeAnnotation(implementation)) {
                isSubtype = true;
                gen = true;
            }
            builder.cns().a(Utils.braque(implementation.getQualifiedName().toString()));
            if (gen) {
                builder.Gg("T");
            }
        }
        if (!isSubtype) {
            builder.cns().processing().a("BraqueObject").Gg("T");
        }
        builder.sbeg().spend();
        Utils.write(pkg+ Utils.DOT+typeElement.getSimpleName(), builder, processingEnvironment);
    }

    static void handleType(TypeElement typeElement, ProcessingEnvironment processingEnvironment) {
        for (AnnotationMirror annotationMirror : typeElement.getAnnotationMirrors()) {
            if (Utils.isType(annotationMirror)) {
                String pkg = Utils.getPackage(typeElement);
                String newPkg = pkg+ Utils.DOT+Utils.GENERATED_OBJECTS_PACKAGE_NAME;
                List<TypeElement> implementations = Utils.getAnnotationMirrorValueArrayAsTypeElementList(annotationMirror);
                buildBaseType(newPkg, typeElement, processingEnvironment);
                buildType(newPkg, implementations, typeElement, processingEnvironment);
            }
        }
    }
}
