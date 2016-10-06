package braque;

/**
 * <p>An interface shadow.</p>
 * <p>The purpose of an interface shadow is to "shadow" an interface in order to
 * create an implementation. This is used under the hood by the Transformer
 * and its audience is, for the time being, not public.</p>
 * <p>For example, if we have:</p>
 *
 *<pre>
 *{@code MeCreateUser user = Transformer.makeMeCreateUser("foo").addBlurb("Hello World!").addName("Mike").commit();}
 *</pre>
 * <p>The commit returns a useable {@code MeCreateUser}.  This can be especially useful if we explicate the implementation.</p>
 * <pre>
 * For example, with the above code, we'd have to do:
 * {@code if (user instanceof BlurbGet) {
 *       ...
 *   }}
 *</pre>
 * <p>However, if the original call were:</p>
 *
 *<pre>
 * {@code MeCreateUserName_Blurb_Implementation user = Transformer.makeMeCreateUser("foo").addBlurb("Hello World!").addName("Mike").commit();}
 *</pre>
 * then one could do:
 *<pre>
 *   {@code String foo = user.getName();}
 *</pre>
 * <p>modern IDEs will know what the commit() will yield and can often even fill this in for you.</p>
 * <p>Created by mikesolomon on 18/09/16.</p>
 */

public interface $hadow<T extends BraqueObject> {
    T commit();
}
