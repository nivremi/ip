import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Deadlines extends Task {
    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("MMM dd yyyy, h:mm a", Locale.ENGLISH);
    private LocalDateTime by;

    public Deadlines(String description, LocalDateTime by) {
        super(description, TaskType.DEADLINE);
        this.by = by;
    }

    public LocalDateTime getBy() {
        return this.by;
    }

    public void setBy(LocalDateTime by) {
        this.by = by;
    }

    @Override
    public String toString() {
        return description + " (by: " + by.format(DISPLAY_FORMAT) + ")";
    }
}
