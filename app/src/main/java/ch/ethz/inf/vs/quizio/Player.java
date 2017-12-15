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
    protected Integer score = 0;
    protected Integer rank = 0;

    public Player(String name) {
        this.name = name;
    }
    public Integer getScore(){
        return  score;
    }
    public Integer getRank(){
        return  rank;
    }

    synchronized public void answerd(boolean result) {
        if (result = true) {
            score++;
        }
        hasAnswerd = true;
    }
}