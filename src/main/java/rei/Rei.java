package rei;

import rei.command.Command;
import rei.exception.ReiException;
import rei.storage.Storage;
import rei.task.Deadlines;
import rei.task.Events;
import rei.task.Task;
import rei.ui.Ui;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Coordinates Rei command processing, task management, storage, and UI. */
public class Rei {
    private static final Path DATA_FILE = Path.of(System.getProperty(
            "rei.data.file", Path.of("data", "rei.txt").toString()));
    private static final List<DateTimeFormatter> INPUT_DATE_FORMATS = List.of(
            DateTimeFormatter.ofPattern("uuuu-MM-dd HHmm", Locale.ENGLISH)
                    .withResolverStyle(ResolverStyle.STRICT),
            DateTimeFormatter.ofPattern("uuuu/MM/dd HHmm", Locale.ENGLISH)
                    .withResolverStyle(ResolverStyle.STRICT),
            DateTimeFormatter.ofPattern("d/M/uuuu HHmm", Locale.ENGLISH)
                    .withResolverStyle(ResolverStyle.STRICT),
            DateTimeFormatter.ofPattern("d-M-uuuu HHmm", Locale.ENGLISH)
                    .withResolverStyle(ResolverStyle.STRICT));
    private static final List<DateTimeFormatter> FIND_DATE_FORMATS = List.of(
            DateTimeFormatter.ofPattern("uuuu-MM-dd", Locale.ENGLISH)
                    .withResolverStyle(ResolverStyle.STRICT),
            DateTimeFormatter.ofPattern("uuuu/MM/dd", Locale.ENGLISH)
                    .withResolverStyle(ResolverStyle.STRICT),
            DateTimeFormatter.ofPattern("d/M/uuuu", Locale.ENGLISH)
                    .withResolverStyle(ResolverStyle.STRICT),
            DateTimeFormatter.ofPattern("d-M-uuuu", Locale.ENGLISH)
                    .withResolverStyle(ResolverStyle.STRICT));

    /**
     * Starts Rei's command-line interface and processes commands until the user exits.
     *
     * @param args Command-line arguments, which are not used.
     */
    public static void main(String[] args) {
        Ui ui = new Ui();
        ui.showGreeting();
        Storage storage = new Storage(DATA_FILE);
        List<Task> tasks = loadTasks(storage, ui);

        while (ui.hasNextCommand()) {
            String userInput = ui.readCommand();
            if (userInput.isEmpty()) {
                ui.showError("Please enter a command. Try: todo read book");
                continue;
            }

            int firstSpace = userInput.indexOf(" ");
            String commandText = firstSpace == -1
                    ? userInput
                    : userInput.substring(0, firstSpace);
            String details = firstSpace == -1
                    ? ""
                    : userInput.substring(firstSpace).trim();
            Command command = Command.fromText(commandText);
            try {
                if (command == null) {
                    throw new ReiException("I'm sorry, I don't know what is '" + commandText
                            + "'. Try todo, deadline, event, list, find, delete, mark, unmark, or bye.");
                }
                if (command == Command.BYE) {
                    ensureNoDetails(command, details);
                    ui.showExit();
                    break;
                }
                if (command == Command.LIST) {
                    ensureNoDetails(command, details);
                    ui.showTasks(tasks);
                    ui.divider();
                    continue;
                }
                if (command == Command.FIND) {
                    printTasksOnDate(details, tasks, ui);
                    ui.divider();
                    continue;
                }
                if (command == Command.DELETE) {
                    Task deletedTask = deleteTask(details, tasks);
                    saveTasks(storage, tasks);
                    ui.showTaskDeleted(deletedTask, tasks.size());
                    ui.divider();
                    continue;
                }

                Task newTask = processCommand(command, details, tasks, ui);
                if (newTask != null) {
                    tasks.add(newTask);
                    saveTasks(storage, tasks);
                    ui.showTaskAdded(newTask, tasks.size());
                } else if (command == Command.MARK || command == Command.UNMARK) {
                    saveTasks(storage, tasks);
                }
            } catch (ReiException exception) {
                ui.showError(exception.getMessage());
                continue;
            }
            ui.divider();
        }
        ui.close();
    }

