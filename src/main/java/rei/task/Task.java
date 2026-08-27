package rei.task;

/**
 * Represents a task with a description, completion status, and task type.
 */
public class Task {
    private boolean isDone;
    private final String description;
    private final TaskType taskType;

    /**
     * Creates an incomplete todo task with the specified description.
     *
     * @param description Description of the task.
     */
    public Task(String description) {
        this(description, TaskType.TODO);
    }

    /**
     * Creates a task with the specified type.
     *
     * @param description Description of the task.
     * @param taskType Category of the task.
     */
    protected Task(String description, TaskType taskType) {
        this.description = description;
        this.isDone = false;
        this.taskType = taskType;
    }

    /**
     * Returns the icon representing the task's completion status.
     *
     * @return {@code X} when completed, or a space when incomplete.
     */
    public String getStatusIcon() {
        return (isDone ? "X" : " ");
    }
    /**
     * Returns the task description.
     *
     * @return Description entered by the user.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the short display code for this task's type.
     *
     * @return Task type display code.
     */
    public String getTaskType() {
        return taskType.getDisplayCode();
    }

    /** Returns whether this task has been completed. */
    public boolean isDone() {
        return isDone;
    }

    /** Marks this task as completed. */
    public void markAsDone() {
        isDone = true;
    }

    /** Marks this task as incomplete. */
    public void markAsUndone() {
        isDone = false;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return description;
    }
}
