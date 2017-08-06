package models;

import actions.User;
import com.google.common.base.Function;
import com.google.common.base.Joiner;

import javax.annotation.Nullable;
import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.*;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.transform;
import static java.math.RoundingMode.HALF_UP;

public class Ranking {

    public Difficulty difficulty;

    public Mode mode;

    public List<Score> scores;

    @Transient
    public String difficultyName;

    @Transient
    public String modeName;

    public boolean general;

    Ranking() {
    }

    public Ranking(Collection<Score> scores) {
        this.scores = new ArrayList<Score>();
        int rank = 1;
        for (Score score : scores) {
            if (score.rank == null) {
                score.updateRank(rank);
                score.update();
            }
            this.scores.add(score);
            rank++;
        }
    }

    public Ranking(Collection<Score> scores, Difficulty difficulty) {
        this(scores);
        this.difficulty = difficulty;
        this.difficultyName = difficulty.name;
    }

    public Ranking(Collection<Score> scores, Mode mode) {
        this(scores);
        this.mode = mode;
        this.modeName = mode.name;
    }

    public Ranking(Collection<Score> scores, Difficulty difficulty, Mode mode) {
        this(scores);
        this.difficulty = difficulty;
        this.difficultyName = difficulty.name;

        this.mode = mode;
        this.modeName = mode.name;
    }

    public String joinedPlayerCountPerSplittedScore() {
        TreeSet<BigDecimal> scores = new TreeSet<BigDecimal>(scoresWithAverageScore());
        List<Integer> playerPerCategories = new ArrayList<Integer>();
        for (BigDecimal category : getSplittedScores()) {
            playerPerCategories.add(scores.tailSet(category).size());
        }
        return Joiner.on(",").join(playerPerCategories);
    }

    public String joinedSplittedScores() {
        return Joiner.on(",").join(getSplittedScores());
    }

    private List<BigDecimal> scoresWithAverageScore() {
        List<BigDecimal> scores = newArrayList(transform(this.scores, new Function<Score, BigDecimal>() {
            @Nullable
            @Override
            public BigDecimal apply(@Nullable Score score) {
                return score.value;
            }
        }));
        Collections.sort(scores);
        return scores;
    }

    private List<BigDecimal> getSplittedScores() {
        TreeSet<BigDecimal> scoresMaps = new TreeSet<BigDecimal>(scoresWithAverageScore());
        List<BigDecimal> scoreCategories = new ArrayList<BigDecimal>();
        BigDecimal min = scoresMaps.first();
        BigDecimal max = scoresMaps.last();
        if (min.equals(max)) {
            min = BigDecimal.ZERO;
        }
        BigDecimal step = (max.subtract(min)).divide(BigDecimal.valueOf(scoresMaps.size()), HALF_UP);
        scoreCategories.add(min);
        for (int i = 1; i < (scoresMaps.size() - 1); i++) {
            scoreCategories.add(scoreCategories.get(i - 1).add(step));
        }
        scoreCategories.add(max);
        TreeSet<BigDecimal> longs = new TreeSet<BigDecimal>(scoreCategories);

        Player current = User.current();
        for (Score score : scores) {
            if (score.isPlayedBy(current)) {
                longs.add(score.value);
                break;
            }
        }

        return new ArrayList<BigDecimal>(longs);
    }

    public String uniqueKey() {
        String difficultyId = "";
        if (difficulty != null) {
            difficultyId = difficulty.id.toString();
        }
        String modeId = "";
        if (mode != null) {
            modeId = mode.id.toString();
        }
        return difficultyId + "_" + modeId;
    }
}
