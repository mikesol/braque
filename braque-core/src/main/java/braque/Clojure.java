package braque;

/**
 * <p>Used to inject code via <a href="http://clojure.org">Clojure</a>.</p>
 *
 * <p>The string is evaluated as the body of a clojure function that should return a
 * list of strings.  These will be treated as lines of code and will be injected
 * in model class implementations at various places depending on the {@code place} parameter.</p>
 *
 * <p>The easiest way to see what this does is just by using one to print values and
 * return an itemless array which has no incidence on the code.</p>
 *
 * <pre>
 *
 *     {@code @Clojure}("(println place name klass klasshierarchy iface type many path previous objdef) '()")
 *     {@code @Property}(String)
 *      public interface CityInWhichIWasBorn {
 *      }
 * </pre>
 *
 * <p>The entire clojure core library can be used and variable names can be anything save the 10 arguments that
 * are automatically passed to the function.  These arguments work as follows:</p>
 *
 * <ol>
 *     <li>{@code place}, the place in the model implementation at which we are. Possible values are:
 *     <ul>
 *         <li>{@code 'classdef}. the code will be injected above the definition of a model class, great for injecting annotations.</li>
 *         <li>{@code 'fielddef}. the code will be injected above the declaration of a field.</li>
 *         <li>{@code 'ctor}. the code will be injected above the constructor of a model class.</li>
 *         <li>{@code 'get}. the code will be injected above the getter of a given property.</li>
 *         <li>{@code 'set}. the code will be injected above the setter of a given property.</li>
 *         <li>{@code 'add}. the code will be injected above the {@code addTo} method of a property set or list.</li>
 *         <li>{@code 'remove}. the code will be injected above the {@code removeFrom} method of a property set or list.</li>
 *     </ul>
 *     </li>
 *     <li>{@code name}: the name of the entity being injected over - could be a model class or a property</li>
 *     <li>{@code klass}: the class into which code is injected (will be the same as {@code name} if the place is {@code 'classdef} or {@code 'ctor})</li>
 *     <li>{@code klasshierarchy}: a list that shows this class as the first element and any supertypes in inheritance order.</li>
 *     <li>{@code iface}: the interface being implemented. The naming convention is that {@code UsersShowUser_Implementation} implements {@code UserShowUser}.</li>
 *     <li>{@code type}: the type of a property (String, Boolean, another BraqueObject, etc.).  Meaningless for class declarations and constructors.</li>
 *     <li>{@code many}: either {@code 'simple}, {@code 'list} or {@code 'set} depending on the property.  Meaningless for class declarations and constructors.</li>
 *     <li>{@code path}: the api path at which this is found.</li>
 *     <li>{@code previous}: previous code being injected here by other {@code @Clojure} annotations.</li>
 *     <li>{@code objdef}: the complete definition of the current model object, with all of its properties and their types.</li>
 * </ol>
 *
 * <p>Created by mikesolomon on 10/09/16.</p>
 */

public @interface Clojure {
    String value();
}
