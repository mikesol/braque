package api.players;

import braque.$;
import braque.Show;
import model.player.*;
import model.player.baseball.Team;

/**
 * For a quick glance at a lot of players
 * Created by mikesolomon on 20/09/16.
 */
@Show(
        argument = Id.class,
        properties = {
                Name.class,
                Position.class,
                NumbersWorn.class,
                Team.class, model.team.Name.class, $.class
        }
)
public interface Players {
}
