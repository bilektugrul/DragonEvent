package io.github.greenmc.dragonevent.event;

import io.github.greenmc.dragonevent.DragonEvent;
import io.github.greenmc.dragonevent.util.Utils;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class EventTask extends BukkitRunnable {

    private final Event event;
    private final long startTimeTicks;

    private long remainingTimeTicks;
    private long minuteCalculate;

    public EventTask(DragonEvent plugin) {
        this.event = plugin.getEvent();

        this.startTimeTicks = (Utils.getInt("dragon-event-time") * 60L) * 20;
        this.remainingTimeTicks = startTimeTicks;

        runTaskTimer(plugin, 20, 20);
    }

    @Override
    public void run() {
        if (!event.isActive()) {
            this.cancel();
            return;
        }

        remainingTimeTicks = remainingTimeTicks - 20;
        minuteCalculate = minuteCalculate + 20;

        if (Utils.ticksToMinutes(minuteCalculate) == 1) {
            minuteCalculate = 0;
        }

        List<LeaderboardEntry> newLeaderboard = new ArrayList<>();
        for (EventPlayerSession session : event.getCurrentSessions()) {
            newLeaderboard.add(new LeaderboardEntry(session.getPlayer().getName(), session.getGivenDamage()));
        }

        event.setLeaderboard(newLeaderboard.stream()
                .sorted(Comparator.comparingDouble(LeaderboardEntry::getValue).reversed())
                .collect(Collectors.toList()));

        if (remainingTimeTicks == 0) {
            event.finish(true);
            this.cancel();
        }
    }

    public long getRemainingTimeTicks() {
        return remainingTimeTicks;
    }

}