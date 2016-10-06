package braque;

/**
 * <p>An enum to represent the three levels of manyness for properties.</p>
 * Created by mikesolomon on 14/09/16.
 */
public enum PropertyManyness {
    /**
     * Only one.
     */
    SIMPLE,
    /**
     * A set of properties.
     */
    SET,
    /**
     * A list of properties.
     */
    LIST
}
