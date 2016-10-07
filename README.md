[![Build Status](https://travis-ci.org/Jongla/braque.svg?branch=master)](https://travis-ci.org/Jongla/braque)
# Braque

Braque is a compile-time model creation framework for Java.

#### tl;dr here's an [Android sample project][4]

![Logo](img/logo.png)

Braque aims to solve the following problems:

1. It lessens the need for the
"What does a property with a value of `null` mean?" conversation
that is (not) had between developers, leading to various
forms of data inconsistency and `NullPointerExceptions`.
2. As server/client communication schema evolve rapidly (think NoSQL),
we need extensible object models that are strong-typed.
3. For clients that [denormalize their data][1], we need a set of tools to keep
data consistent across these denormalizations without enforcing any particular scheme.
4. We need to predictably serialize and deserialize dynamically typed
data into objects whose contents can be queried via inspection instead
of via "Does `name=null` mean that the person's name is null?" (see 1
above).
5. We need to be able to automatically update groups of objects with new annotations,
methods, etc..

## Where can I get it?

In your top-level `build.gradle`:
```groovy

buildscript {
    repositories {
        jcenter()
        mavenCentral()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:2.2.0' // for android
        classpath 'com.neenbedankt.gradle.plugins:android-apt:1.8' // for android
        classpath "net.ltgt.gradle:gradle-apt-plugin:0.8" // for java
    }
}
allprojects {
    repositories {
        jcenter()
        maven {
            url "https://jongla-bin.bintray.com/java-utils"
        }
    }
}
```

And then in the `build.gradle` for the projects where braque is used:
```groovy
dependencies {
    compile 'com.jongla:braque-core:0.0.1'
    apt 'com.jongla:braque-compiler:0.0.1'
}
```

## How does it work?

The easiest way to learn about Braque is to run `./gradlew koans`, which
will walk you through the koans associated with `simple-braque-example`.
Make the tests in `braque.koans.yours` pass.  For help, check out `braque.koans.mine`.
Because Braque is based on code generation, running this in an IDE may be a headache at
first depending on your IDE.  To start off, use the command line for compiling
and your favorite text editor for editing.

You can also checkout the whitepapers for more information
on the motivations for creating this library:

* [Why Braque?](/whitepapers/why.md)
* [Key Concepts in Braque](/whitepapers/concepts.md)

## A small example

First, let's create a `User` object with several potential properties.

`mypackage.model.User.java`
```java
@Type(Id.class)
public interface Id {
}
```

`mypackage.model.Id.java`
```java
@UID
@Property(String.class)
public interface Id {
}
```

`mypackage.model.Name.java`
```java
@Property(String.class)
public interface Name {
}
```

`mypackage.model.Age.java`
```java
@Property(Integer.class)
public interface Age {
}
```

`mypackage.model.Friend.java`
```java
@Property(User.class)
public interface Friend {
}
```

Then, let's create an endpoint.  Endpoints are written like REST endpoints -
they support `Show`, `Create`, `Update` and `Destroy`.

`mypackage.api.UserEndpoint.java`
```java
@Create(
        argument = Id.class,
        properties = {
                Name.class,
                Age.class,
                Friend.class, Name.class, $.class
        }
)
@Show(
        argument = Id.class,
        properties = {
                Name.class,
                Friend.class, Name.class, $.class
        }
)
public interface UserEndpoint {
}
```

`$.class` is used as a separator to
signal that the nested `Friend` object ends.
What you get out of this is when the
annotations are processed is:

```java
UserCreateUser user = Transformer.makeUserEndpointCreate("aUserId").addName("me").commit();
boolean willBeTrue = user instanceof Name;
boolean willBeFalse = user instanceof Age;
UserCreateUserAge_Implementation stronglyTypedUser =
    Transformer.take(user).removeName().addAge(43).commit();
int age = stronglyTypedUser.getAge(); // will be 43
// String name = stronglyTypedUser.getName(); // won't compile because we have removed the name

Map<String, Object> userMap = new HashMap<>();
userMap.put("userendpoint/myId/_type","User");
userMap.put("userendpoint/myId/id", "myId");
userMap.put("userendpoint/myId/name", "John");
userMap.put("userendpoint/myId/friend/_type","User");
userMap.put("userendpoint/myId/friend/id", "anotherId");
userMap.put("userendpoint/myId/friend/name", "Jane");

List<UserEndpointCreateUser> deserialized =
    Deserializer.deserialize(userMap, UserEndpointCreateUser.class);
boolean willAlsoBeTrue = deserialized.get(0) instanceof Name;
boolean willAlsoBeFalse deserialized.get(0) instanceof Age;

Map<String, Object> backAgain = Serializer.serialize(deserialized.get(0));
boolean trueness = backAgain.get("userendpoint/myId/name").equals("John");
```

Whet your appetite?  Check out `simple-braque-example` to see other neat
things like inheritance of types, the `Fanner` class to help with
denormalization and [clojure][2] integration for code injection via the
`@Clojure` annotation.  All of this is covered in the koans.

## A note about IDEs

AndroidStudio is great at handling generated code via the `android-apt` plugin.
IntelliJ is less friendly.  There are a few apt options of which
`simple-braque-example` uses one.  But even this takes Project Structure
tweaking in IntelliJ to get it to recognize the correct source folders.

## A note on access

Fields are by default `protected` so that getters and setters make sense.
However, if you want to use packages like [Dagger][3] (which you can by generating new annotations
via the `@Clojure` annotation - run the koans to learn more), then fields need to be public. This can be tweaked
by setting `makeObjectFieldsPublic` as `true`. Check out `build.gradle` of `simple-braque-example`
to see how this is done in apt (for now it is set to false).

## Why Braque?

Georges Braque (1882-1963), a French painter, was a master at convincingly
representing the same object from various different angles within one work.

## Miscellania

The first version of Braque was written by [Mike Solomon][5] at [Jongla][6].
The Braque Logo was created by [Tomi Tuomela][7]. Braque is Open Source under
the Apache License Version 2.0. Pull requests are welcome!

[1]: https://en.wikipedia.org/wiki/Denormalization
[2]: http://clojure.org
[3]: http://google.github.io/dagger/
[4]: https://github.com/mikesol/favorite-things
[5]: mailto:mike@jongla.com
[6]: http://www.jongla.com
[7]: mailto:tomi@jongla.com
