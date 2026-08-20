import java.util.Scanner;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Runs Rei command-line interface.
 */
public class Rei {
    private static final String DIVIDER = "------------------------------------------------------------";

    /**
     * Greets the user, stores tasks in memory, and exits on {@code bye}.
     *
     * @param args command-line arguments, which are not used
     */

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
            String command = scanner.nextLine();
            String keyword = command.split(" ")[0];
            System.out.println(DIVIDER);

            if (command.equals("bye")) {
                System.out.println("Bye. Hope to see you again soon!");
                System.out.println(DIVIDER);
                break;
            }

            if (command.equals("list")) {
                for (int i = 0; i < taskCount; i++) {
                    Task currTask = tasks[i];
                    System.out.println("[" + currTask.getStatusIcon() + "]" + currTask.getDescription());
                }
            }
            else if (keyword.equals("mark")) {
                int taskNo = Integer.parseInt(command.split(" ")[1]) - 1;
                tasks[taskNo].markAsDone();
                System.out.println("Alright! I have set it to done!:\n");
                System.out.println("[" + tasks[taskNo].getStatusIcon() + "]" + tasks[taskNo].getDescription());
            }
            else if (keyword.equals("unmark")) {
                int taskNo = Integer.parseInt(command.split(" ")[1]) - 1;
                tasks[taskNo].markAsUndone();
                System.out.println("Got it! I have set it to not done!:\n");
                System.out.println("[" + tasks[taskNo].getStatusIcon() + "]" + tasks[taskNo].getDescription());
            }
            else {
                Task newTask = new Task(command);
                tasks[taskCount] = newTask;
                taskCount++;
                System.out.println(" added: " + command);
            }
            System.out.println(DIVIDER);
        }
    }
}
