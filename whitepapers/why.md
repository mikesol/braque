# Why Braque?

Braque stems from a development need at [Jongla][1], where we work with systems that keep millions
of users' accounts in sync. In doing so, we've seen our fair share of bugs, but some tend
to pop up more than others:

- Data is messy and, in certain countries, sending it around is expensive. Apps are more sluggish
and monolithic when data is not aggregated and denormalized into views.
- It is really easy to incrementally grow objects with tens of fields to represent complex objects,
like a group or a user.  It is really hard to remember to deal with the tens of `null` fields
that this object will invariably have when the server only return's the user's birthday.
- A simple change in data (for example, changing a person's gender) can lead to all sorts of model
 breakage based on hard-coded assumptions. Objects should react to data, not the other way around.

Braque helps us solve these problems by making objects less ambiguous and more flexible.  When
we serialize and deserialize data from the server or from a local store, we get back objects
that are strongly typed.  Introspection gives us information about the object.  So instead of code like:

```java
if (foo.name != null) { /**/ }
```

we have code like:

```java
if (foo implements NameGet) { /**/ }
```

The difference might seem tiny, but it is actually really important.  Code like this
helps us make less mistakes.  We cannot set the `name` of `foo` to `null` because `foo`
literally has no `name` field.

The core tenant of Braque is that there is a one-to-many relationship between interfaces,
which represent objects, and implementations, which represent what objects look like
in the real world.  This type of thinking prevents a few things from happening...

### No God objects
Objects in Braque rarely have more than a few fields because we don't need all their information
at the same time.  For example, an object representing a user being visualized on a list will need less fields
than an object representing a full screen user profile, which will still need only a subset
of fields that have to do with the user's full information.  Objects are thus tinier and
easier to read/grock.  If you have ever inherited legacy code that has 50 fields for some object
with a comment next to one of them saying "what does this do?", you know exactly why this is important.
### Goodbye `null`
Objects in Braque are guaranteed to never contain `null` values.  If you try to insert one,
it will throw a NPE like when you call Kotlin objects from Java. This means that we cannot
accidentally set things as `null`, but also, we don't have to worry about making all fields of an
object required just to avoid `null`.

```java
User user = Transformer.makeUser("id0").addName("John Smith").addAge(43).removeName().commit();
boolean willBeTrue = user instanceof AgeGet;
boolean willBeFalse = user instanceof NameGet;
```

This object does not have a `null` value for name because it does not have a `name` field, which reduces
ambiguity in understanding what objects represent.  However, by using a builder pattern, adding and
removing properties to and from objects is as easy, if not easier, than working with getters and setters
(which are [evil][14]).

This flexibility helps in design patterns where one needs a bridge between NoSQL,
where anything can contain anything, and Java, where intelligent strong typing can turn runtime
errors into compile-time errors.

### Tiny
`braque-core` is an interface-and-annotation-only library.  Like
[Dagger][12], it can be used for a subset of objects in your codebase.  This
was an essential requirement at [Jongla][1], where we are constantly phasing things
in and out of evolving codebases and where we keep our apps and services very light.

# Why *not* Braque?

There is an entire ecosystem of Java libraries that deal with model generation.  Many of these are
coupled with some type of persistence mechanism (like [Realm][2], [DBFlow][3], [Iron][4],
[SimpleNoSQL][5])
and some facilitate API building ([Swagger][6]).

These are all great tools, and if you are using any of them, Braque is probably not
for you. Braque is best used with libraries like [Couchbase][7], [Firebase][8], [MapDB][9],
[SQLite][10], [retrofit][11], [DynamoDB][13] and anything else that does not generate models.

[1]: http://www.jongla.com
[2]: http://www.realm.io
[3]: https://github.com/Raizlabs/DBFlow
[4]: https://github.com/FabianTerhorst/Iron
[5]: https://github.com/Jearil/SimpleNoSQL
[6]: http://www.swagger.io
[7]: http://www.couchbase.com
[8]: http://firebase.google.com
[9]: http://www.mapdb.org
[10]: http://www.sqlite.org
[11]: http://square.github.io/retrofit/
[12]: http://google.github.io/dagger/
[13]: https://aws.amazon.com/dynamodb
[14]: http://www.javaworld.com/article/2073723/core-java/why-getter-and-setter-methods-are-evil.html