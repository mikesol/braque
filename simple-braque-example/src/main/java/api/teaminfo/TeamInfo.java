package api.teaminfo;

import braque.$;
import braque.Show;
import model.team.*;
import model.team.Favorite;
import model.team.Id;
import model.team.Name;

/**
 * Detailed player info, like a profile page.
 * Created by mikesolomon on 20/09/16.
 */
@Show(
        argument = Id.class,
        properties = {
                Name.class,
                Favorite.class,
                City.class,
                Mascot.class,
                Players.class, model.player.Name.class, $.class
        }
)
public interface TeamInfo {
}
