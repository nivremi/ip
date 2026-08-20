public class Task {
    protected boolean isDone;
    protected String description;
    protected String taskType;

    public Task(String description) {
        this.description = description;
        this.isDone = false;
        this.taskType = "T";
    }

    public String getStatusIcon() {
        return (isDone ? "X" : " ");
    }
    public String getDescription() {
        return this.description;
    }

    public String getTaskType() {
        return this.taskType;
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