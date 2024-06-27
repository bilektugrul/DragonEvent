package io.github.greenmc.dragonevent.listeners;

import io.github.greenmc.dragonevent.DragonEvent;
import io.github.greenmc.dragonevent.event.Event;
import io.github.greenmc.dragonevent.util.Utils;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final DragonEvent plugin;
    private final Event event;

    public PlayerListener(DragonEvent plugin) {
        this.plugin = plugin;
        this.event = plugin.getEvent();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if (event.isPlaying(e.getPlayer())) event.leave(e.getPlayer());
    }

    @EventHandler
    public void onVoid(EntityDamageEvent e) {
        if (e.getCause() == EntityDamageEvent.DamageCause.VOID && e.getEntity() instanceof Player victim) {
            if (event.isPlaying(victim)) event.leave(victim);
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player player = e.getPlayer();
        String command = e.getMessage();

        if (player.hasPermission("dragonevent.usecmds")) return;

        if (event.isActive() && event.isPlaying(player) && !command.startsWith("dragon")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (!event.isActive()) return;

        Entity victimEntity = e.getEntity();
        Entity attackerEntity = e.getDamager();

        if (victimEntity.getType().equals(EntityType.PLAYER) && attackerEntity.getType().equals(EntityType.PLAYER)) {
            e.setCancelled(!Utils.getBoolean("end-rules.pvp"));
            return;
        }

        Player attacker = null;
        if (victimEntity.getType().equals(EntityType.ENDER_DRAGON)) {
            if (attackerEntity.getType().equals(EntityType.ARROW) || attackerEntity.getType().equals(EntityType.SPECTRAL_ARROW)) {
                attacker = (Player) ((Arrow) attackerEntity).getShooter();
            } else if (attackerEntity.getType().equals(EntityType.PLAYER)) {
                attacker = (Player) attackerEntity;
            }
        }

        if (attacker == null) return;

        if (event.isPlaying(attacker)) {
            event.getSessionOf(attacker).addGivenDamage(e.getFinalDamage());
        }
    }


    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        if (!event.isActive()) {
            return;
        }

        Player victim = e.getEntity().getPlayer();
        if (event.isPlaying(victim)) {
            event.leave(victim);
            e.setKeepInventory(Utils.getBoolean("end-rules.keepInventory"));
        }
    }

}