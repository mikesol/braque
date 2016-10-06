package braque;

/**
 * <p>Represents a {@code DESTROY} api endpoint call.</p>
 * <p>Create a resource on the remote system, not unlike {@code DELETE}.</p>
 * <p>It's form is {@code /foo/{argument}/other/stuff/{maybeAnotherUid}/more/things}.</p>
 * <p>See the koans and the {@code simple-braque-example} to learn more about how this is used.</p>
 * <p>Created by mikesolomon on 10/09/16.</p>
 */

public @interface Destroy {
    /**
     * @return If we need to specify a baseType, we use this.  For example, if we have two types
     * that have the exact same properties, we use this to discern which one we want.
     */
    Class<?> baseType() default Void.class;
    /**
     * @return In {@code /foo/{argument}/other/stuff/{maybeAnotherUid}/more/things}, this is whatever
     * is going into {@code {argument}}. Usually a uid but sometimes not.
     */
    Class<?> argument();

    /**
     *
     * @return A list of properties.
     */
    Class<?>[] properties() default {};
}
