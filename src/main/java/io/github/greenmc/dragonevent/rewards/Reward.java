package io.github.greenmc.dragonevent.rewards;

import java.util.List;

public class Reward {

    private final int place;
    private final List<String> commands, msgsToSend;

    public Reward(int place, List<String> commands, List<String> msgsToSend) {
        this.place = place;
        this.commands = commands;
        this.msgsToSend = msgsToSend;
    }

    public int getPlace() {
        return place;
    }

    public List<String> getCommands() {
        return commands;
    }

    public List<String> getMsgsToSend() {
        return msgsToSend;
    }

}