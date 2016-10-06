package braque.internal.codegen;

import org.apache.commons.lang3.tuple.ImmutablePair;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.TypeElement;

/**
 * Simple storage for types, annotation mirrors and their property elements
 * Created by mikesolomon on 21/09/16.
 */
class TypeElementAnnotationMirrorPair  {
    final String mTypeElement;
    final AnnotationMirror mAnnotationMirror;
    final TypeElement mPropElement;

    TypeElementAnnotationMirrorPair(String typeElement, AnnotationMirror annotationMirror, TypeElement propElement) {
        mTypeElement = typeElement;
        mAnnotationMirror = annotationMirror;
        mPropElement = propElement;
    }
    String getLeft() {
        return mTypeElement;
    }
    String getKey() {
        return getLeft();
    }
    AnnotationMirror getRight() {
        return mAnnotationMirror;
    }
    AnnotationMirror getValue() {
        return mAnnotationMirror;
    }
    TypeElement getPropElement() {
        return mPropElement;
    }
}
