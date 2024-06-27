package io.github.greenmc.dragonevent.event;

import io.github.greenmc.dragonevent.DragonEvent;
import io.github.greenmc.dragonevent.rewards.Reward;
import io.github.greenmc.dragonevent.rewards.RewardManager;
import io.github.greenmc.dragonevent.util.DiscordUtils;
import io.github.greenmc.dragonevent.util.Utils;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.codehaus.plexus.util.FileUtils;

import java.util.*;

public class Event {

    private final DragonEvent plugin;
    private final RewardManager rewardManager;

    private final List<EventPlayerSession> currentSessions = new ArrayList<>();
    private List<LeaderboardEntry> leaderboard = new ArrayList<>();

    private boolean active;
    private EventTask eventTask;

    public Event(DragonEvent plugin) {
        this.plugin = plugin;
        this.rewardManager = plugin.getRewardManager();
    }

    public void start() {
        if (this.active) return;

        this.active = true;

        this.eventTask = new EventTask(plugin);
        DiscordUtils.sendStartEmbed();

        plugin.getServer().getOnlinePlayers().forEach(player -> {
            if (Utils.getBoolean("titles.start.enable")) {
                String title = Utils.getColoredString("titles.start.title");
                String subTitle = Utils.getColoredString("titles.start.subTitle");
                player.sendTitle(title, subTitle, 20, 60, 20);
            }

            if (Utils.getBoolean("sounds.start.enable")) {
                Sound sound = Sound.valueOf(Utils.getString("sounds.start.sound"));
                player.playSound(player.getLocation(), sound, 1.0F, 1.0F);
            }
        });

        List<String> cmds = Utils.getStringList("commands.start");
        if (!cmds.isEmpty()) cmds.forEach(command -> {
            if (!command.isEmpty()) {
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
            }
        });

        Bukkit.broadcastMessage(Utils.getMessage("start", null));

    }

