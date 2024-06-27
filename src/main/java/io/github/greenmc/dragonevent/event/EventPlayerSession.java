package io.github.greenmc.dragonevent.event;

import org.bukkit.entity.Player;

public class EventPlayerSession {

    private final Player player;

    private double givenDamage;

    public EventPlayerSession(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public void addGivenDamage(double damage) {
        givenDamage += damage;
    }

    public double getGivenDamage() {
        return givenDamage;
    }

}