package io.github.greenmc.dragonevent.event;

import io.github.greenmc.dragonevent.DragonEvent;
import io.github.greenmc.dragonevent.rewards.Reward;
import io.github.greenmc.dragonevent.rewards.RewardManager;
import io.github.greenmc.dragonevent.util.DiscordUtils;
import io.github.greenmc.dragonevent.util.Utils;
import me.clip.placeholderapi.PlaceholderAPI;
import me.despical.commons.configuration.ConfigUtils;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.codehaus.plexus.util.FileUtils;

import java.util.ArrayList;
import java.util.List;

public class Event {

    private final DragonEvent plugin;
    private final RewardManager rewardManager;
    private final FileConfiguration locationsFile;

    private final List<EventPlayerSession> currentSessions = new ArrayList<>();
    private final List<Player> joinedBefore = new ArrayList<>();
    private List<LeaderboardEntry> leaderboard = new ArrayList<>();

    private boolean active;

    private final int requiredKills;
    private int remainingKills;
    private EventTask eventTask;
    private World eventWorld;

    public Event(DragonEvent plugin) {
        this.plugin = plugin;
        this.requiredKills = Utils.getInt("dragon.required-kills");
        this.rewardManager = plugin.getRewardManager();
        this.locationsFile = ConfigUtils.getConfig(plugin, "locations");
    }

    public void start() {
        if (this.active) return;

        this.active = true;
        this.remainingKills = this.requiredKills;

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
        String worldName = plugin.getConfig().getString("event-world-name");
        boolean alreadyCreated = false;

        if (eventWorld == null) {
            eventWorld = createWorld();
            alreadyCreated = true;
        } else {
            Location spawn = getSpawn();
            if (spawn != null) {
                eventWorld.getPlayers().forEach(player -> player.teleport(spawn));
            }
        }

        if (!alreadyCreated) {
            try {
                if (plugin.getServer().unloadWorld(eventWorld, false)) {
                    FileUtils.forceDelete(eventWorld.getWorldFolder());
                    eventWorld = createWorld();
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

    private World createWorld() {
        WorldCreator worldCreator = new WorldCreator(Utils.getString("event-world-name"));
        worldCreator.environment(World.Environment.THE_END);
        World world = worldCreator.createWorld();
        world.setGameRule(GameRule.KEEP_INVENTORY, Utils.getBoolean("end-rules.keepInventory"));
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, Utils.getBoolean("end-rules.announceAdvancements"));
        world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, Utils.getBoolean("end-rules.showDeathMessages"));
        return world;
    }

    public void enter(Player player) {
        if (!this.isActive()) {
            player.sendMessage(Utils.getMessage("not-active", player));
            return;
        }

        if (this.isPlaying(player)) {
            player.sendMessage(Utils.getMessage("already-playing", player));
            return;
        }

        long price = Utils.getLong("event-price");
        if (this.plugin.getEconomy().has(player, price)) {

            if (this.isJoinedBefore(player)) {
                if (Utils.getBoolean("price-required-for-every-join")) {
                    this.plugin.getEconomy().withdrawPlayer(player, price);
                }
            } else {
                this.plugin.getEconomy().withdrawPlayer(player, price);
            }
        }

        this.joinedBefore.add(player);
        this.currentSessions.add(new EventPlayerSession(player));

        player.teleport(eventWorld.getSpawnLocation());
        player.sendMessage(Utils.getMessage("joined", player));

    }

    public boolean isJoinedBefore(Player player) {
        return joinedBefore.contains(player);
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

        player.teleport(getSpawn());
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

        if (eventWorld != null) {
            try {
                eventWorld.getEnderDragonBattle().getEnderDragon().remove();
                eventWorld.getEnderDragonBattle().getBossBar().setVisible(false);
            } catch (Exception ignored) {

            }
        }

        for (EventPlayerSession session : currentSessions) {
            session.getPlayer().teleport(getSpawn());
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

                for (EventPlayerSession session : getCurrentSessions()) {
                    Player player = session.getPlayer();

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

    public int getRequiredKills() {
        return requiredKills;
    }

    public int getRemainingKills() {
        return remainingKills;
    }

    public void setRemainingKills(int remainingKills) {
        this.remainingKills = remainingKills;
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
            if (entry.name().equals(player.getName())) return i;
            ++i;
        }

        return -1;
    }

    public Player getPlayerAt(int place) {
        if (!leaderboard.isEmpty()) {
            if (leaderboard.size() < place) {
                return null;
            }

            return Bukkit.getPlayerExact(leaderboard.get(place - 1).name());
        }

        return null;
    }

    public double getDamageOf(String name) {
        for (LeaderboardEntry entry : leaderboard) {
            if (entry.name().equals(name)) return entry.value();
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

    public FileConfiguration getLocationsFile() {
        return locationsFile;
    }

    public World getEventWorld() {
        return eventWorld;
    }

    public Location getSpawn() {
        Location spawn = locationsFile.getLocation("spawn");
        return spawn == null ? plugin.getServer().getWorlds().get(0).getSpawnLocation() : spawn;
    }

    public void setSpawn(Location location) {
        locationsFile.set("spawn", location);
    }
}