    public void resetWorld() {
        World world = plugin.getServer().getWorld(plugin.getConfig().getString("end-world-name", "world_the_end"));
        Location spawn = plugin.getConfig().getLocation("locations.spawn");
        if (world != null && spawn != null) {
            world.getPlayers().forEach(player -> player.teleport(spawn));
        }

        if (world != null) {
            try {
                if (plugin.getServer().unloadWorld(world, false)) {
                    FileUtils.forceDelete(world.getWorldFolder());
                    WorldCreator worldCreator = new WorldCreator(world.getName());
                    worldCreator.copy(world);
                    plugin.getServer().createWorld(worldCreator);
                    plugin.getLogger().info("Dragon event world reset.");
                } else {
                    plugin.getLogger().severe("Something went wrong while unloading event world.");
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Something went terribly wrong with event world creation.");
                e.printStackTrace();
            }
        }
    }

    public void enter(Player player) {
        if (!isActive()) {
            player.sendMessage(Utils.getMessage("not-active", player));
            return;
        }

        if (isPlaying(player)) {
            player.sendMessage(Utils.getMessage("already-playing", player));
            return;
        }

        currentSessions.add(new EventPlayerSession(player));
        player.sendMessage(Utils.getMessage("joined", player));
    }

    public void leave(Player player) {
        if (!isActive()) {
            player.sendMessage(Utils.getMessage("not-active", player));
            return;
        }

        if (!isPlaying(player)) {
            player.sendMessage(Utils.getMessage("not-playing", player));
            return;
        }

        EventPlayerSession session = getSessionOf(player);

        Location spawn = plugin.getConfig().getLocation("locations.spawn");
        player.teleport(spawn);
        currentSessions.remove(session);

        player.sendMessage(Utils.getMessage("left-event", player));
    }

    public void finish(boolean timeOver) {
        if (!this.active) return;

        this.active = false;
        if (this.eventTask != null)  {
            this.eventTask.cancel();
            this.eventTask = null;
        }

        Location spawn = plugin.getConfig().getLocation("locations.spawn");
        World world = plugin.getServer().getWorld(plugin.getConfig().getString("end-world-name", "world_the_end"));
        if (world != null) {
            try {
                world.getEnderDragonBattle().getEnderDragon().remove();
                world.getEnderDragonBattle().getBossBar().setVisible(false);
            } catch (Exception ignored) {

            }
        }

        for (EventPlayerSession session : currentSessions) {
            session.getPlayer().teleport(spawn);
        }

        plugin.getServer().getOnlinePlayers().forEach(player -> {
            if (Utils.getBoolean("titles.end.enable")) {
                String title = Utils.getColoredString("titles.end.title");
                String subTitle = Utils.getColoredString("titles.end.subTitle");
                player.sendTitle(title, subTitle, 20, 60, 20);
            }

            if (Utils.getBoolean("sounds.end.enable")) {
                Sound sound = Sound.valueOf(Utils.getString("sounds.end.sound"));
                player.playSound(player.getLocation(), sound, 1.0F, 1.0F);
            }
        });

        List<String> cmds = Utils.getStringList("commands.end");
        if (!cmds.isEmpty()) cmds.forEach(command -> {
            if (!command.isEmpty()) {
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
            }
        });

        Bukkit.broadcastMessage(Utils.getMessage("end", null));

        if (!timeOver) {
            List<Player> rewardedPlayers = new ArrayList<>();
            for (Reward reward : rewardManager.getRewards()) {
                Player player = getPlayerAt(reward.getPlace());

                if (player == null) {
                    continue;
                }

                rewardedPlayers.add(player);
                for (String cmd : reward.getCommands()) {
                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), PlaceholderAPI.setPlaceholders(player, cmd));
                }

                for (String msg : reward.getMsgsToSend()) {
                    player.sendMessage(PlaceholderAPI.setPlaceholders(player, msg));
                }
            }

            Reward normalReward = rewardManager.getNormalReward();
            if (normalReward != null) {

                for (LeaderboardEntry entry : leaderboard) {
                    Player player = Bukkit.getPlayerExact(entry.getName());

                    for (String cmd : normalReward.getCommands()) {
                        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), PlaceholderAPI.setPlaceholders(player, cmd));
                    }

                    for (String msg : normalReward.getMsgsToSend()) {
                        player.sendMessage(PlaceholderAPI.setPlaceholders(player, msg));
                    }
                }

            }
        } else {
            Bukkit.broadcastMessage(Utils.getMessage("timed-out", null));
        }

        resetWorld();
        leaderboard.clear();
        currentSessions.clear();

        DiscordUtils.sendEndEmbed();
    }

    public boolean isPlaying(Player player) {
        for (EventPlayerSession session : currentSessions) {
            if (session.getPlayer().equals(player)) {
                return true;
            }
        }

        return false;
    }

    public List<LeaderboardEntry> getLeaderboard() {
        return leaderboard;
    }

    public void setLeaderboard(List<LeaderboardEntry> leaderboard) {
        this.leaderboard = leaderboard;
    }

    public int getPlaceOf(Player player) {
        int i = 1;
        for (LeaderboardEntry entry : leaderboard) {
            if (entry.getName().equals(player.getName())) return i;
            ++i;
        }

        return -1;
    }

    public Player getPlayerAt(int place) {
        if (!leaderboard.isEmpty()) {
            if (leaderboard.size() < place) {
                return null;
            }

            return Bukkit.getPlayerExact(leaderboard.get(place - 1).getName());
        }

        return null;
    }

    public double getDamageOf(String name) {
        for (LeaderboardEntry entry : leaderboard) {
            if (entry.getName().equals(name)) return entry.getValue();
        }

        return -1;
    }

    public List<EventPlayerSession> getCurrentSessions() {
        return currentSessions;
    }

    public EventPlayerSession getSessionOf(Player player) {
        for (EventPlayerSession session : currentSessions) {
            if (session.getPlayer().equals(player)) {
                return session;
            }
        }

        return null;
    }

    public boolean isActive() {
        return active;
    }

    public EventTask getEventTask() {
        return eventTask;
    }

}