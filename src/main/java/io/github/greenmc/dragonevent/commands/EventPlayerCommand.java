package io.github.greenmc.dragonevent.commands;

import io.github.greenmc.dragonevent.DragonEvent;
import io.github.greenmc.dragonevent.event.Event;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;

public class EventPlayerCommand extends Command {

    private final DragonEvent plugin;

    public EventPlayerCommand(DragonEvent plugin) {
        super("dragon", "Dragon event player command", "/dragon <sub>", Collections.singletonList("dragonevent"));

        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        if (args.length == 0) {
            return true;
        }

        Event event = plugin.getEvent();
        String arg = args[0];

        if (arg.equalsIgnoreCase("join")) {
        }

        if (arg.equalsIgnoreCase("leave")) {
            event.leave(player);
        }

        return true;
    }

}
