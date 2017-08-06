package models;

import java.util.Collection;

import static com.avaje.ebean.Expr.isNotNull;

public class Timeline {

    public Collection<Score> scores;

    public Timeline() {
        this.scores = Score.finder.
                where(isNotNull("rank")).
                orderBy("createdAt desc").
                setMaxRows(10).
                fetch("game", "*").
                fetch("stage", "*").
                fetch("ship", "*").
                fetch("platform", "*").
                fetch("player", "*").
                fetch("mode", "*").
                fetch("difficulty", "*").
                findList();
    }

}
