package api.me;

import braque.$;
import braque.Create;
import braque.Show;
import braque.Update;
import model.fan.*;
import model.player.baseball.AtBats;
import model.player.baseball.Team;

/**
 * Detailed player info, like a profile page.
 * Created by mikesolomon on 20/09/16.
 */
@Show(
        argument = Id.class,
        properties = {
                Name.class,
                FavoritePlayers.class,
                model.player.Name.class,
                Team.class,
                model.team.Name.class, $.class, $.class,
                FavoriteTeams.class,
                model.team.Name.class, model.team.Mascot.class, $.class
        }
)
@Update(
        argument = Id.class,
        properties = {
                Name.class,
                Hometown.class,
                FavoritePlayers.class, $.class,
                FavoriteTeams.class, $.class
        }
)
@Create(argument = Id.class)
public interface Me {
}
