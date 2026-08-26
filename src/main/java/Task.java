public class Task {
    protected boolean isDone;
    protected String description;
    protected TaskType taskType;

    public Task(String description) {
        this(description, TaskType.TODO);
    }

    /**
     * Creates a task with the specified type.
     *
     * @param description task description
     * @param taskType category of the task
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
        return this.description;
    }

    public String getTaskType() {
        return this.taskType.getDisplayCode();
    }

    /** Returns whether this task has been completed. */
    public boolean isDone() {
        return isDone;
    }

    public void markAsDone() {
        this.isDone = true;
    }

    public void markAsUndone() {
        this.isDone = false;
    }

    @Override
    public String toString(){
        return this.description;
    }
}
