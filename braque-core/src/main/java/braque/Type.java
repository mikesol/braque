package braque;

/**
 * <p>Represents a type, meaning something that will inherit from BraqueObject.</p>
 * <p>Created by mikesolomon on 10/09/16.</p>
 */

public @interface Type {
    /**
     * @return At most one supertype and as many necessary properties as your heart desires.
     * Necessary properties mean properties that every instance of this type must have.
     * Usually, this is only a UID.
     */
    Class<?>[] value() default {};
}
