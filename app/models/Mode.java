package models;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class Mode extends BaseModel<Mode> {

    public String name;

    public String sortOrder;

    @ManyToOne
    public Game game;

    public String scoreType;

    public Mode(String name) {
        this.name = name;
    }

    public Mode(String name, String scoreType) {
        this(name);
        this.scoreType = scoreType;
    }

    @Override
    public String toString() {
        return name;
    }

    public boolean isTimerScore() {
        return "timer".equals(scoreType);
    }

}
