package ch.ethz.inf.vs.quizio;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jonas on 11/12/2017.
 */

public class Clock {

    //debugging
    private static final String TAG = "Clock";

    //implements a vector clock
    private Map<String,Integer> vector;


    public Clock(){vector = new HashMap<>();}

    public void update(Clock other){
        for (Map.Entry<String,Integer> entry :  other.vector.entrySet())
            vector.put(entry.getKey(), Math.max(getTime(entry.getKey()), entry.getValue()));
    }

    public void setClock(Clock other){
        vector = new HashMap<>(other.vector);
    }

    public void tick(String playerName){
        addPlayer(playerName,getTime(playerName)+1);
    }

    public boolean happenedBefore(Clock other){
        int a, b;
        boolean leq = true;
        boolean le  = false;
        for (Map.Entry<String,Integer> kv : vector.entrySet()) {
            a = kv.getValue();
            b = other.getTime(kv.getKey());
            if (a > b)
                leq = false;
            if (a < b)
                le = true;
        }
        return leq && le;


    }

    public void addPlayer(String playerName, Integer time){
        vector.put(playerName,Math.max(getTime(playerName),time));
    }

    public int getTime(String playerName){
        return vector.containsKey(playerName) ? vector.get(playerName):0;
    }


}
