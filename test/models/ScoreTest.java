package models;

import org.junit.Test;

import java.math.BigDecimal;

import static org.fest.assertions.Assertions.assertThat;

public class ScoreTest {

    @Test
    public void worst_standard_value() {
        Game game = new Game("game", "", "");
        game.id = 1L;

        Score scoreA = new Score(BigDecimal.ONE);
        scoreA.game = game;

        Score scoreB = new Score(BigDecimal.TEN);
        scoreB.game = game;
        assertThat(scoreA.isWorstThan(scoreB)).isTrue();
    }

    @Test
    public void worst_standard_value_with_numeric_mode() {
        Game game = new Game("game", "", "");
        game.id = 1L;

        Mode mode = new Mode("mode");
        mode.id = 1L;

        Score scoreA = new Score(BigDecimal.ONE);
        scoreA.game = game;
        scoreA.mode = mode;

        Score scoreB = new Score(BigDecimal.TEN);
        scoreB.game = game;
        scoreB.mode = mode;

        assertThat(scoreA.isWorstThan(scoreB)).isTrue();
    }

    @Test
    public void worst_standard_value_with_difficulty() {
        Game game = new Game("game", "", "");
        game.id = 1L;

        Difficulty difficulty = new Difficulty("difficulty");
        difficulty.id = 1L;

        Score scoreA = new Score(BigDecimal.ONE);
        scoreA.game = game;
        scoreA.difficulty = difficulty;

        Score scoreB = new Score(BigDecimal.TEN);
        scoreB.game = game;
        scoreB.difficulty = difficulty;

        assertThat(scoreA.isWorstThan(scoreB)).isTrue();
    }

    @Test
    public void worst_standard_value_with_difficulty_and_mode() {
        Game game = new Game("game", "", "");
        game.id = 1L;

        Difficulty difficulty = new Difficulty("difficulty");
        difficulty.id = 1L;

        Mode mode = new Mode("mode");
        mode.id = 1L;

        Score scoreA = new Score(BigDecimal.ONE);
        scoreA.game = game;
        scoreA.difficulty = difficulty;
        scoreA.mode = mode;

        Score scoreB = new Score(BigDecimal.TEN);
        scoreB.game = game;
        scoreB.difficulty = difficulty;
        scoreB.mode = mode;

        assertThat(scoreA.isWorstThan(scoreB)).isTrue();
    }

    @Test
    public void worst_timer_value() {
        Game game = new Game("game", "", "");
        game.id = 1L;

        Mode mode = new Mode("mode");
        mode.scoreType = "timer";
        mode.id = 1L;

        Score scoreA = new Score(BigDecimal.ONE);
        scoreA.game = game;
        scoreA.mode = mode;

        Score scoreB = new Score(BigDecimal.TEN);
        scoreB.game = game;
        scoreB.mode = mode;
        assertThat(scoreA.isWorstThan(scoreB)).isFalse();
    }


}
