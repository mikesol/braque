package braque;

/**
 * <p>A base object from which all other objects must inherit.
 * This is done under the hood via the annotation processor.</p>
 *
 * <p>Created by mikesolomon on 10/09/16.</p>
 */

public interface BraqueObject<T extends BraqueObject> {
    T klone();
}
