import java.io.IOException;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/** Runs Rei command-line interface. */
public class Rei {
    private static final String DIVIDER = "------------------------------------------------------------";
    private static final Path DATA_FILE = Path.of(System.getProperty(
            "rei.data.file", Path.of("data", "rei.txt").toString()));

    public static void main(String[] args) {
        printGreeting();
        Scanner scanner = new Scanner(System.in);
        Storage storage = new Storage(DATA_FILE);
        List<Task> tasks = loadTasks(storage);

        while (scanner.hasNextLine()) {
            String userInput = scanner.nextLine().trim();
            if (userInput.isEmpty()) {
                printError("Please enter a command. Try: todo read book");
                continue;
            }

            int firstSpace = userInput.indexOf(" ");
            String commandText = firstSpace == -1 ? userInput : userInput.substring(0, firstSpace);
            String details = firstSpace == -1 ? "" : userInput.substring(firstSpace).trim();
            Command command = Command.fromText(commandText);
            try {
                if (command == null) {
                    throw new ReiException("I'm sorry, I don't know what is '" + commandText
                            + "'. Try todo, deadline, event, list, delete, mark, unmark, or bye.");
                }
                if (command == Command.BYE) {
                    ensureNoDetails(command, details);
                    System.out.println("Bye! Hope to see you again soon!");
                    System.out.println(DIVIDER);
                    break;
                }
                if (command == Command.LIST) {
                    ensureNoDetails(command, details);
                    printTasks(tasks);
                    System.out.println(DIVIDER);
                    continue;
                }
                if (command == Command.DELETE) {
                    Task deletedTask = deleteTask(details, tasks);
                    saveTasks(storage, tasks);
                    System.out.println("Gotcha, I will remove this task from your list:");
                    System.out.println("  [" + deletedTask.getTaskType() + "]"
                            + "[" + deletedTask.getStatusIcon() + "] " + deletedTask);
                    System.out.println("Now you have " + tasks.size() + " tasks in the list.");
                    System.out.println(DIVIDER);
                    continue;
                }

                Task newTask = processCommand(command, details, tasks);
                if (newTask != null) {
                    tasks.add(newTask);
                    saveTasks(storage, tasks);
                    System.out.println("Okay, I've added: [" + newTask.getTaskType() + "]"
                            + "[" + newTask.getStatusIcon() + "] " + newTask);
                    System.out.println("You have a total of " + tasks.size() + " tasks in the list.");
                } else if (command == Command.MARK || command == Command.UNMARK) {
                    saveTasks(storage, tasks);
                }
            } catch (ReiException exception) {
                printError(exception.getMessage());
                continue;
            }
            System.out.println(DIVIDER);
        }
        scanner.close();
    }

    /** Loads saved tasks while keeping startup usable if the file cannot be read. */
    private static List<Task> loadTasks(Storage storage) {
        try {
            Storage.LoadResult result = storage.load();
            if (result.skippedLines() > 0) {
                System.out.println("Warning: I skipped " + result.skippedLines()
                        + " invalid line(s) in the data file.");
                System.out.println(DIVIDER);
            }
            return result.tasks();
        } catch (IOException exception) {
            System.out.println("Warning: I could not read the data file. Starting with an empty list.");
            System.out.println(DIVIDER);
            return new ArrayList<>();
        }
    }

