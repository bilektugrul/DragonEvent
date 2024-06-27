package io.github.greenmc.dragonevent.autostart;

import io.github.greenmc.dragonevent.DragonEvent;
import io.github.greenmc.dragonevent.util.DiscordUtils;
import io.github.greenmc.dragonevent.util.Utils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AutoStartTask extends BukkitRunnable {

    private final DragonEvent plugin;
    private final AutoStartManager autoStartManager;

    private final List<Integer> sentTimerMsgs = new ArrayList<>();

    public AutoStartTask(DragonEvent plugin, AutoStartManager autoStartManager) {
        this.plugin = plugin;
        this.autoStartManager = autoStartManager;
    }

    @Override
    public void run() {
        if (plugin.getEvent().isActive()) return;

        List<AutoStartInfo> infos = autoStartManager.getAutoStartInfos();
        FileConfiguration data = autoStartManager.getAutoStartData();

        LocalDate date = LocalDate.now();
        LocalTime time = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mma");
        String formattedTime = time.format(formatter);

        String monthName = date.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());

        int monthlyMax = Utils.getInt("auto-start.max-per-month");
        int runTimesThisMonth = data.getInt("months." + monthName + ".run-times");
        int dayOfMonth = date.getDayOfMonth();
        List<String> todayDoneTimes = data.getStringList("months." + monthName + ".days." + date.getDayOfMonth() + ".doneTimes");

        if (runTimesThisMonth >= monthlyMax) {
            data.set("next", autoStartManager.findClosestCalendar(true).getTimeInMillis());
            return;
        }

        if (todayDoneTimes.contains(formattedTime)) return;

        for (AutoStartInfo info : infos) {
            List<Calendar> infoTimes = info.getTimes();
            for (Calendar calendarTime : infoTimes) {

                if (info.isCurrentTime(calendarTime)) {
                    todayDoneTimes.add(formattedTime);

                    data.set("last-run", calendarTime.getTimeInMillis());
                    data.set("next", autoStartManager.findClosestCalendar(false).getTimeInMillis());

                    data.set("months." + monthName + ".run-times", runTimesThisMonth + 1);
                    data.set("months." + monthName + ".days." + dayOfMonth + ".doneTimes", todayDoneTimes);

                    plugin.getEvent().start();
                    sentTimerMsgs.clear();

                } else if (autoStartManager.isWithinNextMinutes(calendarTime, 30) && !sentTimerMsgs.contains(30)) {
                    DiscordUtils.sendTimerEmbed(calendarTime.getTimeInMillis() / 1000);
                    sentTimerMsgs.add(30);
                } else if (autoStartManager.isWithinNextMinutes(calendarTime, 15) && !sentTimerMsgs.contains(15)) {
                    DiscordUtils.sendTimerEmbed(calendarTime.getTimeInMillis() / 1000);
                    sentTimerMsgs.add(15);
                } else if (autoStartManager.isWithinNextMinutes(calendarTime, 5) && !sentTimerMsgs.contains(5)) {
                    DiscordUtils.sendTimerEmbed(calendarTime.getTimeInMillis() / 1000);
                    sentTimerMsgs.add(5);
                } else if (autoStartManager.isWithinNextMinutes(calendarTime, 1) && !sentTimerMsgs.contains(1)) {
                    DiscordUtils.sendTimerEmbed(calendarTime.getTimeInMillis() / 1000);
                    sentTimerMsgs.add(1);
                }
            }
        }

    }

}
