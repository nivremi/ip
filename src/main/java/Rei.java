import java.util.Scanner;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Runs Rei command-line interface.
 */
public class Rei {
    private static final String DIVIDER = "------------------------------------------------------------";

    public static void main(String[] args) {
        ZonedDateTime now = ZonedDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, hh:mm a z", Locale.ENGLISH);
        String formattedDateTime = now.format(formatter);

        String banner = "\n" +
                "██████╗ ███████╗██╗\n" +
                "██╔══██╗██╔════╝██║\n" +
                "██████╔╝█████╗  ██║\n" +
                "██╔══██╗██╔══╝  ██║\n" +
                "██║  ██║███████╗██║\n" +
                "╚═╝  ╚═╝╚══════╝╚═╝";
        System.out.println(banner);
        System.out.println(DIVIDER);
        System.out.println(formattedDateTime);
        System.out.println(DIVIDER);
        System.out.println("Hey there, my name is Rei!");
        System.out.println("How can I help you today?");
        System.out.println(DIVIDER);

        Scanner scanner = new Scanner(System.in);
        Task[] tasks = new Task[100];
        int taskCount = 0;

        while (true) {
            String userInput = scanner.nextLine().trim();
            if (userInput.isEmpty()) {
                continue;
            }

            int firstSpace = userInput.indexOf(" ");
            String command = (firstSpace == -1) ? userInput : userInput.substring(0, firstSpace);
            String details = (firstSpace == -1) ? "" : userInput.substring(firstSpace).trim();

            if (command.equals("bye")) {
                System.out.println("Bye! Hope to see you again soon!");
                System.out.println(DIVIDER);
                break;
            }

            if (command.equals("list")) {
                if (taskCount == 0) {
                    System.out.println("Yay! You have completed all your tasks");
                } else {
                    for (int i = 0; i < taskCount; i++) {
                        Task currTask = tasks[i];
                        System.out.println("[" + currTask.getTaskType() + "]" +
                                "[" + currTask.getStatusIcon() + "] " + currTask.toString());
                    }
                }
                System.out.println(DIVIDER);
                continue;
            }

            Task newTask = null;

            switch (command) {
                case "mark" -> {
                    if (details.isEmpty()) {
                        System.out.println("Error: Please specify a task number.");
                        break;
                    }
                    int taskNo = Integer.parseInt(details) - 1;
                    if (taskNo >= 0 && taskNo < taskCount) {
                        tasks[taskNo].markAsDone();
                        System.out.println("Alright! I have set it to done!:\n");
                        System.out.println("[" + tasks[taskNo].getTaskType() + "]"
                                + "[" + tasks[taskNo].getStatusIcon() + "] "
                                + tasks[taskNo].getDescription());
                    } else {
                        System.out.println("Error: Task index out of bounds.");
                    }
                }
                case "unmark" -> {
                    if (details.isEmpty()) {
                        System.out.println("Error: Please specify a task number.");
                        break;
                    }
                    int taskNo = Integer.parseInt(details) - 1;
                    if (taskNo >= 0 && taskNo < taskCount) {
                        tasks[taskNo].markAsUndone();
                        System.out.println("Got it! I have set it to not done!:\n");
                        System.out.println("[" + tasks[taskNo].getTaskType() + "]"
                                + "[" + tasks[taskNo].getStatusIcon() + "] "
                                + tasks[taskNo].getDescription());
                    } else {
                        System.out.println("Error: Task index out of bounds.");
                    }
                }
                case "todo" -> {
                    if (details.isEmpty()) {
                        System.out.println("Error: The description of a todo cannot be empty.");
                        break;
                    }
                    newTask = new Task(details);
                }
                case "deadline" -> {
                    int byIndex = details.indexOf("/by");
                    if (byIndex == -1 || details.substring(0, byIndex).trim().isEmpty()) {
                        System.out.println("Error: Invalid deadline format. Use: deadline [desc] /by [date]");
                        break;
                    }
                    String taskDescription = details.substring(0, byIndex).trim();
                    String byDate = details.substring(byIndex + 3).trim();
                    newTask = new Deadlines(taskDescription, byDate);
                }
                case "event" -> {
                    int fromIndex = details.indexOf("/from");
                    int toIndex = details.indexOf("/to");
                    if (fromIndex == -1 || toIndex == -1 || fromIndex > toIndex) {
                        System.out.println("Error: Invalid event format. Use: event [desc] /from [start] /to [end]");
                        break;
                    }
                    String taskDescription = details.substring(0, fromIndex).trim();
                    String startDate = details.substring(fromIndex + 5, toIndex).trim();
                    String endDate = details.substring(toIndex + 3).trim();
                    newTask = new Events(taskDescription, startDate, endDate);
                }
                default -> System.out.println("Error: Unknown command.");
            }

            if (newTask != null) {
                if (taskCount < tasks.length) {
                    tasks[taskCount] = newTask;
                    taskCount++;
                    System.out.println("Okay, I've added: " + "[" + newTask.getTaskType() + "]" +
                            "[" + newTask.getStatusIcon() + "] " + newTask.toString());
                    System.out.println("You have a total of " + taskCount + " tasks in the list.");
                } else {
                    System.out.println("Error: Task list is full!");
                }
            }
            System.out.println(DIVIDER);
        }
        scanner.close();
    }
}
