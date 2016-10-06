package braque.internal.codegen;

import org.apache.commons.lang3.tuple.Pair;

import java.util.LinkedHashMap;

import javax.lang.model.element.AnnotationMirror;

/**
 * Maps properties to their types.
 * The second pair contains the raw type on the left and an annotation mirror on the right with the raw type.
 * The latter is useful in determining if it is a @PropertySet, @PropertyList or @Property.
 * Created by mikesolomon on 18/09/16.
 */

class PropertyToTypeMap extends LinkedHashMap<String, TypeElementAnnotationMirrorPair> {
    PropertyToTypeMap() {
        super();
    }
    PropertyToTypeMap(PropertyToTypeMap p) {
        super(p);
    }
}
