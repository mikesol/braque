package braque.internal.codegen;

import braque.Operation;

import static braque.Operation.CREATE;
import static braque.Operation.DESTROY;
import static braque.Operation.SHOW;
import static braque.Operation.UPDATE;


/**
 * Utility functions for dealing with operations
 * Created by mikesolomon on 14/09/16.
 */

class OperationUtils {
    static Operation fromString(String s) {
        switch (s) {
            case Utils.CREATE:
                return CREATE;
            case Utils.UPDATE:
                return UPDATE;
            case Utils.DESTROY:
                return DESTROY;
            case Utils.SHOW:
            default:
                return SHOW;
        }
    }
    static String toUcaseString(Operation op) {
        switch (op) {
            case CREATE:
                return "CREATE";
            case UPDATE:
                return "UPDATE";
            case DESTROY:
                return "DESTROY";
            case SHOW:
            default:
                return "SHOW";
        }
    }
    static String toLcaseString(Operation op) {
        switch (op) {
            case CREATE:
                return Utils.CREATE;
            case UPDATE:
                return Utils.UPDATE;
            case DESTROY:
                return Utils.DESTROY;
            case SHOW:
            default:
                return Utils.SHOW;
        }
    }
}
