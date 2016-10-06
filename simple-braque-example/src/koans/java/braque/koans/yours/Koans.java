package braque.koans.yours;

import api.baseballplayerinfo.braqued.BaseballPlayerInfoShowBaseballPlayer;
import api.baseballplayerinfo.braqued.BaseballPlayerInfoShowPlayerName_NumbersWorn_Implementation;
import api.me.braqued.MeUpdateFan;
import api.me.braqued.MeUpdateFanHometown_Name_Implementation;
import api.players.braqued.PlayersShowPlayer;
import api.players.braqued.PlayersShowPlayerNumbersWorn_Position_Implementation;
import braque.braqued.Deserializer;
import braque.braqued.Fanner;
import braque.braqued.Serializer;
import braque.braqued.Transformer;
import model.fan.braqued.Hometown;
import model.fan.braqued.HometownGet;
import model.fan.braqued.Name;
import model.player.baseball.braqued.Average;
import model.player.baseball.braqued.BaseballPlayer;
import model.player.baseball.braqued.Runs;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mikesolomon on 20/09/16.
 */
public class Koans {

    public static boolean areThereAnyDeprecatedFields(Class<?> klass) {
        Field[] fields = klass.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field fi = fields[i];
            if (fi.isAnnotationPresent(Deprecated.class)) {
                return true;
            }

        }
        return false;
    }

    @Test
    public void koans() {
        Assert.assertTrue("Truth will set you free. Or, in less philosophical terms, " +
                "go to simple-example/src/koans/java/braque/koans/yours/Koans.java and modify "+
                "the tests so that they pass. For hints, check out simple-example/src/koans/java/braque/koans/mine/Koans.java."+
                "In doing so, you will learn how braque works.", false);

        Map<String, Object> test1Map = new HashMap<>();
        test1Map.put("baseballplayerinfo/myId/_type", "bogusType");
        test1Map.put("baseballplayerinfo/myId/id", "myId");
        test1Map.put("baseballplayerinfo/myId/gamesplayed", 100);
        List<BaseballPlayerInfoShowBaseballPlayer> test1Players = Deserializer.deserialize(test1Map, BaseballPlayerInfoShowBaseballPlayer.class);

        Assert.assertTrue("The deserializer deserializes serialized data, represented as a collection of values at "+
                "api endpoints, into strongly typed objects.  If you give it a valid type for the _type endpoint, "+
                "it will attempt deserializing to that type before other ones.  Otherwise, it will work hard to find a "+
                "valid type to deserialize to. Above, even though the type should be BaseballPlayer, we feed it \"bogusType\"." +
                "A timid person has asserted that the deserializer will fail...let's fix that!", !(test1Players.get(0) instanceof BaseballPlayer));

        Map<String, Object> test2Map = new HashMap<>();
        test2Map.put("players/foo/_type", "FootballPlayer");
        test2Map.put("players/foo/id", "foo");
        test2Map.put("players/foo/position", "Wide Receiver");
        test2Map.put("players/bar/_type", "FootballPlayer");
        test2Map.put("players/bar/position", "Runningback");
        List<PlayersShowPlayer> test2Players = Deserializer.deserialize(test2Map, PlayersShowPlayer.class);


        Assert.assertTrue("The @Type annotation takes an optional first element as a supertype followed by a list of " +
                "necessary properties.  All necessary properties must be used when deserializing an object. "+
                "In simple-example/src/main/java/model/player/football/FootballPlayer.java, FootballPlayer " +
                "is a type which inherits from Player which has one necessary property, the Id. Look at how the player "+
                "with id=foo is created and see if you can do the same thing to create the player with id=bar.", test2Players.size() == 2);

        Map<String, Object> serialized = Serializer.serialize(test1Players.get(0));
        Assert.assertTrue("The Serializer works in the opposite direction. Here, we serialize the first deserialized "+
                "player.  Their gamesplayed should be the same as what we put in.",
                serialized.get("baseballplayerinfo/myId/gamesplayed").equals(403));

        Map<String, Object> test3Map = new HashMap<>();
        test3Map.put("players/helsinki/_type", "Player");
        test3Map.put("players/helsinki/id", "helsinki");
        test3Map.put("players/helsinki/position", "Pitcher");
        List<PlayersShowPlayer> test3Players = Deserializer.deserialize(test3Map, PlayersShowPlayer.class);

        Assert.assertTrue("Class hierarchies are enforced by braque, and all classes that are not at the bottom "+
                "of a hierarchy are abstract.  Make the Player above one of its two subclasses.",
                test3Players.size() == 1);


        Map<String, Object> test4Map = new HashMap<>();
        test4Map.put("baseballplayerinfo/myId/_type", "BaseballPlayer");
        test4Map.put("baseballplayerinfo/myId/id", "myId");
        test4Map.put("baseballplayerinfo/myId/gamesplayed", 100);
        test4Map.put("baseballplayerinfo/myId/hits", 200);
        test4Map.put("baseballplayerinfo/myId/numbersworn/0", 55);
        test4Map.put("baseballplayerinfo/myId/numbersworn/1", 3);
        test4Map.put("baseballplayerinfo/myId/numbersworn/2", 101);
        test4Map.put("baseballplayerinfo/myId/runs", 3.1416);

        List<BaseballPlayerInfoShowBaseballPlayer> test4Players = Deserializer.deserialize(test4Map, BaseballPlayerInfoShowBaseballPlayer.class);

        Assert.assertTrue("Type correctness is enforced in the deserializer. "+
                "Check the type of simple-example/src/main/java/model/player/baseball/Runs.java.", test4Players.get(0) instanceof Runs);

        Map<String, Object> test5Map = new HashMap<>();
        test5Map.put("baseballplayerinfo/myId/_type", "BaseballPlayer");
        test5Map.put("baseballplayerinfo/myId/id", "myId");
        test5Map.put("baseballplayerinfo/myId/gamesplayed", 100);
        test5Map.put("baseballplayerinfo/myId/hits", 200);

        List<BaseballPlayerInfoShowBaseballPlayer> test5Players = Deserializer.deserialize(test5Map, BaseballPlayerInfoShowBaseballPlayer.class);

        Assert.assertTrue("Braque is all about strong types.  Objects are instances of all their properties.  Because this player "+
                "has no average, it is not an instance of Average.  Fix that by giving the player an average (\"baseballplayerinfo/myId/average\"). "+
                "Make sure to check the correct type in simple-example/src/main/java/model/player/baseball/Average.java", test5Players.get(0) instanceof Average);

        MeUpdateFan aFan = Transformer.makeMeUpdateFan("aFanId").addName("Bob").commit();

        Assert.assertTrue("The transformer works in a similar way. Adding and removing properties add and remove "+
                "type inheritance.  Currently, the object inherits from Name but not Hometown. "+
                "Add something between the addName() and commit() call to add a hometown.  I bet you can guess the name of the method...", aFan instanceof Hometown);

        MeUpdateFan anotherFan = Transformer.makeMeUpdateFan("aFanId").addHometown("New York").addName("Joe Smith").commit();

        Assert.assertTrue("If you want to access properties of objects created by the transformer,"+
                "you can cast them to the appropriate Get interface. Below, we want the name but we've accidentally gotten the hometown. "+
                "Let's fix that by casting to the right Get interface and calling the right get method for a Hometown. "+
                "Don't cheat by changing the name to \"New York\"!", ((HometownGet)anotherFan).getHometown().equals("Joe Smith"));

        MeUpdateFanHometown_Name_Implementation yetAnotherFan = Transformer.makeMeUpdateFan("aFanId").addHometown("New York").addName("Joe Smith").commit();

        Assert.assertTrue("We can avoid casts by using the type returned by commit.  This is easily found by most modern IDEs. "+
                "The naming convention is all of the asked-for properties in alphabetical order separated by an underscore. "+
                "Change getHometown to a function that will yield the name.", (yetAnotherFan.getHometown().equals("Joe Smith")));

        MeUpdateFan aFanWithoutANameOrAHometown = Transformer.take(yetAnotherFan).removeName().commit();

        Assert.assertTrue("We can take an object via take() and remove properties.  Here, we have removed its name. "+
                "Remove the hometown for the test to compile.",
                (!(aFanWithoutANameOrAHometown instanceof Name || aFanWithoutANameOrAHometown instanceof Hometown)));

        Assert.assertTrue("The Fanner helps us know where our data appears in the API. This helps us keep data consistent if "+
                "we are manually managing views or denormalizing data."+
                        "Look to see how many times a player's Name is represented in the API. "+
                "Of course, you could just cheat and read Fanner.java, but it's more fun if you look for Name in the API.",
                Fanner.mPropertiesToPaths.get(model.player.braqued.Name.class).size() == 3);

        Assert.assertTrue("@Clojure allows us to inject code into our objects using Clojure code. "+
                "Find the use of @Clojure in the code base (hint - it's on a property in model.player.baseball) "+
                "and see how it is being used.  Change the .class object to any class for which areThereAnyDeprecatedFields "+
                "will evaluate to true.",
                areThereAnyDeprecatedFields(BaseballPlayerInfoShowPlayerName_NumbersWorn_Implementation.class));

    }
}
