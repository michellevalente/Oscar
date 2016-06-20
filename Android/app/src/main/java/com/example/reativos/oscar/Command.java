package com.example.reativos.oscar;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by calmattoso on 6/18/16.
 */
public class Command {
    public List<Command> children = new ArrayList<>();
    public String type;
    public int param;

    Command(String type, int param) {
        this.type = type;
        this.param = param;
    }

    Command(String type) {
        this(type, -1);
    }

    public boolean addCommand(Command c) {
        if (type.equals("loop") && !c.type.equals("loop")) {
            children.add(c);
            return true;
        }
        return false;
    }

    public boolean removeCommand(int index) {
        if (index > children.size())
            return false;
        children.remove(index);
        return true;
    }

    public String toString() {
        String str = this.type;

        if (type.equals("loop")) {
            // tratar dos filhos
            for (Command c : children) {
                str += c.toString();
            }
        }
        else if (param != -1) {
            str += Integer.toString(param);
        }

        return str;
    }
}
