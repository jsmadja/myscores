package models;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.annotation.Where;
import com.google.common.base.Predicate;
import org.apache.commons.collections.map.MultiKeyMap;
import play.db.ebean.Model;

import javax.annotation.Nullable;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import java.util.*;

import static com.avaje.ebean.Expr.eq;
import static com.google.common.collect.Collections2.filter;
import static javax.persistence.CascadeType.PERSIST;

@Entity
public class Game extends BaseModel<Game> implements Comparable<Game> {

    public static Finder<Long, Game> finder = new Model.Finder(Long.class, Game.class);

    public String thread;

    public String cover;

    public String title;

    @OneToMany(mappedBy = "game")
    @Where(clause = "rank > 0")
    public List<Score> scores;

    @OneToMany(mappedBy = "game")
    public List<Score> allScores;

    @OneToMany(mappedBy = "game")
    @Where(clause = "onecc = true")
    public List<Score> oneccs;

    @OrderBy("name")
    @OneToMany(mappedBy = "game", cascade = PERSIST)
    public List<Platform> platforms;

    @OrderBy("sortOrder")
    @OneToMany(mappedBy = "game", cascade = PERSIST)
    public List<Difficulty> difficulties;

    @OrderBy("sortOrder")
    @OneToMany(mappedBy = "game", cascade = PERSIST)
    public List<Mode> modes;

    @OrderBy("sortOrder")
    @OneToMany(mappedBy = "game", cascade = PERSIST)
    public List<Ship> ships;

    @OrderBy("sortOrder")
    @OneToMany(mappedBy = "game", cascade = PERSIST)
    public List<Stage> stages;

    private boolean generalRanking;

    public Game(String title, String cover, String thread) {
        this.title = title;
        this.cover = cover;
        this.thread = thread;
    }

    public static List<Game> findAll() {
        return Ebean.find(Game.class).order("title").findList();
    }

    public List<Ranking> rankings() {
        List<Ranking> rankings = new ArrayList<Ranking>();
        if (generalRanking) {
            rankings.add(createGeneralRanking());
        }
        if (modes.isEmpty()) {
            if (difficulties.isEmpty()) {
                rankings.add(createGeneralRanking());
            } else {
                for (Difficulty difficulty : difficulties) {
                    rankings.add(new Ranking(findBestScoresByVIPPlayers(difficulty, null), difficulty));
                }
            }
        } else {
            for (Mode mode : modes) {
                if (difficulties.isEmpty()) {
                    rankings.add(new Ranking(findBestScoresByVIPPlayers(null, mode), mode));
                } else {
                    for (Difficulty difficulty : difficulties) {
                        rankings.add(new Ranking(findBestScoresByVIPPlayers(difficulty, mode), difficulty, mode));
                    }
                }
            }
        }
        return rankings;
    }

    private Ranking createGeneralRanking() {
        Ranking ranking = new Ranking(findBestScoresByVIPPlayers());
        List<Score> scores = new ArrayList<Score>();
        for (int rank = 0; rank < ranking.scores.size(); rank++) {
            Score score = ranking.scores.get(rank);
            scores.add(new Score(score.id, score.game, score.player, score.stage, score.ship, score.mode, score.difficulty, score.comment, score.platform, score.value, score.photo, score.replay, rank + 1));
        }
        Ranking ranking1 = new Ranking(scores);
        ranking1.general = true;
        return ranking1;
    }

    public Collection<Score> findBestScoresByVIPPlayers(final Difficulty difficulty, final Mode mode) {
        List<Score> scores = Score.finder.
                fetch("mode").
                fetch("difficulty").
                fetch("player").
                where().conjunction().
                add(eq("game", this)).
                add(eq("difficulty", difficulty)).
                add(eq("mode", mode)).
                orderBy((mode == null || !mode.isTimerScore()) ? "value desc" : "value").findList();
        return keepBestScoreByVIPPlayer(scores);
    }

    private Collection<Score> findBestScoresByVIPPlayers() {
        if (scores == null) {
            return new ArrayList<Score>();
        }
        List<Score> scores = Score.finder.
                where().conjunction().
                add(eq("game", this)).
                orderBy("value desc").findList();
        return keepBestScoreByVIPPlayer(scores);
    }

    private Collection<Score> keepBestScoreByVIPPlayer(List<Score> scores) {
        final Set<Player> players = new HashSet<Player>();
        return filter(scores, new Predicate<Score>() {
            @Override
            public boolean apply(@Nullable Score score) {
                if (players.contains(score.player)) {
                    return false;
                }
                players.add(score.player);
                return true;
            }
        });
    }

    public String post() {
        return thread.replace("viewtopic.php?", "posting.php?mode=reply&f=20&");
    }

    @Override
    public String toString() {
        return title;
    }

    public String getEscapedTitle() {
        String s = title.replaceAll("[^a-zA-Z0-9]", "_");
        s = s.replaceAll("_(_)*", "_");
        if (s.endsWith("_")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    public void recomputeRankings() {
        for (Score score : allScores) {
            score.updateRank(null);
            score.update();
        }
        rankings();
    }

    public Collection<Player> getPlayers() {
        Set<Player> players = new HashSet<Player>();
        for (Score score : scores) {
            players.add(score.player);
        }
        return players;
    }

    @Override
    public int compareTo(Game game) {
        return this.title.compareTo(game.title);
    }

    public boolean hasShip() {
        return ships != null && !ships.isEmpty();
    }

    public boolean hasDifficulties() {
        return difficulties != null && !difficulties.isEmpty();
    }

    public boolean hasModes() {
        return modes != null && !modes.isEmpty();
    }

    public int getOneCreditCount() {
        Collection<Score> oneCreditScores = filter(allScores, new Predicate<Score>() {
            @Override
            public boolean apply(@Nullable Score score) {
                return score.onecc;
            }
        });
        Set<String> uniqueOneCreditScores = new HashSet<String>();
        for (Score oneCreditScore : oneCreditScores) {
            String player = oneCreditScore.player.name;
            String mode = oneCreditScore.modeName();
            String difficulty = oneCreditScore.difficultyName();
            uniqueOneCreditScores.add(player + mode + difficulty);
        }
        return uniqueOneCreditScores.size();
    }

    public boolean hasTimerScores() {
        if (this.modes == null || this.modes.isEmpty()) {
            return false;
        }
        for (Mode mode : modes) {
            if (mode.isTimerScore()) {
                return true;
            }
        }
        return false;
    }

    public Collection<Score> getAllOneCCS() {
        MultiKeyMap map = new MultiKeyMap();
        for (Score onecc : this.oneccs) {
            Score score = (Score) map.get(onecc.player, onecc.mode, onecc.difficulty);
            if (score == null) {
                map.put(onecc.player, onecc.mode, onecc.difficulty, onecc);
            } else if (score.isWorstThan(onecc)) {
                map.put(onecc.player, onecc.mode, onecc.difficulty, onecc);
            }
        }
        List<Score> scores = new ArrayList<Score>(map.values());
        Collections.sort(scores, new Comparator<Score>() {
            @Override
            public int compare(Score o1, Score o2) {
                return o1.player.name.compareToIgnoreCase(o2.player.name);
            }
        });
        return scores;
    }
}
