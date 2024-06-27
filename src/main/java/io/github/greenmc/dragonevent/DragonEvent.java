package io.github.greenmc.dragonevent;

import io.github.greenmc.dragonevent.autostart.AutoStartManager;
import io.github.greenmc.dragonevent.commands.EventAdminCommand;
import io.github.greenmc.dragonevent.commands.EventPlayerCommand;
import io.github.greenmc.dragonevent.listeners.DragonListener;
import io.github.greenmc.dragonevent.listeners.PlayerListener;
import io.github.greenmc.dragonevent.event.Event;
import io.github.greenmc.dragonevent.listeners.PlayerTeleportListener;
import io.github.greenmc.dragonevent.placeholders.PAPIPlaceholders;
import io.github.greenmc.dragonevent.rewards.RewardManager;
import io.github.greenmc.dragonevent.util.CommandHandler;
import io.github.greenmc.dragonevent.util.Utils;
import me.despical.commons.configuration.ConfigUtils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.TimeZone;

public class DragonEvent extends JavaPlugin {

    private Event event;
    private AutoStartManager autoStartManager;
    private RewardManager rewardManager;
    private JDA bot;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.rewardManager = new RewardManager(this);
        this.event = new Event(this);
        this.event.resetWorld();

        if (Utils.getBoolean("auto-start.enabled")) {
            this.autoStartManager = new AutoStartManager(this);
            TimeZone.setDefault(TimeZone.getTimeZone(Utils.getString("auto-start.timezone")));
        }

        new PAPIPlaceholders(this).register();

        CommandHandler.register(new EventAdminCommand(this), new EventPlayerCommand(this));
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerTeleportListener(this), this);
        getServer().getPluginManager().registerEvents(new DragonListener(this), this);

        this.bot = JDABuilder.createDefault(Utils.getString("discord.token")).build();
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

}