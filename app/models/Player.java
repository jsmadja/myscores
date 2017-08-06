package models;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.annotation.Where;
import controllers.PlayersController;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import java.util.*;

import static com.avaje.ebean.Ebean.find;
import static com.avaje.ebean.Expr.*;

@Entity
public class Player extends BaseModel<Player> implements Comparable<Player> {

    public static Player guest = new Player(0L, "guest");

    public static Model.Finder<Long, Player> finder = new Model.Finder(Long.class, Player.class);

    public String name;

    public Long shmupUserId;

    @OneToMany(mappedBy = "player", fetch = FetchType.EAGER)
    @Where(clause = "rank > 0")
    public List<Score> scores = new ArrayList<Score>();

    @OneToMany(mappedBy = "player")
    public List<Score> allScores = new ArrayList<Score>();

    private PlayersController.Counts counts;

    public Player(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Player(String name) {
        this.name = name;
    }

    public static Player findOrCreatePlayer(String name) {
        Player player = Player.finder.where()
                .eq("name", name)
                .findUnique();
        if (player == null) {
            player = new Player(name);
            player.save();
        }
        return player;
    }

    public static Player findByShmupUserId(Long shmupUserId) {
        return Player.finder.where()
                .eq("shmupUserId", shmupUserId)
                .findUnique();
    }

    public static List<Player> findAll() {
        return finder.orderBy("name").findList();
    }

    @Override
    public String toString() {
        return name;
    }

    public boolean isAuthenticated() {
        return !this.equals(guest);
    }

    public Collection<Score> bestReplayableScores() {
        return Score.finder.fetch("game").fetch("platform").fetch("mode").fetch("difficulty").where().not(eq("replay", "")).eq("player", this).findList();
    }

    public boolean hasReplays() {
        return !bestReplayableScores().isEmpty();
    }

    public boolean canImportScores() {
        return id == 1;
    }

    public boolean isAdministrator() {
        return id == 1;
    }

    public int computeOneCredit() {
        return oneccs().size();
    }

    public Collection<Score> oneccs() {
        List<Score> oneCreditScores = find(Score.class).fetch("platform").fetch("game").fetch("mode").fetch("difficulty").where().eq("player", this).eq("onecc", true).findList();
        List<Score> oneccs = new ArrayList<Score>();
        Set<String> uniqueOneCreditScores = new HashSet<String>();
        for (Score oneCreditScore : oneCreditScores) {
            String gameTitle = oneCreditScore.getGameTitle();
            String mode = oneCreditScore.modeName();
            String difficulty = oneCreditScore.difficultyName();
            String key = gameTitle + mode + difficulty;
            boolean add = uniqueOneCreditScores.add(key);
            if (add) {
                oneccs.add(oneCreditScore);
            }
        }
        return oneccs;
    }

    public PlayersController.Counts getCounts() {
        return counts;
    }

    public void setCounts(PlayersController.Counts counts) {
        this.counts = counts;
    }

    public PlayersController.Counts computeCounts() {
        Integer firstRankCount = find(Score.class).where(and(eq("player", this), eq("rank", 1))).findRowCount();
        int secondRankCount = find(Score.class).where(and(eq("player", this), eq("rank", 2))).findRowCount();
        int thirdRankCount = find(Score.class).where(and(eq("player", this), eq("rank", 3))).findRowCount();
        int oneCreditCount = computeOneCredit();
        return counts = new PlayersController.Counts(firstRankCount, secondRankCount, thirdRankCount, oneCreditCount);
    }

    @Override
    public int compareTo(Player p) {
        return this.name.compareToIgnoreCase(p.name);
    }

    public List<Score> fetchScores() {
        return Score.finder.
                where(and(gt("rank", 0), eq("player", this))).
                fetch("platform").
                fetch("stage").
                fetch("game").
                fetch("mode").
                fetch("player").
                fetch("difficulty").
                findList();
    }

    public Score getBestScoreFor(Game game, Mode mode, Difficulty difficulty) {
        return Ebean.createQuery(Score.class).
                setMaxRows(1).
                orderBy(game.hasTimerScores() ? "value" : "value desc").
                where().eq("player", this).eq("game", game).eq("mode", mode).eq("difficulty", difficulty).
                findUnique();
    }
}
