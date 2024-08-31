package io.github.greenmc.dragonevent;

import io.github.greenmc.dragonevent.autostart.AutoStartManager;
import io.github.greenmc.dragonevent.commands.EventAdminCommand;
import io.github.greenmc.dragonevent.commands.EventPlayerCommand;
import io.github.greenmc.dragonevent.economy.VaultManager;
import io.github.greenmc.dragonevent.event.Event;
import io.github.greenmc.dragonevent.listeners.DragonListener;
import io.github.greenmc.dragonevent.listeners.PlayerListener;
import io.github.greenmc.dragonevent.listeners.PlayerTeleportListener;
import io.github.greenmc.dragonevent.placeholders.PAPIPlaceholders;
import io.github.greenmc.dragonevent.rewards.RewardManager;
import io.github.greenmc.dragonevent.util.CommandHandler;
import io.github.greenmc.dragonevent.util.Utils;
import me.despical.commons.configuration.ConfigUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.TimeZone;

public class DragonEvent extends JavaPlugin {

    private Event event;
    private AutoStartManager autoStartManager;
    private RewardManager rewardManager;
    private VaultManager vaultManager;
    private JDA bot;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.vaultManager = new VaultManager(this);
        this.rewardManager = new RewardManager(this);
        this.event = new Event(this);
        this.event.loadWorld();

        if (Utils.getBoolean("auto-start.enabled")) {
            this.autoStartManager = new AutoStartManager(this);
            TimeZone.setDefault(TimeZone.getTimeZone(Utils.getString("auto-start.timezone")));
        }

        new PAPIPlaceholders(this).register();

        CommandHandler.register(new EventAdminCommand(this), new EventPlayerCommand(this));
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerTeleportListener(this), this);
        getServer().getPluginManager().registerEvents(new DragonListener(this), this);

        if (Utils.getBoolean("discord.enabled")) {
            try {
                this.bot = JDABuilder.createDefault(Utils.getString("discord.token")).build();
            } catch (Exception e) {
                getLogger().warning("Something went wrong while trying to connect discord. Please check your token.");
            }
        }
    }

    @Override
    public void onDisable() {
        save();
        event.finish(true);
    }

    public void load() {
        reloadConfig();

        rewardManager.load();
        save();
        autoStartManager.load();
    }

    public void save() {
        if (autoStartManager != null) {
            ConfigUtils.saveConfig(this, autoStartManager.getAutoStartData(), "autostart_data");
        }

        ConfigUtils.saveConfig(this, event.getLocationsFile(), "locations");
    }

    public Event getEvent() {
        return event;
    }

    public AutoStartManager getAutoStartManager() {
        return autoStartManager;
    }

    public RewardManager getRewardManager() {
        return rewardManager;
    }

    public JDA getBot() {
        return bot;
    }

    public Economy getEconomy() {
        return vaultManager.getEconomy();
    }

}