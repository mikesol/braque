# Key Concepts in Braque

### Code Generation

Braque is a code generation library that, run out of the box, will put data in
two places:

* In every package where a braque annotation lives, a `braqued` directory will
be created and the generated objects will be put there.  The name of this directory
can be changed with the annotation processor option `braqueGeneratedObjectsPackageName`.
* A root directory in which `Deserializer.java`, `Serializer.java`, `Transformer.java`,
`Fanner.java` and `StringProvisioner.java` live.  This is by default `braque.braqued`
but it is usually a bad idea to use this to avoid namespace conflicts with other
packages using Braque.  You can set this value with
`braqueStaticMethodClassesFullyQualifiedPackageName`.

If you check out `simple-braque-example` in this project or the
[Android sample project][1], you'll see both using braque generated objects, which
usually look something like this:

`import my.package.braqued.MyEndpointShowUser`

Braque code is best organized into a `model` package, which describes what can go into the
data, and an `api` package, which describes how the data is used (again, see the
sample projects).  An API that contains:

```java
@Show(
  argument=Id.class,
  properties = {Name.class}
)
public interface BrowseUsers
```

will result in the creation of an interface called `BrowseUsersShowUser.java` in
the `braqued` directory as well as several implementation files of this interface.
Usually, you do not care about the implementation files (except for the Transformer,
which allows interfaces to be transformed - see below).  It's important to get
used to this naming scheme - in your IDE, the object will be proposed for you,
but you need to know what you're looking for.

Unlike libraries like `retrofit` and `Dagger`, the original interfaces used for Braque
code generation are irrelevant to Braque after processing the annotations.  Of course,
you can use them in your code for whatever you want, but as far as Braque is concerned,
they serve only as instructions to generate new objects.  These new objects make
no reference (don't inherit from, don't store, etc...) the original interfaces.

### The Serializer

```
public interface Serializer {
    <T extends braque.RESTEndpoint> java.util.Map<String, Object> serialize(final T deserialized);
}
```

A file called `Serializer.java` lives in your `braque.braqued` directory
and allows you to serialize all Braque REST endpoints to a flat `Map<String, Object>`.
Braque objects can also be serialized with libraries like `gson` and `jackson`.
The advantage of using the Braque serializer is that its structure as a flat map
helps communicate with REST Apis and data stores like DynamoDB or Firebase.

For example, a Braque REST Endpoint defined as:

```java
Show(
        argument = Id.class
        properties = {
            Name.class,
            LuckyNumbers.class, // a list of integers
            Boss.class, // a reference to a braque object
            user.Name.class, $.class
            Members.class, // a reference to a set of braque objects
            user.Name.class,
            user.Age.class, $.class
        }
)
interface MyEndpoint {

}
```

could be serialized to the following paths:

```
myendpoint/uid0/_type -> User
myendpoint/uid0/id -> uid0 
myendpoint/uid0/name -> Jane
myendpoint/uid0/luckynumbers/0 10
myendpoint/uid0/luckynumbers/1 15
myendpoint/uid0/luckynumbers/2 15
myendpoint/uid0/boss/id uid1
myendpoint/uid0/boss/name Helen
myendpoint/uid0/members/uid2/id uid2
myendpoint/uid0/members/uid2/name Steve
myendpoint/uid0/members/uid2/age 43
myendpoint/uid0/members/uid3/id uid3
myendpoint/uid0/members/uid3/name Bob
myendpoint/uid0/members/uid3/age 44
```

The `$.class` acts as a delimiter for nested interfaces so that we know when they
end.  For example, the first `$.class` above signals that the first `user.Name.class`
belongs to the `Boss.class` but afterwards `Members.class` belongs to the
top-level object.  The Braque compiler will generally complain politely
about misplaced `$.class`.


### The Deserializer

```
public interface Deserializer {
    <T extends braque.RESTEndpoint> java.util.List<T> deserialize(java.util.Map<String, Object> serialized, Class<T> type);
    <T extends braque.RESTEndpoint> java.util.List<T> deserialize(java.util.Map<String, Object> serialized, Class<T> type, java.util.Collection<String> remainingPaths);
}
```

