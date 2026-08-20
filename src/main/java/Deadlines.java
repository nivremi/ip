public class Deadlines extends Task {
    public String by;

    public Deadlines(String description, String by) {
        super(description);
        this.by = by;
        this.taskType = "D";
    }

    public String getBy() {
        return this.by;
    }

    public void setBy(String by) {
        this.by = by;
    }

    @Override
    public String toString() {
        return description + " (by: " + by + ")";
    }
}