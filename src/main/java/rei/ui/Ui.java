package rei.ui;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import rei.task.Task;

/** Handles all console input and output for Rei. */
public class Ui {
    private static final String DIVIDER = "------------------------------------------------------------";
    private static final DateTimeFormatter CURRENT_DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd MMMM yyyy, hh:mm a z", Locale.ENGLISH);
    private static final DateTimeFormatter TASK_DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMM dd yyyy", Locale.ENGLISH);

    private final Scanner scanner;

    /** Creates a UI that reads commands from standard input. */
    public Ui() {
        scanner = new Scanner(System.in);
    }

    /** Returns whether another command is available from the user. */
    public boolean hasNextCommand() {
        return scanner.hasNextLine();
    }

    /** Reads the next command and removes surrounding whitespace. */
    public String readCommand() {
        return scanner.nextLine().trim();
    }

    /** Displays Rei's welcome banner and greeting. */
    public void showGreeting() {
        String banner = "\n██████╗ ███████╗██╗\n"
                + "██╔══██╗██╔════╝██║\n"
                + "██████╔╝█████╗  ██║\n"
                + "██╔══██╗██╔══╝  ██║\n"
                + "██║  ██║███████╗██║\n"
                + "╚═╝  ╚═╝╚══════╝╚═╝";
        System.out.println(banner);
        divider();
        System.out.println(ZonedDateTime.now().format(CURRENT_DATE_FORMAT));
        divider();
        System.out.println("Hey there, my name is Rei!");
        System.out.println("How can I help you today?");
        divider();
    }

    /** Displays the divider between command responses. */
    public void divider() {
        System.out.println(DIVIDER);
    }

    /** Displays an error followed by a divider. */
    public void showError(String message) {
        System.out.println(message);
        divider();
    }

    /** Displays a warning about invalid saved task records. */
    public void showSkippedTasksWarning(int skippedLines) {
        System.out.println("Warning: I skipped " + skippedLines
                + " invalid line(s) in the data file.");
        divider();
    }

    /** Displays a warning when saved tasks cannot be loaded. */
    public void showLoadingError() {
        System.out.println("Warning: Oh man I could not read the data file. Gonna create an empty list.");
        divider();
    }

    /** Displays the farewell message. */
    public void showExit() {
        System.out.println("Bye! Hope to see you again soon!");
        divider();
    }

    /** Displays confirmation that a task was added. */
    public void showTaskAdded(Task task, int taskCount) {
        System.out.println("Okay, I've added: [" + task.getTaskType() + "]"
                + "[" + task.getStatusIcon() + "] " + task);
        System.out.println("You have a total of " + taskCount + " tasks in the list.");
    }

    /** Displays confirmation that a task was deleted. */
    public void showTaskDeleted(Task task, int taskCount) {
        System.out.println("Gotcha, I will remove this task from your list:");
        System.out.println("  [" + task.getTaskType() + "]"
                + "[" + task.getStatusIcon() + "] " + task);
        System.out.println("Now you have " + taskCount + " tasks in the list.");
    }

    /** Displays confirmation that a task's completion status changed. */
    public void showTaskStatusChanged(Task task, boolean isDone) {
        if (isDone) {
            System.out.println("Alright! I have set it to done! Good Work!\n");
        } else {
            System.out.println("Got it! I have set it to not done!\n");
        }
        System.out.println("[" + task.getTaskType() + "]"
                + "[" + task.getStatusIcon() + "] " + task.getDescription());
    }

    /** Displays all tasks in their current list order. */
    public void showTasks(List<Task> tasks) {
        if (tasks.isEmpty()) {
            System.out.println("Yay! You have completed all your tasks");
            return;
        }
        System.out.println("Here are the tasks in your list:");
        System.out.println("No. of tasks: " + tasks.size());
        for (int i = 0; i < tasks.size(); i++) {
            showNumberedTask(i + 1, tasks.get(i));
        }
    }

    /** Displays the heading for tasks occurring on a date. */
    public void showTasksOnDateHeading(LocalDate date) {
        System.out.println("Tasks occurring on " + date.format(TASK_DATE_FORMAT) + ":");
    }

    /** Displays one task with its position in the complete task list. */
    public void showNumberedTask(int taskNumber, Task task) {
        System.out.println(taskNumber + ".[" + task.getTaskType() + "]"
                + "[" + task.getStatusIcon() + "] " + task);
    }

    /** Displays that no deadline or event occurs on the requested date. */
    public void showNoTasksOnDate() {
        System.out.println("No deadlines or events occur on this date.");
    }

    /** Displays the heading for a keyword search result. */
    public void showMatchingTasksHeading() {
        System.out.println("Here are the matching tasks in your list:");
    }

    /** Displays that no task description contains the requested keyword. */
    public void showNoMatchingTasks() {
        System.out.println("No matching tasks found.");
    }

    /** Releases the input scanner when Rei exits. */
    public void close() {
        scanner.close();
    }
}
