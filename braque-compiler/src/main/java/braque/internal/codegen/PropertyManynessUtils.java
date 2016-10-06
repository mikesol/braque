package braque.internal.codegen;

import braque.PropertyManyness;

/**
 * Utility functions for dealing with operations
 * Created by mikesolomon on 14/09/16.
 */

class PropertyManynessUtils {
    static String toString(PropertyManyness propertyManyness) {
        switch (propertyManyness) {
            case LIST:
                return "LIST";
            case SET:
                return "SET";
            case SIMPLE:
            default:
                return "SIMPLE";
        }
    }
}
