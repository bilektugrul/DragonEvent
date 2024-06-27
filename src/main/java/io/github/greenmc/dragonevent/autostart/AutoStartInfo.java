package io.github.greenmc.dragonevent.autostart;

import com.google.common.collect.Lists;
import io.github.greenmc.dragonevent.util.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class AutoStartInfo {

    private static final SimpleDateFormat formatter = new SimpleDateFormat("hh:mma");

    private final List<Calendar> times;

    public AutoStartInfo(List<Calendar> times) {
        this.times = times;
    }

    public List<Calendar> getTimes() {
        return times;
    }

    public String getDayName() {
        return Utils.getDayName(times.get(0));
    }

    public String getTimesStr() {
        List<String> times = Lists.newArrayList();

        for (Calendar time : this.times) {
            times.add(formatter.format(time.getTime()));
        }

        return Utils.listToStringComma(times);
    }

    public boolean isSameDay(Calendar element, Calendar other) {
        return element.get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR);
    }

    public boolean isCurrentDay(Calendar element) {
        return isSameDay(element, Calendar.getInstance());
    }

    public boolean isCurrentTime(Calendar element) {
        Calendar now = Calendar.getInstance();
        now.setTimeZone(TimeZone.getTimeZone(Utils.getString("auto-start.timezone")));

        return isCurrentDay(element) && element.get(Calendar.HOUR_OF_DAY) == now.get(Calendar.HOUR_OF_DAY) && element.get(Calendar.MINUTE) == now.get(Calendar.MINUTE);
    }

}