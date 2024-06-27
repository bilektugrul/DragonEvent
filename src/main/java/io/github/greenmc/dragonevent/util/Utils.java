package io.github.greenmc.dragonevent.util;

import io.github.greenmc.dragonevent.DragonEvent;
import me.clip.placeholderapi.PlaceholderAPI;
import me.despical.commons.miscellaneous.MiscUtils;
import me.despical.commons.string.StringUtils;
import me.despical.commons.util.Strings;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Utils {

    private static final DragonEvent plugin = JavaPlugin.getPlugin(DragonEvent.class);

    public static int getInt(String path) {
        return plugin.getConfig().getInt(path);
    }

    public static String getString(String string) {
        return plugin.getConfig().getString(string);
    }

    public static String getColoredString(String string) {
        return colored(getString(string));
    }

    public static String getPAPIParsedString(String string, Player sender) {
        return PlaceholderAPI.setPlaceholders(sender, getColoredString(string));
    }

    public static List<String> getPAPIParsedStringList(String string, Player sender) {
        return PlaceholderAPI.setPlaceholders(sender, colored(getStringList(string)));
    }

    public static double getDouble(String string) {
        return plugin.getConfig().getDouble(string);
    }

    public static Boolean getBoolean(String string) {
        return plugin.getConfig().getBoolean(string);
    }

    public static List<String> getStringList(String string) {
        return plugin.getConfig().getStringList(string);
    }

    public static String getMessage(String msg, CommandSender player) {
        String message;
        if (plugin.getConfig().isList("messages." + msg)) {
            message = listToString(colored(getStringList("messages." + msg)));
        } else {
            message = colored(getString("messages." + msg));
        }

        if (player instanceof Player) {
            message = message.replace("%player%", player.getName());
            message = PlaceholderAPI.setPlaceholders((Player) player, message);
        } else {
            message = PlaceholderAPI.setPlaceholders(null, message);
        }

        return message;
    }

    public static String colored(String string) {
        return Strings.format(string);
    }

    public static List<String> colored(List<String> strings) {
        List<String> list = new ArrayList<>();
        for (String str : strings) {
            list.add(colored(str));
        }
        return list;
    }

    public static String arrayToString(String[] array) {
        return String.join(" ", array);
    }

    public static String listToStringComma(List<String> list) {
        return String.join(", ", list);
    }

    public static String listToString(List<String> list) {
        return String.join("\n", list);
    }

    public static String itemToString(ItemStack item) {
        ItemMeta meta = item.getItemMeta();

        if (meta.hasDisplayName())
            return meta.getDisplayName();
        else
            return item.getType().name().replace("_", " ");
    }

    public static String placeString(int place) {
        if (place == 1) {
            return place + "st";
        } else if (place == 2) {
            return place + "nd";
        } else if (place == 3) {
            return place + "third";
        } else {
            return place + "th";
        }
    }

    public static <T> T getRandomElement(Set<T> set) {
        if (set == null || set.isEmpty()) {
            throw new IllegalArgumentException("The set cannot be null or empty.");
        }

        int randomIndex = ThreadLocalRandom.current().nextInt(set.size());
        int currentIndex = 0;
        for (T element : set) {
            if (currentIndex == randomIndex) {
                return element;
            }
            currentIndex++;
        }

        return null;
    }

    public static int generateRandomNumber(int minValue, int maxValue) {
        if (minValue >= maxValue) {
            throw new IllegalArgumentException("Invalid range. minValue must be less than maxValue.");
        }

        return ThreadLocalRandom.current().nextInt(minValue, maxValue + 1);
    }

    public static Map<Enchantment, Integer> getEnchantments(FileConfiguration file, String path) {
        Map<Enchantment, Integer> enchs = new HashMap<>();
        if (file.isSet(path)) {
            for (String ench : file.getStringList(path)) {
                String[] split = ench.split(":");
                Enchantment enchantment = Enchantment.getByName(split[0]);
                int level = Integer.parseInt(split[1]);
                enchs.put(enchantment, level);
            }
        }

        return enchs;
    }

    public static <T> T getNextElement(List<T> list, T currentElement) {
        int currentIndex = list.indexOf(currentElement);
        int nextIndex = (currentIndex + 1) % list.size();
        return list.get(nextIndex);
    }

    public static <T> boolean isLastElement(List<T> list, T element) {
        int lastIndex = list.size() - 1;
        T lastElement = list.get(lastIndex);
        return lastElement.equals(element);
    }

    public static String getDayName(Calendar calendar) {
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        switch (dayOfWeek) {
            case Calendar.SUNDAY:
                return "Sunday";
            case Calendar.MONDAY:
                return "Monday";
            case Calendar.TUESDAY:
                return "Tuesday";
            case Calendar.WEDNESDAY:
                return "Wednesday";
            case Calendar.THURSDAY:
                return "Thursday";
            case Calendar.FRIDAY:
                return "Friday";
            case Calendar.SATURDAY:
                return "Saturday";
            default:
                return "Invalid day";
        }
    }

    public static long ticksToMinutes(long ticks) {
        int ticksPerMinute = 20 * 60;
        return ticks / ticksPerMinute;
    }

}