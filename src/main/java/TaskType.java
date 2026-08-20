/**
 * Identifies the kind of task stored by Rei and its display code.
 */
public enum TaskType {
    TODO("T"),
    DEADLINE("D"),
    EVENT("E");

    private final String displayCode;

    TaskType(String displayCode) {
        this.displayCode = displayCode;
    }

    /** Returns the short code used in the command-line task list. */
    public String getDisplayCode() {
        return displayCode;
    }
}
