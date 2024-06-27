package io.github.greenmc.dragonevent.rewards;

import com.google.common.collect.Lists;
import io.github.greenmc.dragonevent.DragonEvent;
import io.github.greenmc.dragonevent.util.Utils;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class RewardManager {

    private final DragonEvent plugin;
    private final List<Reward> rewards = new ArrayList<>();

    private Reward normalReward;

    public RewardManager(DragonEvent plugin) {
        this.plugin = plugin;

        load();
    }

    public void load() {
        FileConfiguration config = plugin.getConfig();
        rewards.clear();

        for (String key : config.getConfigurationSection("endgame-commands.places").getKeys(false)) {
            int place = Integer.parseInt(key);
            List<String> commands = new ArrayList<>();
            List<String> msgs = new ArrayList<>();

            for (String value : config.getStringList("endgame-commands.places." + key)) {
                if (value.startsWith("msg: ")) msgs.add(Utils.colored(value.replace("msg: ", "")));
                else commands.add(value);
            }
            rewards.add(new Reward(place, commands, msgs));
        }

        List<String> commands = new ArrayList<>();
        List<String> msgs = new ArrayList<>();

        for (String value : config.getStringList("endgame-commands.every-other-player")) {
            if (value.startsWith("msg: ")) msgs.add(Utils.colored(value.replace("msg: ", "")));
            else commands.add(value);
        }

        normalReward = new Reward(23123, commands, msgs);
    }

    public Reward getRewardOf(int place) {
        for (Reward reward : rewards) {
            if (reward.getPlace() == place) {
                return reward;
            }
        }

        return new Reward(999, Lists.newArrayList(), Lists.newArrayList());
    }

    public List<Reward> getRewards() {
        return rewards;
    }

    public Reward getNormalReward() {
        return normalReward;
    }

}