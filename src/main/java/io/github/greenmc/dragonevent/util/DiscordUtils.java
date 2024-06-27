package io.github.greenmc.dragonevent.util;

import io.github.greenmc.dragonevent.DragonEvent;
import me.clip.placeholderapi.PlaceholderAPI;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.OffsetDateTime;
import java.util.function.Function;

public class DiscordUtils {

    private static final DragonEvent plugin = JavaPlugin.getPlugin(DragonEvent.class);
    private static final FileConfiguration config = plugin.getConfig();

    public static void sendTimerEmbed(long time) {
        if (plugin.getBot() == null) return;

        TextChannel channel = plugin.getBot().getTextChannelById(config.getString("discord.channel-id", "0"));
        if (channel != null) {
            ConfigurationSection embedSec = config.getConfigurationSection("discord.embeds.timer");
            if (embedSec != null) {
                MessageEmbed embed = getEmbedFromYml(embedSec, s -> {
                    s = s.replace("%timestamp%", String.valueOf(time));
                    s = PlaceholderAPI.setPlaceholders(null, s);
                    return s;
                }).build();
                MessageCreateAction action = channel.sendMessageEmbeds(embed);
                String message = config.getString("discord.embeds.timer.message", "");
                if (!message.isEmpty()) {
                    action.setContent(message);
                }
                action.queue();
            }
        }
    }

    public static void sendStartEmbed() {
        if (plugin.getBot() == null) return;

        TextChannel channel = plugin.getBot().getTextChannelById(config.getString("discord.channel-id", "0"));
        if (channel != null) {
            ConfigurationSection embedSec = config.getConfigurationSection("discord.embeds.start");
            if (embedSec != null) {
                MessageEmbed embed = getEmbedFromYml(embedSec, s -> {
                    s = PlaceholderAPI.setPlaceholders(null, s);
                    return s;
                }).build();
                MessageCreateAction action = channel.sendMessageEmbeds(embed);
                String message = config.getString("discord.embeds.start.message", "");
                if (!message.isEmpty()) {
                    action.setContent(message);
                }
                action.queue();
            }
        }
    }

    public static void sendEndEmbed() {
        if (plugin.getBot() == null) return;
        TextChannel channel = plugin.getBot().getTextChannelById(config.getString("discord.channel-id", "0"));
        if (channel != null) {
            ConfigurationSection embedSec = config.getConfigurationSection("discord.embeds.end");
            if (embedSec != null) {
                MessageEmbed embed = getEmbedFromYml(embedSec, s -> {
                    s = PlaceholderAPI.setPlaceholders(null, s);
                    return s;
                }).build();
                MessageCreateAction action = channel.sendMessageEmbeds(embed);
                String message = config.getString("discord.embeds.end.message", "");
                if (!message.isEmpty()) {
                    action.setContent(message);
                }
                action.queue();
            }
        }
    }

    public static EmbedBuilder getEmbedFromYml(ConfigurationSection yml, Function<String, String> placeholders) {
        EmbedBuilder builder = new EmbedBuilder();
        String url = yml.getString("url", null);
        String title = yml.getString("title", null);
        String description = yml.getString("description", null);
        OffsetDateTime timestamp = yml.getString("timestamp", null) != null ? OffsetDateTime.parse(yml.getString("timestamp")) : null;
        int color = Integer.parseInt(yml.getString("color", "0"), 16);
        String thumbnail = yml.getString("thumbnail", null);
        String author;
        if (yml.isString("author")) {
            author = yml.getString("author", null);
        } else {
            author = yml.getString("author.name", null);
        }
        String authorUrl = yml.getString("author.url", null);
        String authorIconUrl = yml.getString("author.icon-url", null);
        String image = yml.getString("image", null);
        String footer;
        if (yml.isString("footer")) {
            footer = yml.getString("footer", null);
        } else {
            footer = yml.getString("footer.text", null);
        }
        String footerUrl = yml.getString("footer.icon-url", null);

        if (url != null && !url.isEmpty()) {
            if (title != null) {
                builder.setTitle(placeholders.apply(title), placeholders.apply(url));
            }
        } else {
            if (title != null) {
                builder.setTitle(placeholders.apply(title));
            }
        }
        if (description != null) builder.setDescription(placeholders.apply(description));
        if (timestamp != null) builder.setTimestamp(timestamp);
        if (color != 0) builder.setColor(color);
        if (thumbnail != null && !thumbnail.isEmpty()) builder.setThumbnail(placeholders.apply(thumbnail));
        if (author != null) {
            if (authorUrl != null && authorIconUrl != null && !authorIconUrl.isEmpty()) {
                builder.setAuthor(placeholders.apply(author), placeholders.apply(authorUrl), placeholders.apply(authorIconUrl));
            } else if (authorIconUrl == null || authorIconUrl.isEmpty()) {
                builder.setAuthor(placeholders.apply(author), placeholders.apply(authorUrl));
            }
        }
        if (image != null && !image.isEmpty()) builder.setImage(placeholders.apply(image));
        if (footer != null) {
            if (footerUrl != null && !footerUrl.isEmpty()) {
                builder.setFooter(placeholders.apply(footer), placeholders.apply(footerUrl));
            } else {
                builder.setFooter(placeholders.apply(footer));
            }
        }
        ConfigurationSection fields = yml.getConfigurationSection("fields");
        if (fields != null) {
            for (String key : fields.getKeys(false)) {
                ConfigurationSection fieldSec = fields.getConfigurationSection(key);
                String name = fieldSec.getString("name");
                String value = fieldSec.getString("value");
                boolean inline = fieldSec.getBoolean("inline", false);
                builder.addField(placeholders.apply(name), placeholders.apply(value), inline);
            }
        }
        return builder;
    }

}
