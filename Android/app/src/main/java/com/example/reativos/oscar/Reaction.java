package com.example.reativos.oscar;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by calmattoso on 6/18/16.
 */
public class Reaction {
    public List<Command> children = new ArrayList<>();
    public Types type;

    public interface Types {
        String GENERIC = "0";
        String CAR_BLOCK = "1";
        String TIMER = "2";
    }

    Reaction(Types type) {
        this.type = type;
    }

    public void addCommand(Command c) {
        children.add(c);
    }

    public boolean removeCommand(int index) {
        if (index > children.size())
            return false;
        children.remove(index);
        return true;
    }

    public String toString() {
        String str = type.toString();

        for(Command c : children) {
            str += c.toString();
        }

        return str;
    }
}
