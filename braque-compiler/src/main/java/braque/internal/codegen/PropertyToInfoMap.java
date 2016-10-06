package braque.internal.codegen;

import java.util.LinkedHashMap;

/**
 * Maps properties to paths, used by fanner.
 * Created by mikesolomon on 18/09/16.
 */

class PropertyToInfoMap extends LinkedHashMap<String, PropertyInfo> {
    PropertyToInfoMap() {
        super();
    }
    PropertyToInfoMap(PropertyToInfoMap p) {
        super(p);
    }
}
