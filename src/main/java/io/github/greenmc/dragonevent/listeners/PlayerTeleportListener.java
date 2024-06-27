package io.github.greenmc.dragonevent.listeners;

import io.github.greenmc.dragonevent.DragonEvent;
import io.github.greenmc.dragonevent.util.Utils;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerTeleportListener implements Listener {

    private final DragonEvent plugin;
    private final List<UUID> cooldown = new ArrayList<>();

    public PlayerTeleportListener(DragonEvent plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onTeleport(PlayerTeleportEvent event) {
        Location to = event.getTo();
        World world = to.getWorld();
        Player player = event.getPlayer();

        if (event.getCause().equals(PlayerTeleportEvent.TeleportCause.END_PORTAL) ||
                (world != null && world.getName().equalsIgnoreCase(plugin.getConfig().getString("end-world-name", "world_the_end")))) {
            if (!plugin.getEvent().isActive()) {
                String message = Utils.getMessage("not-active", player);
                if (!cooldown.contains(player.getUniqueId())) {
                    player.sendMessage(message);
                    cooldown.add(player.getUniqueId());
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> cooldown.remove(player.getUniqueId()), 60L);
                }
                Location location = plugin.getConfig().getLocation("locations.spawn");
                if (location != null) {
                    player.teleport(location);
                }

                event.setCancelled(true);
            } else {
                plugin.getEvent().enter(player);
            }
        }
    }

}