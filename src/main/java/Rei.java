import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Scanner;

public class Rei {
    private static final String DIVIDER = "------------------------------------------------------------";

    public static void main(String[] args) {
        printGreeting();
        Scanner scanner = new Scanner(System.in);
        Task[] tasks = new Task[100];
        int taskCount = 0;

        while (scanner.hasNextLine()) {
            String userInput = scanner.nextLine().trim();
            if (userInput.isEmpty()) {
                printError("Please enter a command. Try: todo read book");
                continue;
            }

            int firstSpace = userInput.indexOf(" ");
            String command = firstSpace == -1 ? userInput : userInput.substring(0, firstSpace);
            String details = firstSpace == -1 ? "" : userInput.substring(firstSpace).trim();
            try {
                if (command.equals("bye")) {
                    ensureNoDetails(command, details);
                    System.out.println("Bye! Hope to see you again soon!");
                    System.out.println(DIVIDER);
                    break;
                }
                if (command.equals("list")) {
                    ensureNoDetails(command, details);
                    printTasks(tasks, taskCount);
                    System.out.println(DIVIDER);
                    continue;
                }

                Task newTask = processCommand(command, details, tasks, taskCount);
                if (newTask != null) {
                    if (taskCount == tasks.length) {
                        throw new ReiException("Your task list is full. Remove a task before adding another one.");
                    }
                    tasks[taskCount++] = newTask;
                    System.out.println("Okay, I've added: [" + newTask.getTaskType() + "]"
                            + "[" + newTask.getStatusIcon() + "] " + newTask);
                    System.out.println("You have a total of " + taskCount + " tasks in the list.");
                }
            } catch (ReiException exception) {
                printError(exception.getMessage());
                continue;
            }
            System.out.println(DIVIDER);
        }
        scanner.close();
    }

    /** Prints welcome message. */
    private static void printGreeting() {
        String banner = "" +
                "\n██████╗ ███████╗██╗\n"
                + "██╔══██╗██╔════╝██║\n"
                + "██████╔╝█████╗  ██║\n"
                + "██╔══██╗██╔══╝  ██║\n"
                + "██║  ██║███████╗██║\n"
                + "╚═╝  ╚═╝╚══════╝╚═╝";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, hh:mm a z", Locale.ENGLISH);
        System.out.println(banner);
        System.out.println(DIVIDER);
        System.out.println(ZonedDateTime.now().format(formatter));
        System.out.println(DIVIDER);
        System.out.println("Hey there, my name is Rei!");
        System.out.println("How can I help you today?");
        System.out.println(DIVIDER);
    }

    private static Task processCommand(String command, String details, Task[] tasks, int taskCount)
            throws ReiException {
        return switch (command) {
        case "todo" -> createTodo(details);
        case "deadline" -> createDeadline(details);
        case "event" -> createEvent(details);
        case "mark" -> {
            updateTaskStatus(details, tasks, taskCount, true);
            yield null;
        }
        case "unmark" -> {
            updateTaskStatus(details, tasks, taskCount, false);
            yield null;
        }
        default -> throw new ReiException("I'm sorry, I don't know what is '" + command
                + "'. Try todo, deadline, event, list, mark, unmark, or bye.");
        };
    }

    /** Creates a todo task.*/
    private static Task createTodo(String details) throws ReiException {
        if (details.isEmpty()) {
            throw new ReiException("A todo needs a description. \nTry: todo {your task}");
        }
        return new Task(details);
    }

    /** Creates a deadline task */
    private static Task createDeadline(String details) throws ReiException {
        int byIndex = details.indexOf("/by");
        if (byIndex <= 0 || details.substring(0, byIndex).trim().isEmpty()
                || details.substring(byIndex + 3).trim().isEmpty()) {
            throw new ReiException("A deadline needs a description and a by date. "
                    + "\nTry: deadline {your task} /by {deadline}");
        }
        return new Deadlines(details.substring(0, byIndex).trim(), details.substring(byIndex + 3).trim());
    }

    /** Creates an event task*/
    private static Task createEvent(String details) throws ReiException {
        int fromIndex = details.indexOf("/from");
        int toIndex = details.indexOf("/to");
        if (fromIndex <= 0 || toIndex <= fromIndex
                || details.substring(0, fromIndex).trim().isEmpty()
                || details.substring(fromIndex + 5, toIndex).trim().isEmpty()
                || details.substring(toIndex + 3).trim().isEmpty()) {
            throw new ReiException("An event needs a description, start, and end time. "
                    + "\nTry: event {task} /from {start} /to {end}");
        }
        return new Events(details.substring(0, fromIndex).trim(),
                details.substring(fromIndex + 5, toIndex).trim(), details.substring(toIndex + 3).trim());
    }

    /** Updates a task's completion status*/
    private static void updateTaskStatus(String details, Task[] tasks, int taskCount, boolean isDone)
            throws ReiException {
        String command = isDone ? "mark" : "unmark";
        if (details.isEmpty()) {
            throw new ReiException("Please provide a task number! Try: " + command + " {task no.}read");
        }
        if (!details.matches("\\d+")) {
            throw new ReiException("The task number must be a whole number! Try: " + command + " {task no.}");
        }
        int taskIndex = getTaskIndex(details, taskCount, command);
        if (isDone) {
            tasks[taskIndex].markAsDone();
            System.out.println("Alright! I have set it to done! Good Work!\n");
        } else {
            tasks[taskIndex].markAsUndone();
            System.out.println("Got it! I have set it to not done!\n");
        }
        System.out.println("[" + tasks[taskIndex].getTaskType() + "]"
                + "[" + tasks[taskIndex].getStatusIcon() + "] " + tasks[taskIndex].getDescription());
    }

    /** Prints all stored tasks*/
    private static void printTasks(Task[] tasks, int taskCount) {
        if (taskCount == 0) {
            System.out.println("Yay! You have completed all your tasks");
            return;
        }
        for (int i = 0; i < taskCount; i++) {
            Task task = tasks[i];
            System.out.println("[" + task.getTaskType() + "]"
                    + "[" + task.getStatusIcon() + "] " + task);
        }
    }

    /** Rejects a command that has unexpected extra text. */
    private static void ensureNoDetails(String command, String details) throws ReiException {
        if (!details.isEmpty()) {
            throw new ReiException("The " + command + " command does not take any extra text. Try: " + command);
        }
    }

    /** Converts a valid task number to an array index. */
    private static int getTaskIndex(String details, int taskCount, String command) throws ReiException {
        try {
            int taskNumber = Integer.parseInt(details);
            if (taskNumber < 1) {
                throw new ReiException("The task number must be at least 1. Try: " + command + " 1\n" +
                        "Let's try that again!");
            }
            if (taskNumber > taskCount) {
                throw new ReiException("Task " + taskNumber + " does not exist. Try a number from 1 to "
                        + taskCount + ".\nLet's try that again!");
            }
            return taskNumber - 1;
        } catch (NumberFormatException exception) {
            throw new ReiException("Try a number from 1 to " + taskCount + "!");
        }
    }

    /** Prints one error message */
    private static void printError(String message) {
        System.out.println(message);
        System.out.println(DIVIDER);
    }
}
