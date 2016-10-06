package braque;

/**
 * <p>This holds the type of a property list, i.e. a Boolean, Integer, or another BraqueObject.</p>
 *
 * <pre>
 *     {@code @PropertyList(String)}
 *      public interface OrderedList {
 *      }
 * </pre>
 *
 * <p>Created by mikesolomon on 10/09/16.</p>
 */


public @interface PropertyList {
    /**
     * @return The type of a list element. Can be one of the following:
     * <ul>
     *     <li>{@code Boolean}</li>
     *     <li>{@code String}</li>
     *     <li>{@code Integer}</li>
     *     <li>{@code Double}</li>
     *     <li>{@code Float}</li>
     *     <li>{@code Long}</li>
     *     <li>an arbitrary {@code BraqueObject}</li>
     * </ul>
     */
    Class<?> value();
}
