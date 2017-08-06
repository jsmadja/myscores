package controllers;

import actions.User;
import models.Player;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.IOException;

public class PlayerController extends Controller {

    public static Result index(Player player) {
        if (player == null) {
            return notFound();
        }
        return ok(views.html.player_read.render(player, player.fetchScores()));
    }

    public static Result hideMedals(Boolean hide) {
        Player player = User.current();
        player.update();
        return index(player);
    }

    public static Result indexShmup(Long shmupId) throws IOException {
        Player player = Player.findByShmupUserId(shmupId);
        return index(player);
    }

}
