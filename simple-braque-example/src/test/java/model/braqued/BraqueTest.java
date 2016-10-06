package model.braqued;

import api.baseballplayerinfo.braqued.BaseballPlayerInfoShowBaseballPlayer;
import api.teaminfo.braqued.TeamInfoShowBaseballTeam;
import api.teaminfo.braqued.TeamInfoShowTeam;
import braque.braqued.Serializer;
import model.fan.braqued.Name;
import model.player.baseball.braqued.AtBats;
import model.player.baseball.braqued.AtBatsGet;
import model.player.baseball.braqued.Hits;
import model.player.baseball.braqued.HitsGet;
import model.player.braqued.*;
import model.team.braqued.CityGet;
import model.team.braqued.PlayersGet;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import braque.braqued.Transformer;
import braque.braqued.Deserializer;
import api.me.braqued.MeUpdateFan;
import api.me.braqued.MeUpdateFanName_Implementation;
import api.me.braqued.MeUpdateFan_Implementation;
import api.me.braqued.MeUpdateFanFavoritePlayers_Implementation;
import api.me.braqued.MeUpdateFavoritePlayersPlayer;

import java.util.*;

/**
 * Created by mikesolomon on 20/09/16.
 */
public class BraqueTest {

    @Test
    public void testTransformer() {
        MeUpdateFan fan = Transformer.makeMeUpdateFan("aFanId").addName("Mike S").commit();
        Assert.assertTrue("fan is instance of NameGet", fan instanceof Name);

        MeUpdateFanName_Implementation anotherFan = Transformer.makeMeUpdateFan("anotherFanId").addName("John D").commit();
        Assert.assertTrue("fan explicit implementation can access and return name", "John D".equals(anotherFan.getName()));

        MeUpdateFan_Implementation anotherFanWithoutName = Transformer.take(anotherFan).removeName().commit();
        Assert.assertTrue("id does not change when removing name", "anotherFanId".equals(anotherFanWithoutName.getId()));

        MeUpdateFanFavoritePlayers_Implementation anotherFanWithFavoritePlayers =
                Transformer.take(anotherFanWithoutName).addFavoritePlayers(Transformer
                        .makeMeUpdateFavoritePlayersBaseballPlayer("aPlayerId").commit()).commit();
        MeUpdateFavoritePlayersPlayer player = new ArrayList<>(anotherFanWithFavoritePlayers.getFavoritePlayers()).get(0);
        Assert.assertTrue("player id is correct", "aPlayerId".equals(player.getId()));

        anotherFanWithFavoritePlayers.addToFavoritePlayers(Transformer
                .makeMeUpdateFavoritePlayersBaseballPlayer("anotherPlayerId").commit());
        Assert.assertTrue("there are two players in the set", anotherFanWithFavoritePlayers.getFavoritePlayers().size() == 2);

        anotherFanWithFavoritePlayers.addToFavoritePlayers(Transformer
                .makeMeUpdateFavoritePlayersBaseballPlayer("anotherPlayerId").commit());
        Assert.assertTrue("using the same ID is a no-op on the set add", anotherFanWithFavoritePlayers.getFavoritePlayers().size() == 2);

        anotherFanWithFavoritePlayers.addToFavoritePlayers(Transformer
                .makeMeUpdateFavoritePlayersBaseballPlayer("yetAnotherPlayerId").commit());
        Assert.assertTrue("a new ID will result in the player being added", anotherFanWithFavoritePlayers.getFavoritePlayers().size() == 3);

        anotherFanWithFavoritePlayers.removeFromFavoritePlayers(Transformer
                .makeMeUpdateFavoritePlayersBaseballPlayer("yetAnotherPlayerId").commit());
        Assert.assertTrue("we should be able to remove from the set", anotherFanWithFavoritePlayers.getFavoritePlayers().size() == 2);
    }

