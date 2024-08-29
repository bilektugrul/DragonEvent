package io.github.greenmc.dragonevent.commands;

import com.google.common.collect.Lists;
import io.github.greenmc.dragonevent.DragonEvent;
import io.github.greenmc.dragonevent.event.Event;
import io.github.greenmc.dragonevent.util.Utils;
import me.despical.commons.serializer.LocationSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EventAdminCommand extends Command {

    private final DragonEvent plugin;

    public EventAdminCommand(DragonEvent plugin) {
        super("dragonadmin", "Dragon event main command", "/dragonadmin <sub>", Lists.newArrayList());

        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!sender.hasPermission("dragonevent.cmd")) {
            sender.sendMessage(Utils.getMessage("no-perm", sender));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(Utils.getMessage("no-arguments", sender));
            return true;
        }

        Event event = plugin.getEvent();
        String arg = args[0];

        if (arg.equalsIgnoreCase("reload")) {
            plugin.load();
            sender.sendMessage(Utils.getMessage("reloaded", sender));
            return true;
        }

        if (arg.equalsIgnoreCase("setspawn") && sender instanceof Player player) {
            Location loc = player.getLocation();
            event.setSpawn(loc);
            player.sendMessage(Utils.getMessage("new-spawn", player).replace("%location%", LocationSerializer.toString(loc)));
        }

        if (arg.equalsIgnoreCase("save")) {
            plugin.save();
            sender.sendMessage(Utils.getMessage("saved", sender));
            return true;
        }

        if (arg.equalsIgnoreCase("start")) {
            if (event.isActive()) {
                sender.sendMessage(Utils.getMessage("already-active", sender));
                return true;
            }

            event.start();
            Bukkit.broadcastMessage(Utils.getMessage("force-start", sender));
            return true;
        }

        if (arg.equalsIgnoreCase("stop")) {
            if (!event.isActive()) {
                sender.sendMessage(Utils.getMessage("not-active", sender));
                return true;
            }

            event.finish(true);
            Bukkit.broadcastMessage(Utils.getMessage("force-stop", sender));
            return true;
        }

        return true;
    }

}