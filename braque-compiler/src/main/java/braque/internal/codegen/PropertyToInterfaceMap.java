package braque.internal.codegen;

import java.util.HashMap;
import java.util.List;

import javax.lang.model.element.TypeElement;

/**
 * Shorthand for a map that maps properties to interfaces for getters, setters etc.
 * Created by mikesolomon on 18/09/16.
 */

class PropertyToInterfaceMap extends HashMap<TypeElement, List<String>> {
    PropertyToInterfaceMap() {
        super();
    }
    PropertyToInterfaceMap(PropertyToInterfaceMap e) {
        super(e);
    }
}
