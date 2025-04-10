package Entities;
import java.time.DayOfWeek;
import java.util.HashMap;
import java.util.Map;

public class Disponibility {
    private int id; // Added ID field to uniquely identify each availability record
    private Map<DayOfWeek, Hours> daysOfWeek;

    public Disponibility(Map<DayOfWeek, Hours> daysOfWeek) {
        this.daysOfWeek = daysOfWeek != null ? daysOfWeek : new HashMap<>();
    }

    public Disponibility(int id, Map<DayOfWeek, Hours> daysOfWeek) {
        this.id = id;
        this.daysOfWeek = daysOfWeek != null ? daysOfWeek : new HashMap<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Map<DayOfWeek, Hours> getDaysOfWeek() {
        return daysOfWeek;
    }

    public void setDaysOfWeek(Map<DayOfWeek, Hours> daysOfWeek) {
        this.daysOfWeek = daysOfWeek;
    }

    // This method is important for ComboBox display
    @Override
    public String toString() {
        if (daysOfWeek == null || daysOfWeek.isEmpty()) {
            return "Aucune disponibilité";
        }

        // Take the first entry to represent the availability in the combo box
        Map.Entry<DayOfWeek, Hours> entry = daysOfWeek.entrySet().iterator().next();
        DayOfWeek day = entry.getKey();
        Hours hours = entry.getValue();

        String dayName = translateDayOfWeek(day);
        return dayName + " " + hours.getStarthour() + "h-" + hours.getEndhour() + "h" +
                (daysOfWeek.size() > 1 ? " + " + (daysOfWeek.size() - 1) + " jour(s)" : "");
    }

    // Helper method to translate DayOfWeek to French
    private String translateDayOfWeek(DayOfWeek day) {
        switch (day) {
            case MONDAY: return "Lundi";
            case TUESDAY: return "Mardi";
            case WEDNESDAY: return "Mercredi";
            case THURSDAY: return "Jeudi";
            case FRIDAY: return "Vendredi";
            case SATURDAY: return "Samedi";
            case SUNDAY: return "Dimanche";
            default: return day.toString();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Disponibility that = (Disponibility) obj;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}