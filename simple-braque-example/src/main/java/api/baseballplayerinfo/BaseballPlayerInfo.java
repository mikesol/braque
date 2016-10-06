package api.baseballplayerinfo;

import braque.$;
import braque.Show;
import model.player.*;
import model.player.baseball.*;

/**
 * Detailed player info, like a profile page.
 * Created by mikesolomon on 20/09/16.
 */
@Show(
        baseType = BaseballPlayer.class,
        argument = Id.class,
        properties = {
                Name.class,
                AtBats.class,
                Average.class,
                Hits.class,
                Favorite.class,
                GamesPlayed.class,
                NumbersWorn.class,
                Position.class,
                Runs.class,
                Team.class, model.team.Name.class, model.team.Mascot.class, $.class
        }
)
public interface BaseballPlayerInfo {
}
