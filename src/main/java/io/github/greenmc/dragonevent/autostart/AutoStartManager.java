package io.github.greenmc.dragonevent.autostart;

import com.google.common.collect.Lists;
import io.github.greenmc.dragonevent.DragonEvent;
import io.github.greenmc.dragonevent.util.Utils;
import me.despical.commons.configuration.ConfigUtils;
import org.bukkit.configuration.file.FileConfiguration;

import java.text.DateFormatSymbols;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class AutoStartManager {

    private final DragonEvent plugin;
    private final List<AutoStartInfo> autoStartInfos = new ArrayList<>();

    private AutoStartTask autoStartTask;
    private FileConfiguration autoStartData;

    public AutoStartManager(DragonEvent plugin) {
        this.plugin = plugin;

        load();
    }

    public void load() {
        autoStartInfos.clear();

        this.autoStartData = ConfigUtils.getConfig(plugin, "autostart_data");
        if (autoStartTask != null) {
            autoStartTask.cancel();
        }

        LocalDate date = LocalDate.now();
        String monthName = date.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());

        int monthlyMax = Utils.getInt("auto-start.max-per-month");
        int runTimesThisMonth = autoStartData.getInt("months." + monthName + ".run-times");
        boolean nextMonth = runTimesThisMonth >= monthlyMax;

        FileConfiguration config = plugin.getConfig();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mma");

        for (String day : config.getConfigurationSection("auto-start.days").getKeys(false)) {
            Calendar calendarDate = createDateFromDayName(day);
            calendarDate.setTimeZone(TimeZone.getTimeZone(Utils.getString("auto-start.timezone")));

            Calendar cloned = (Calendar) calendarDate.clone();

            if (nextMonth) {
                calendarDate.add(Calendar.MONTH, 1);
                calendarDate.set(Calendar.DAY_OF_MONTH, 1);
                findFirstNamedDay(calendarDate, cloned.get(Calendar.DAY_OF_WEEK));
            }

            String[] times = config.getString("auto-start.days." + day + ".times").split(", ");
            List<Calendar> calendarTimes = Lists.newArrayList();

            for (String time : times) {
                Calendar timeCalender = (Calendar) calendarDate.clone();
                LocalTime localTime = LocalTime.parse(time, formatter);
                timeCalender.set(Calendar.HOUR_OF_DAY, localTime.getHour());
                timeCalender.set(Calendar.MINUTE, localTime.getMinute());
                timeCalender.set(Calendar.SECOND, localTime.getSecond());

                calendarTimes.add(timeCalender);
            }

            AutoStartInfo info = new AutoStartInfo(calendarTimes);
            autoStartInfos.add(info);
        }

        if (nextMonth) {
            autoStartData.set("next", findClosestCalendar(true).getTimeInMillis());
        } else {
            autoStartData.set("next", findClosestCalendar(false).getTimeInMillis());
        }

        if (Utils.getBoolean("auto-start.enabled")) {
            autoStartTask = new AutoStartTask(plugin, this);
            autoStartTask.runTaskTimer(plugin, 0, nextMonth ? 100 : 20);
        }
    }

    public static void findFirstNamedDay(Calendar calendar, int lookingForDay) {
        while (calendar.get(Calendar.DAY_OF_WEEK) != lookingForDay) {
            calendar.add(Calendar.DAY_OF_MONTH, 1); // Move to the next day
        }

    }

    public Calendar findClosestCalendar(boolean nextMonth) {
        Calendar now = Calendar.getInstance();
        now.setTimeZone(TimeZone.getTimeZone(Utils.getString("auto-start.timezone")));

        if (nextMonth) {
            now.add(Calendar.MONTH, 1);
            now.set(Calendar.HOUR_OF_DAY, 0);
            now.set(Calendar.MINUTE, 0);
            now.set(Calendar.DAY_OF_MONTH, 1);
        }

        List<Calendar> allCalendars = Lists.newArrayList();
        for (AutoStartInfo info : autoStartInfos) {
            allCalendars.addAll(info.getTimes());
        }

        Calendar closestFutureCalendar = null;
        long closestDifference = Long.MAX_VALUE;

        for (Calendar calendar : allCalendars) {
            if (calendar.after(now)) {
                long difference = Math.abs(calendar.getTimeInMillis() - now.getTimeInMillis());
                if (difference < closestDifference) {
                    closestDifference = difference;
                    closestFutureCalendar = calendar;
                }
            }
        }

        return closestFutureCalendar;
    }

    public static Calendar createDateFromDayName(String dayName) {
        DateFormatSymbols symbols = new DateFormatSymbols(Locale.getDefault());
        String[] dayNames = symbols.getWeekdays();

        int dayIndex = -1;
        for (int i = 1; i < dayNames.length; i++) {
            if (dayNames[i].equalsIgnoreCase(dayName)) {
                dayIndex = i;
                break;
            }
        }

        if (dayIndex == -1) {
            throw new IllegalArgumentException("Invalid day name");
        }

        Calendar now = Calendar.getInstance();
        now.setTimeZone(TimeZone.getTimeZone(Utils.getString("auto-start.timezone")));

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone(Utils.getString("auto-start.timezone")));
        calendar.setTime(new Date());

        calendar.set(Calendar.DAY_OF_WEEK, dayIndex);
        if (calendar.get(Calendar.DAY_OF_YEAR) < now.get(Calendar.DAY_OF_YEAR)) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1);
        }

        return calendar;
    }

    public boolean isWithinNextMinutes(Calendar calendar, int minutes) {
        long estimatedTime = calendar.getTimeInMillis();
        long now = Calendar.getInstance().getTimeInMillis();
        long diff = TimeUnit.MINUTES.convert(estimatedTime - now, TimeUnit.MILLISECONDS);
        return diff + 1 == minutes;
    }

    public long getNext() {
        return autoStartData.getLong("next", -31);
    }

    public long getLast() {
        return autoStartData.getLong("last-run", 1);
    }

    public String getTimeRemainingToNext() {
        long nextDateTime = getNext();

        Instant instant = Instant.ofEpochMilli(nextDateTime);
        long seconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), LocalDateTime.ofInstant(instant, ZoneId.systemDefault()));

        long days = seconds / (24 * 3600);
        seconds = seconds % (24 * 3600);
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;

        String format = Utils.getString("remaining-time-format");
        return format
                .replace("%days%", String.valueOf(days))
                .replace("%hours%", String.valueOf(hours))
                .replace("%minutes%", String.valueOf(minutes))
                .replace("%seconds%", String.valueOf(seconds));
    }

    public List<String> getTimeInformations() {
        List<String> info = Lists.newArrayList();
        String format = Utils.getColoredString("time-information.format");

        for (AutoStartInfo autoStartInfo : autoStartInfos) {
            String str = format.replace("%day%", autoStartInfo.getDayName());
            str = str.replace("%times%", autoStartInfo.getTimesStr());

            info.add(str);
        }

        return info;
    }

    public List<AutoStartInfo> getAutoStartInfos() {
        return autoStartInfos;
    }

    public FileConfiguration getAutoStartData() {
        return autoStartData;
    }

}