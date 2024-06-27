package io.github.greenmc.dragonevent.placeholders;

import io.github.greenmc.dragonevent.DragonEvent;
import io.github.greenmc.dragonevent.autostart.AutoStartManager;
import io.github.greenmc.dragonevent.event.Event;
import io.github.greenmc.dragonevent.util.Utils;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PAPIPlaceholders extends PlaceholderExpansion {

    private final AutoStartManager autoStartManager;
    private final Event event;
    private final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    public PAPIPlaceholders(DragonEvent plugin) {
        this.autoStartManager = plugin.getAutoStartManager();
        this.event = plugin.getEvent();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "DragonEvent";
    }

    @Override
    public @NotNull String getAuthor() {
        return "bilektugrul";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (params.equalsIgnoreCase("remaining_game")) {
            if (event.isActive()) {
                int minutes = (int) Math.ceil(Utils.ticksToMinutes(event.getEventTask().getRemainingTimeTicks()));
                if (minutes == 0) {
                    return (int) Math.floor((double) event.getEventTask().getRemainingTimeTicks() / 20) + "s";
                }
                return minutes + "m";
            }

            return "0m";
        }

        if (params.equalsIgnoreCase("players")) {
            return String.valueOf(event.getCurrentSessions().size());
        }

        String[] splitted = params.split("_");
        if (splitted[0].equalsIgnoreCase("top")) {
            int place = Integer.parseInt(splitted[3]);
            Player placePlayer = event.getPlayerAt(place);
            if (placePlayer == null) {
                return Utils.getMessage("leaderboard-empty-entry", null);
            }

            if (splitted[2].equalsIgnoreCase("name")) {
                return placePlayer.getName();
            } else if (splitted[2].equalsIgnoreCase("damage")) {
                return decimalFormat.format(event.getDamageOf(placePlayer.getName()));
            }

        }

        if (autoStartManager != null) {

            if (params.equalsIgnoreCase("remaining")) {
                return autoStartManager.getTimeRemainingToNext();
            }

            if (params.equalsIgnoreCase("last")) {
                SimpleDateFormat format = new SimpleDateFormat(Utils.getString("next-event-time-format"));
                return format.format(new Date(autoStartManager.getLast()));
            }

            if (params.equalsIgnoreCase("next")) {
                SimpleDateFormat format = new SimpleDateFormat(Utils.getString("next-event-time-format"));
                return format.format(new Date(autoStartManager.getNext()));
            }

        }

        return "";
    }

}
