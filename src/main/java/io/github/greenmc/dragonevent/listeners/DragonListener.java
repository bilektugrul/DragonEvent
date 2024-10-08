package io.github.greenmc.dragonevent.listeners;

import io.github.greenmc.dragonevent.DragonEvent;
import io.github.greenmc.dragonevent.event.Event;
import io.github.greenmc.dragonevent.util.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class DragonListener implements Listener {

    private final DragonEvent plugin;
    private final Event event;

    public DragonListener(DragonEvent plugin) {
        this.plugin = plugin;
        this.event = plugin.getEvent();
    }

    @EventHandler
    public void onDragonSpawn(EntitySpawnEvent e) {
        if (e.getEntity().getType().equals(EntityType.ENDER_DRAGON)) {
            if (!plugin.getEvent().isActive()) return;

            World world = e.getEntity().getWorld();
            if (world != event.getEventWorld()) return;

            EnderDragon dragon = (EnderDragon) e.getEntity();
            dragon.setCustomName(Utils.getColoredString("dragon.name"));
            dragon.setCustomNameVisible(true);
            dragon.setGlowing(Utils.getBoolean("dragon.glow"));
            Utils.setGlowing(dragon, ChatColor.valueOf(Utils.getString("dragon.glow-color")));
            dragon.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(Utils.getDouble("dragon.health"));
            dragon.setHealth(Utils.getDouble("dragon.health"));
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (!e.getEntity().getType().equals(EntityType.ENDER_DRAGON)) return;

        LivingEntity ent = (LivingEntity) e.getEntity();

        World world = e.getEntity().getWorld();
        if (world != event.getEventWorld()) return;

        if (e.getFinalDamage() >= ent.getHealth()) {
            int remainingKills = event.getRemainingKills() - 1;
            event.setRemainingKills(remainingKills);

            if (remainingKills != 0) {
                e.setCancelled(true);
                ent.setHealth(ent.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue());

                event.getCurrentSessions().forEach(s -> {
                    Player player = s.getPlayer();

                    if (Utils.getBoolean("titles.respawn.enable")) {
                        String title = Utils.getColoredString("titles.respawn.title").replace("%times%", String.valueOf(event.getRemainingKills()));
                        String subTitle = Utils.getColoredString("titles.respawn.subTitle").replace("%times%", String.valueOf(event.getRemainingKills()));
                        player.sendTitle(title, subTitle, 20, 60, 20);
                    }

                });

                return;
            }

            event.finish(false);
        }


    }

}