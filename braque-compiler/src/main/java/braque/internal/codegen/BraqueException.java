package braque.internal.codegen;

/**
 * A generic exception for issues with processing annotations.
 * Created by mikesolomon on 19/09/16.
 */

class BraqueException extends RuntimeException {
    BraqueException(Exception e) {
        super(e);
    }
    BraqueException(String s) {
        super(s);
    }
}