    @Test
    public void testDeserializerAndSerializer0() {
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("baseballplayerinfo/id0/_type", "BaseballPlayer");
        objectMap.put("baseballplayerinfo/id0/id", "id0");
        objectMap.put("baseballplayerinfo/id0/name", "Bob the Baseball Player");
        objectMap.put("baseballplayerinfo/id0/atbats", 100);
        objectMap.put("baseballplayerinfo/id0/numbersworn/0", 5);
        objectMap.put("baseballplayerinfo/id0/numbersworn/1", 6);
        objectMap.put("baseballplayerinfo/id0/numbersworn/2", 5);
        objectMap.put("baseballplayerinfo/id0/team/_type", "BaseballTeam");
        objectMap.put("baseballplayerinfo/id0/team/id", "teamId0");
        objectMap.put("baseballplayerinfo/id0/team/name", "The Trenton Thunder");

        objectMap.put("baseballplayerinfo/id1/_type", "BaseballPlayer");
        objectMap.put("baseballplayerinfo/id1/id", "id1");
        objectMap.put("baseballplayerinfo/id1/name", "Steve McQueen");
        objectMap.put("baseballplayerinfo/id1/hits", 401);

        String bogus = "baseballplayerinfo/id7/hits";
        objectMap.put(bogus, 5.11);

        Set<String> leftover = new HashSet<>();
        List<BaseballPlayerInfoShowBaseballPlayer> players = Deserializer.deserialize(objectMap, BaseballPlayerInfoShowBaseballPlayer.class, leftover);

        Assert.assertTrue("two players are returned", players.size() == 2);
        Assert.assertTrue("player 1 id is id0", players.get(0).getId().equals("id0"));
        Assert.assertTrue("player 1 has 100 atbats", players.get(0) instanceof AtBats && ((AtBatsGet)players.get(0)).getAtBats().equals(100));
        Assert.assertTrue("player 2 has 401 hits", players.get(1) instanceof Hits && ((HitsGet)players.get(1)).getHits().equals(401));
        Assert.assertTrue("one bogus piece of data is left over", leftover.size() == 1 && new ArrayList<>(leftover).get(0).equals(bogus));

        Map<String, Object> serialized = Serializer.serialize(players.get(0));
        Assert.assertTrue("serialized id is correct", serialized.get("baseballplayerinfo/id0/id").equals("id0"));
        Assert.assertTrue("serialized at bats is correct", serialized.get("baseballplayerinfo/id0/atbats").equals(100));
        Assert.assertTrue("serialized numbers worn is correct", serialized.get("baseballplayerinfo/id0/numbersworn/0").equals(5));
        Assert.assertTrue("serialized numbers worn is correct", serialized.get("baseballplayerinfo/id0/numbersworn/1").equals(6));
        Assert.assertTrue("serialized numbers worn is correct", serialized.get("baseballplayerinfo/id0/numbersworn/2").equals(5));
    }

    @Test
    public void testDeserializerAndSerializer1() {
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("teaminfo/id0/_type", "BaseballTeam");
        objectMap.put("teaminfo/id0/id", "id0");
        objectMap.put("teaminfo/id0/name", "Royals");
        objectMap.put("teaminfo/id0/city", "Kansas City");
        objectMap.put("teaminfo/id0/players/player0/_type", "BaseballPlayer");
        objectMap.put("teaminfo/id0/players/player0/id", "player0");
        objectMap.put("teaminfo/id0/players/player0/name", "Bob");
        objectMap.put("teaminfo/id0/players/player1/_type", "BaseballPlayer");
        objectMap.put("teaminfo/id0/players/player1/id", "player1");
        objectMap.put("teaminfo/id0/players/player1/name", "Steve");

        String bogus = "teaminfo/id7/player0";
        objectMap.put(bogus, 5.11);

        List<TeamInfoShowTeam> teams = Deserializer.deserialize(objectMap, TeamInfoShowTeam.class);
        Assert.assertTrue("one team is returned", teams.size() == 1);
        Assert.assertTrue("team 1 id is id0", teams.get(0).getId().equals("id0"));
        Assert.assertTrue("team 1's city is Kansas City", teams.get(0) instanceof CityGet && ((CityGet)teams.get(0)).getCity().equals("Kansas City"));
        Assert.assertTrue("the team has two players", teams.get(0) instanceof PlayersGet && ((PlayersGet)teams.get(0)).getPlayers().size() == 2);


        Map<String, Object> serialized = Serializer.serialize(teams.get(0));
        Assert.assertTrue("serialized id is correct", serialized.get("teaminfo/id0/id").equals("id0"));
        Assert.assertTrue("serialized name is correct", serialized.get("teaminfo/id0/name").equals("Royals"));
        Assert.assertTrue("serialized player 1's name is correct", serialized.get("teaminfo/id0/players/player0/name").equals("Bob"));
        Assert.assertTrue("serialized player 2's name is correct", serialized.get("teaminfo/id0/players/player1/name").equals("Steve"));
    }
}
