import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class Events extends Task {
    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("MMM dd yyyy, h:mm a", Locale.ENGLISH);
    private LocalDateTime start;
    private LocalDateTime end;

    public Events(String description, LocalDateTime start, LocalDateTime end) {
        super(description, TaskType.EVENT);
        this.start = start;
        this.end = end;
    }

    public LocalDateTime getStart() {
        return this.start;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public LocalDateTime getEnd() {
        return this.end;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    @Override
    public String toString(){
        return description + " (from: " + start.format(DISPLAY_FORMAT)
                + " to: " + end.format(DISPLAY_FORMAT) + ")";
    }
}
