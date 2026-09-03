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
        printLines(banner);
        divider();
        printLines(ZonedDateTime.now().format(CURRENT_DATE_FORMAT));
        divider();
        printLines(
                "Hey there, my name is Rei!",
                "How can I help you today?");
        divider();
    }

    /** Displays the divider between command responses. */
    public void divider() {
        printLines(DIVIDER);
    }

    /** Displays an error followed by a divider. */
    public void showError(String message) {
        printLines(message);
        divider();
    }

    /** Displays a warning about invalid saved task records. */
    public void showSkippedTasksWarning(int skippedLines) {
        printLines("Warning: I skipped " + skippedLines
                + " invalid line(s) in the data file.");
        divider();
    }

    /** Displays a warning when saved tasks cannot be loaded. */
    public void showLoadingError() {
        printLines("Warning: Oh man I could not read the data file. Gonna create an empty list.");
        divider();
    }

    /** Displays the farewell message. */
    public void showExit() {
        printLines("Bye! Hope to see you again soon!");
        divider();
    }

    /** Displays confirmation that a task was added. */
    public void showTaskAdded(Task task, int taskCount) {
        printLines(
                "Okay, I've added: [" + task.getTaskType() + "]"
                        + "[" + task.getStatusIcon() + "] " + task,
                "You have a total of " + taskCount + " tasks in the list.");
    }

    /** Displays confirmation that a task was deleted. */
    public void showTaskDeleted(Task task, int taskCount) {
        printLines(
                "Gotcha, I will remove this task from your list:",
                "  [" + task.getTaskType() + "]"
                        + "[" + task.getStatusIcon() + "] " + task,
                "Now you have " + taskCount + " tasks in the list.");
    }

    /** Displays confirmation that a task's completion status changed. */
    public void showTaskStatusChanged(Task task, boolean isDone) {
        String statusMessage = isDone
                ? "Alright! I have set it to done! Good Work!\n"
                : "Got it! I have set it to not done!\n";
        printLines(
                statusMessage,
                "[" + task.getTaskType() + "]"
                        + "[" + task.getStatusIcon() + "] " + task.getDescription());
    }

    /** Displays all tasks in their current list order. */
    public void showTasks(List<Task> tasks) {
        if (tasks.isEmpty()) {
            printLines("Yay! You have completed all your tasks");
            return;
        }
        printLines(
                "Here are the tasks in your list:",
                "No. of tasks: " + tasks.size());
        for (int i = 0; i < tasks.size(); i++) {
            showNumberedTask(i + 1, tasks.get(i));
        }
    }

    /** Displays the heading for tasks occurring on a date. */
    public void showTasksOnDateHeading(LocalDate date) {
        printLines("Tasks occurring on " + date.format(TASK_DATE_FORMAT) + ":");
    }

    /** Displays one task with its position in the complete task list. */
    public void showNumberedTask(int taskNumber, Task task) {
        printLines(taskNumber + ".[" + task.getTaskType() + "]"
                + "[" + task.getStatusIcon() + "] " + task);
    }

    /** Displays that no deadline or event occurs on the requested date. */
    public void showNoTasksOnDate() {
        printLines("No deadlines or events occur on this date.");
    }

    /** Displays the heading for a keyword search result. */
    public void showMatchingTasksHeading() {
        printLines("Here are the matching tasks in your list:");
    }

    /** Displays that no task description contains the requested keyword. */
    public void showNoMatchingTasks() {
        printLines("No matching tasks found.");
    }

    /** Prints each supplied line in order. */
    private static void printLines(String... lines) {
        for (String line : lines) {
            System.out.println(line);
        }
    }

    /** Releases the input scanner when Rei exits. */
    public void close() {
        scanner.close();
    }
}
