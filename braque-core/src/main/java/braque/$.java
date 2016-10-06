package braque;

/**
 * <p>Used as a separator in lists of objects for @Show, @Update, @Create and @Destroy.</p>
 * <p>For example if we have:</p>
 * <pre>
 * {@code @Create(
 *   argument = Id.class,
 *   properties = {
 *     Name.class,
 *     Avatar.class, Digest.class, $.class,
 *     Creator.class, $.class,
 *     Admins.class, $.class,
 *     Members.class,
 *     com.foo.user.Name.class,
 *     LastSeen.class,
 *     com.foo.user.Avatar.class, Digest.class, $.class, $.class})}
 * </pre>
 * <p>then the {@code $.class} is used to denote the end of an object.  For example, first {@code $.class}</p>
 * above denotes that the Avatar object is finished and we are moving onto the Creator object.
 * Objects can be nested and finish at the same point, requiring multiple {@code $.class}.
 * For example, the Avatar class nested in Members needs to be terminated by two {@code $.class}
 * because it is nested two levels deep ({@code Avatar.class} inside an {@code Member.class}).
 * <p>Created by mikesolomon on 11/09/16.</p>
 */

public interface $ {
}