The Braque deserializer has more going on under the hoods than the serializer:
it will try hard to marshall your data into Braque objects.  If you take the example
above for `MyEndpoint`, feeding that data to the deserializer will produce the
original Braque object.  What's important to take away about the Deserializer is
that, unlike `gson` and `jackson`, the Braque deserializer is created at compile
time and knows how inheritence in Braque works.  If the original data is
missing `_type` information, it analyzes the data and creates the object that
represents the data best.  If no object is created, it will add the path to
`remainingPaths`. The only thing that the deserializer won't guess is the
top-level RESTEndpoint to use, which is supplied in the `type` parameter.
This may change to be more flexible in future versions of Braque, but for
now it is a guarantee that all of the object that come out of the deserializer
will implement this interface.

### The Transformer

The transformer follows the object-as-builder pattern, allowing you to compose
objects on the fly as data comes in and commit the objects.  It also allows you
to take committed objects, put them back in the transformer, and keep working on them.

Some pseudocode:

```java
Transformer.makeBrowseUpdateUser("uid").addName("Pekka").addAge(52).commit();
...
Transformer.take(previousResult).removeAge().addHometown("Helsinki").commit();
```

The Transformer is still in the works and its API can/should be extended to accept
more values in its `take` method.

### The Fanner

```java
public interface Fanner {
    java.util.Map<Class<? extends braque.Prop>, java.util.Set<String> > propertyToPaths();
    java.util.Map<String, Class<? extends braque.Prop>> pathToProperty();
    java.util.Map<String, java.util.Set<braque.Operation> > pathToOperations();
    java.util.Map<braque.Operation, java.util.Set<String> > operationToPaths();
    java.util.Map<Class<? extends braque.Prop>, braque.PropertyManyness> propertyToPropertyManyness();
    java.util.Map<Class<? extends braque.Prop>, String> propertyToPropertyName();
    java.util.Map<String, Class<? extends braque.Prop>> propertyNameToProperty();
    java.util.Map<Class<? extends braque.BraqueObject>, String> typeToTypeName();
    java.util.Map<String, Class<? extends braque.BraqueObject>> typeNameToType();
    java.util.Map<Class<?>, java.util.Set<String>> typeToPaths();
    java.util.Map<String, java.util.Set<Class<?>>> pathToTypes();
    java.util.Set<Class<? extends braque.Prop>> uidProperties();
    java.util.Map<Class<? extends braque.BraqueObject>, java.util.List<Class<? extends braque.BraqueObject>>> typeToSubTypes();
    java.util.Map<Class<? extends braque.BraqueObject>, Class<? extends braque.BraqueObject>>  typeToSuperTypes();
}

```

The Fanner helps to fan-out data to different endpoints and is analagous to the
Dagger Object Graph in that it represents how everything in Braque is linked together.
For example, `typeToPaths()` links types, like `String` or `Integer`, to Braque
endpoints at which these types reside, like `endpoint/*/age`.  Paths in the
Fanner and StringProvisioner use wildcard notation for UIDs and list indices,
making them easy to plug directly into Android's `URIBuilder`.

### The StringProvisioner

The String Provisioner contains all information for Braque paths, property names
and type names.  By consolidating everything here, it allows for subsequent
compilation steps to encrypt the strings if need be.

### Gotchyas and Issues

There are currently a few gotchyas in Braque:

* If you have an error elsewhere in your code and you have cleaned your
build directory, the compiler can throw lots of errors about missing Braque
objects in addition to the error blocking the compilation.  This depends
on the compiler's order of execution of what it compiles when.  In the future, we'd
like to improve the signal to noise ratio so that, if compilation crashes before
Braque object generation, errors will not be issued relating to missing Braque objects.

* `Deserializer.java` can get really large for large objects.  This is not a
propblem in production because proguard reduces it nicely, but can hit Java's
64 KB limit for method generation in extreme cases.  You can just increase the limit
if this happens and proguard can still squeeze it down.  There are other ways
to write the `Deserializer` that would make it smaller but the current one
is blazingly fast.

#### Pull requests are welcome!

[1]: http://www.github.com/mikesol/favorite-things