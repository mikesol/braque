package braque.internal.codegen;

import braque.PropertyManyness;
import clojure.java.api.Clojure;
import clojure.lang.*;
import clojure.lang.Compiler;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static braque.internal.codegen.Utils.*;

/**
 * Injects results from a Clojure function into code.
 * Created by mikesolomon on 21/09/16.
 */
public class ClojureHelper {
    enum Place {
        CLASSDEF,
        FIELDDEF,
        CTOR,
        GET,
        SET,
        ADD,
        REMOVE
    }

    static private String placeToString(Place place) {
        switch (place) {
            case CLASSDEF:
                return "classdef";
            case FIELDDEF:
                return "fielddef";
            case GET:
                return "get";
            case SET:
                return "set";
            case CTOR:
                return "ctor";
            case ADD:
                return "add";
            case REMOVE:
                return "remove";
        }
        return null;
    }

    static private final RandGen mRandom = new RandGen();

    TypeElement mPropElt;
    TypeElement mRestElt;
    TypeElement mTypeElt;
    Place mPlace;
    String mName;
    String mKlass;
    TypeTree mTypeTree;
    String mIface;
    String mType;
    String mPath;
    PropertyManyness mPropertyManyness;
    PropertyToTypeMap mDeclarationsPropertyToTypeMap;
    PropertyToTypeMap mSuperPropertyToTypeMap;

    private void initVariableFields(TypeElement propElt, Place place, String name, PropertyManyness propertyManyness) {
        mPropElt = propElt;
        mPlace = place;
        mName = name;
        mPropertyManyness = propertyManyness;
    }

    void initVariableFieldsAndInject(TypeElement propElt, Place place, String name, PropertyManyness propertyManyness, String path, CodeBuilder codeBuilder) {
        mPropElt = propElt;
        mPlace = place;
        mName = name;
        mPropertyManyness = propertyManyness;
        mPath = path;
        inject(codeBuilder);
    }

    ClojureHelper(TypeElement propElt, TypeElement restElt, TypeElement typeElt, Place place, String name, String klass,
                  TypeTree typeTree, String iface, String type, String path, PropertyManyness propertyManyness,
                  PropertyToTypeMap declarationsPropertyToTypeMap, PropertyToTypeMap superPropertyToTypeMap) {
        mPropElt = propElt;
        mRestElt = restElt;
        mTypeElt = typeElt;
        mPlace = place;
        mName = name;
        mKlass = klass;
        mTypeTree = typeTree;
        mIface = iface;
        mType = type;
        mPath = path;
        mPropertyManyness = propertyManyness;
        mDeclarationsPropertyToTypeMap = declarationsPropertyToTypeMap;
        mSuperPropertyToTypeMap = superPropertyToTypeMap;
    }

    private String makeObjDetails(TypeElementAnnotationMirrorPair details) {
        return new CB().a("{:type").q(details.getLeft())
                .a(" :many '")
                .a(isPropertyList(details.getRight()) ? "list" : isPropertySet(details.getRight()) ? "set" : "simple")
                .a("}").toS();
    }

    private String makeObjDef(PropertyToTypeMap propertyToTypeMap) {
        List<String> elts = new ArrayList<>();
        for (Map.Entry<String, TypeElementAnnotationMirrorPair> entry : propertyToTypeMap.entrySet()) {
            elts.add(":" + last(entry.getKey()) + " " + makeObjDetails(entry.getValue()));
        }
        return "{" + StringUtils.join(elts, " ") + "}";
    }

    List<String> inject(TypeElement typeElement, List<String> previousAnnotations) {
        if (typeElement == null) {
            return new ArrayList<>();
        }
        AnnotationMirror clojureAnnotation = getClojureAnnotation(typeElement);
        if (clojureAnnotation == null) {
            return new ArrayList<>();
        }

        List<String> out = new ArrayList<>();
        ClassLoader previousLoader = Thread.currentThread().getContextClassLoader();
        List<String> hierarchyStrings = new ArrayList<>();

        Thread.currentThread().setContextClassLoader(BraqueAnnotationProcessor.class.getClassLoader());
        try {
            String clojure = getAnnotationMirrorValueAsString(clojureAnnotation);

            Object place = Clojure.read("'" + placeToString(mPlace));
            String name = mName;
            String klass = mKlass;
            List<TypeElement> hierarchyTypes = getClassHierarchy(mTypeTree);
            for (TypeElement stage : hierarchyTypes) {
                hierarchyStrings.add("\"" + stage.getQualifiedName().toString() + "\"");
            }
            Object klasshierarchy = Clojure.read("(list " + StringUtils.join(hierarchyStrings, " ") + ")");
            String iface = mIface;
            String type = mType;
            Object many = Clojure.read("'" + PropertyManynessUtils.toString(mPropertyManyness).toLowerCase());
            String path = mPath;
            List<String> previousAnnotationsQuoted = new ArrayList<>();
            for (String prev : previousAnnotations) {
                previousAnnotationsQuoted.add("\"" + prev + "\"");
            }
            Object previous = Clojure.read("'(" + StringUtils.join(previousAnnotationsQuoted, " ") + ")");
            Object objdef = Clojure.read("{:current " + makeObjDef(mDeclarationsPropertyToTypeMap) + " :super " + makeObjDef(mSuperPropertyToTypeMap) + "}");

            String clojureFn = "(ns braque) (defn " + mRandom.next()
                    + " [place name klass klasshierarchy iface type many path previous objdef] " + clojure + ")";
            Compiler.load(new StringReader(clojureFn));

            Var bar = RT.var("braque", mRandom.current());
            Object result = bar.invoke(place, name, klass, klasshierarchy, iface, type, many, path, previous, objdef);
            if (result != null && result instanceof ISeq) {
                do {
                    Object first = ((ISeq) result).first();
                    if (first != null && first instanceof String) {
                        out.add((String) first);
                    }
                    result = ((ISeq) result).next();
                } while (result != null);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(previousLoader);
            return out;
        }
    }

    private void inject(CodeBuilder cb) {
        List<String> out = new ArrayList<>();
        List<String> prev = new ArrayList<>();
        for (TypeElement typeElement : new TypeElement[]{mPropElt, mTypeElt, mRestElt}) {
            List<String> curr = inject(typeElement, prev);
            out.addAll(curr);
            prev = curr;
        }
        for (String annotation : out) {
            cb.sp().a(annotation).n();
        }
    }
}
