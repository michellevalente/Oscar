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

    public String serialize() {
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

    @Override
    public String toString() {
        String str = this.type + " ";

        if (type.equals("move")) {
            if (param == 1) str += "Forward";
            else if (param == 0) str += "Backward";
        }
        else if (type.equals("turn")) {
            if (param == 1) str += "Right";
            else if (param == 0) str += "Left";
        }
        else if (type.contains("led")) {
            if (str.charAt(3) == 'r')
                str = "red led ";
            else if (str.charAt(3) == 'b')
                str = "blue led ";

            if (param == 1) str += "ON";
            else if (param == 0) str += "OFF";
        }
        else if (type.equals("buzz") || type.equals("wait"))
            str += Integer.toString(param) + "ms";


        return str;
    }
}
