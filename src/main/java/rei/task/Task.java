package rei.task;

/** Represents a task tracked by Rei. */
public class Task {
    private boolean isDone;
    private final String description;
    private final TaskType taskType;

    /** Creates an incomplete todo with the specified description. */
    public Task(String description) {
        this(description, TaskType.TODO);
    }

    /**
     * Creates a task with the specified type.
     *
     * @param description Task description.
     * @param taskType Category of the task.
     */
    protected Task(String description, TaskType taskType) {
        this.description = description;
        this.isDone = false;
        this.taskType = taskType;
    }

    public String getStatusIcon() {
        return (isDone ? "X" : " ");
    }

    public String getDescription() {
        return description;
    }

    public String getTaskType() {
        return taskType.getDisplayCode();
    }

    /** Returns whether this task has been completed. */
    public boolean isDone() {
        return isDone;
    }

    public void markAsDone() {
        isDone = true;
    }

    public void markAsUndone() {
        isDone = false;
    }

    @Override
    public String toString() {
        return description;
    }
}