    /** Loads saved tasks while keeping startup usable if the file cannot be read. */
    private static List<Task> loadTasks(Storage storage, Ui ui) {
        try {
            Storage.LoadResult result = storage.load();
            if (result.skippedLines() > 0) {
                ui.showSkippedTasksWarning(result.skippedLines());
            }
            return result.tasks();
        } catch (IOException exception) {
            ui.showLoadingError();
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

    /** Processes a task-creating or task-status command entered by the user. */
    private static Task processCommand(Command command, String details, List<Task> tasks, Ui ui)
            throws ReiException {
        return switch (command) {
        case TODO -> createTodo(details);
        case DEADLINE -> createDeadline(details);
        case EVENT -> createEvent(details);
        case MARK -> {
            updateTaskStatus(details, tasks, true, ui);
            yield null;
        }
        case UNMARK -> {
            updateTaskStatus(details, tasks, false, ui);
            yield null;
        }
        case LIST, FIND, DELETE, BYE -> throw new IllegalStateException("Command already handled: " + command);
        };
    }

    /** Creates a todo task after validating that a description was provided. */
    private static Task createTodo(String details) throws ReiException {
        if (details.isEmpty()) {
            throw new ReiException("A todo needs a description. \nTry: todo {your task}");
        }
        return new Task(details);
    }

    /** Creates a deadline task from its description and typed due date. */
    private static Task createDeadline(String details) throws ReiException {
        String[] deadlineParts = details.split("\\s+/by\\s+", -1);
        if (deadlineParts.length != 2 || deadlineParts[0].isEmpty() || deadlineParts[1].isEmpty()) {
            throw new ReiException("A deadline needs a description and a by date. "
                    + "\nTry: deadline {your task} /by {deadline}");
        }
        return new Deadlines(deadlineParts[0], parseDateTime(deadlineParts[1]));
    }

    /** Creates an event task after validating its description and time range. */
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
        LocalDateTime start = parseDateTime(timeParts[0]);
        LocalDateTime end = parseDateTime(timeParts[1]);
        if (end.isBefore(start)) {
            throw new ReiException("An event's end time cannot be before its start time.");
        }
        return new Events(eventParts[0], start, end);
    }

    /** Converts a supported user-entered date and time into a typed value. */
    private static LocalDateTime parseDateTime(String text) throws ReiException {
        for (DateTimeFormatter formatter : INPUT_DATE_FORMATS) {
            try {
                return LocalDateTime.parse(text, formatter);
            } catch (DateTimeParseException ignored) {
                // Try the next supported format.
            }
        }
        throw new ReiException("Please use a valid date and time in yyyy-MM-dd, yyyy/MM/dd, "
                + "dd/MM/yyyy, or dd-MM-yyyy format, followed by a 24-hour time such as 1800.");
    }

    /** Updates a selected task's completion status and displays the result. */
    private static void updateTaskStatus(String details, List<Task> tasks, boolean isDone, Ui ui)
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
        Task task = tasks.get(taskIndex);
        if (isDone) {
            task.markAsDone();
        } else {
            task.markAsUndone();
        }
        ui.showTaskStatusChanged(task, isDone);
    }

    /** Prints deadlines and events that occur on a user-specified calendar date. */
    private static void printTasksOnDate(String details, List<Task> tasks, Ui ui) throws ReiException {
        LocalDate date = getDate(details);

        boolean foundTask = false;
        ui.showTasksOnDateHeading(date);
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            if (occursOn(task, date)) {
                ui.showNumberedTask(i + 1, task);
                foundTask = true;
            }
        }
        if (!foundTask) {
            ui.showNoTasksOnDate();
        }
    }

    /** Converts a supported user-entered date into a typed calendar date. */
    private static LocalDate getDate(String details) throws ReiException {
        LocalDate date = null;
        for (DateTimeFormatter formatter : FIND_DATE_FORMATS) {
            try {
                date = LocalDate.parse(details, formatter);
                break;
            } catch (DateTimeParseException ignored) {
                // Try the next supported format.
            }
        }
        if (date == null) {
            throw new ReiException("Please provide a valid date in yyyy-MM-dd, yyyy/MM/dd, "
                    + "dd/MM/yyyy, or dd-MM-yyyy format. Try: find 2019-12-02");
        }
        return date;
    }

    /** Returns whether a scheduled task falls on the supplied date. */
    private static boolean occursOn(Task task, LocalDate date) {
        if (task instanceof Deadlines deadline) {
            return deadline.getBy().toLocalDate().equals(date);
        }
        if (task instanceof Events event) {
            LocalDate startDate = event.getStart().toLocalDate();
            LocalDate endDate = event.getEnd().toLocalDate();
            return !date.isBefore(startDate) && !date.isAfter(endDate);
        }
        return false;
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
}
