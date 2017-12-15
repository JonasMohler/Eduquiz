package ch.ethz.inf.vs.quizio;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by peter on 01.12.17.
 */

public class Player {
    public final String name;
    private boolean hasAnswerd = false;
    protected Integer score;
    protected Integer rank;
    public Clock clock;

    public Player(String name) {
        this.name = name;
        this.score = 0;
        this.rank = 0;
        this.clock = new Clock();
    }
    public Integer getScore(){
        return  score;
    }
    public Integer getRank(){
        return  rank;
    }
    public void setScore(int Score){this.score = Score; }

    synchronized public void answerd(boolean result) {
        if (result = true) {
            score++;
        }
        hasAnswerd = true;
    }
}