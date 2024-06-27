package io.github.greenmc.dragonevent.listeners;

import io.github.greenmc.dragonevent.DragonEvent;
import io.github.greenmc.dragonevent.util.Utils;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

public class DragonListener implements Listener {

    private final DragonEvent plugin;

    public DragonListener(DragonEvent plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDragonSpawn(EntitySpawnEvent event) {
        if (event.getEntity().getType().equals(EntityType.ENDER_DRAGON)) {
            if (!plugin.getEvent().isActive()) return;

            EnderDragon dragon = (EnderDragon) event.getEntity();
            dragon.setCustomName(Utils.getColoredString("dragon.name"));
            dragon.setCustomNameVisible(true);
            dragon.setGlowing(Utils.getBoolean("dragon.glow"));
            dragon.setHealth(Utils.getDouble("dragon.health"));
        }
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (!event.getEntity().getType().equals(EntityType.ENDER_DRAGON)) return;
        if (!event.getEntity().getName().equals(Utils.getColoredString("dragon.name"))) return;

        plugin.getEvent().finish(false);
    }

}