package io.github.greenmc.dragonevent.event;

public class LeaderboardEntry {

    private final String name;
    private final double value;

    public LeaderboardEntry(String name, double value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public double getValue() {
        return value;
    }

}