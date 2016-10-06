package braque;

/**
 * <p>This holds the type of a property set, i.e. a Boolean, Integer, or another BraqueObject.</p>
 *
 * <pre>
 *     {@code @PropertySet(String)}
 *      public interface MyLuckyNumbers {
 *      }
 * </pre>
 *
 * <p>Created by mikesolomon on 10/09/16.</p>
 */


public @interface PropertySet {
    /**
     * @return The type of a set element. Can be one of the following:
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
