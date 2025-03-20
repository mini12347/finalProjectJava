package Entities;
import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.Map;

public class Disponibility {
    private Map<DayOfWeek, Hours> daysOfWeek;

    public Disponibility(Map<DayOfWeek, Hours> daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
        if (daysOfWeek == null) {
            this.daysOfWeek = new HashMap<>();
        } else {
            this.daysOfWeek = daysOfWeek;
        }
    }

    public Map<DayOfWeek, Hours> getDaysOfWeek() {
        return daysOfWeek;
    }

    public void setDaysOfWeek(Map<DayOfWeek, Hours> daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<DayOfWeek, Hours> entry : daysOfWeek.entrySet()) {
            DayOfWeek day = entry.getKey();
            Hours hours = entry.getValue();
            sb.append(day.name())
                    .append(":")
                    .append(hours.getStarthour())
                    .append("-")
                    .append(hours.getEndhour())
                    .append(";");
        }

        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }

        return sb.toString();
    }
}