    /** Saves tasks immediately after a successful list-changing command. */
    private static void saveTasks(Storage storage, List<Task> tasks) throws ReiException {
        try {
            storage.save(tasks);
        } catch (IOException exception) {
            throw new ReiException("I couldn't save your tasks. Please check that the data folder is writable.");
        }
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
    /** Process the command that is given by user. */
    private static Task processCommand(Command command, String details, List<Task> tasks)
            throws ReiException {
        return switch (command) {
        case TODO -> createTodo(details);
        case DEADLINE -> createDeadline(details);
        case EVENT -> createEvent(details);
        case MARK -> {
            updateTaskStatus(details, tasks, true);
            yield null;
        }
        case UNMARK -> {
            updateTaskStatus(details, tasks, false);
            yield null;
        }
        case LIST, DELETE, BYE -> throw new IllegalStateException("Command already handled: " + command);
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
        String[] deadlineParts = details.split("\\s+/by\\s+", -1);
        if (deadlineParts.length != 2 || deadlineParts[0].isEmpty() || deadlineParts[1].isEmpty()) {
            throw new ReiException("A deadline needs a description and a by date. "
                    + "\nTry: deadline {your task} /by {deadline}");
        }
        return new Deadlines(deadlineParts[0], deadlineParts[1]);
    }

    /** Creates an event task*/
    private static Task createEvent(String details) throws ReiException {
        String[] eventParts = details.split("\\s+/from\\s+", -1);
        if (eventParts.length != 2) {
            throw new ReiException("An event needs a description, start, and end time. "
                    + "\nTry: event {task} /from {start} /to {end}");
        }
        String[] timeParts = eventParts[1].split("\\s+/to\\s+", -1);
        if (eventParts[0].isEmpty() || timeParts.length != 2
                || timeParts[0].isEmpty() || timeParts[1].isEmpty()) {
            throw new ReiException("An event needs a description, start, and end time. "
                    + "\nTry: event {task} /from {start} /to {end}");
        }
        return new Events(eventParts[0], timeParts[0], timeParts[1]);
    }

    /** Updates a task's completion status*/
    private static void updateTaskStatus(String details, List<Task> tasks, boolean isDone)
            throws ReiException {
        Command command = isDone ? Command.MARK : Command.UNMARK;
        if (details.isEmpty()) {
            throw new ReiException("Please provide a task number! Try: " + command.getKeyword() + " {task no.}");
        }
        if (!details.matches("\\d+")) {
            throw new ReiException("The task number must be a whole number! Try: "
                    + command.getKeyword() + " {task no.}");
        }
        int taskIndex = getTaskIndex(details, tasks.size(), command.getKeyword());
        if (isDone) {
            tasks.get(taskIndex).markAsDone();
            System.out.println("Alright! I have set it to done! Good Work!\n");
        } else {
            tasks.get(taskIndex).markAsUndone();
            System.out.println("Got it! I have set it to not done!\n");
        }
        Task task = tasks.get(taskIndex);
        System.out.println("[" + task.getTaskType() + "]"
                + "[" + task.getStatusIcon() + "] " + task.getDescription());
    }

    /** Prints all stored tasks in their current list order. */
    private static void printTasks(List<Task> tasks) {
        if (tasks.isEmpty()) {
            System.out.println("Yay! You have completed all your tasks");
            return;
        }
        System.out.println("Here are the tasks in your list:");
        System.out.println("No. of tasks: " + tasks.size());
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            System.out.println((i + 1) + ".[" + task.getTaskType() + "]"
                    + "[" + task.getStatusIcon() + "] " + task);
        }
    }

    /** Removes the requested task and keeps the remaining task indexes contiguous. */
    private static Task deleteTask(String details, List<Task> tasks) throws ReiException {
        if (details.isEmpty()) {
            throw new ReiException("Please provide a task number. Try: delete 1");
        } else if (!details.matches("\\d+")) {
            throw new ReiException("The task number must be a whole number. Try: delete 1");
        }
        int taskIndex = getTaskIndex(details, tasks.size(), "delete");
        return tasks.remove(taskIndex);
    }

    /** Rejects a command that has unexpected extra text. */
    private static void ensureNoDetails(Command command, String details) throws ReiException {
        if (!details.isEmpty()) {
            throw new ReiException("The " + command.getKeyword()
                    + " command does not take any extra text. Try: " + command.getKeyword());
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
                throw new ReiException("Task " + taskNumber + " does not exist.\nLet's try that again!");
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
