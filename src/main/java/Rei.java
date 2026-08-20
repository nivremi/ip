import java.util.Scanner;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Runs Rei's command-line interface.
 */
public class Rei {
    private static final String DIVIDER = "------------------------------------------------------------";

    /**
     * Greets the user, echoes each entered command, and exits on {@code bye}.
     *
     * @param args command-line arguments, which are not used
     */
    public static void main(String[] args) {
        ZonedDateTime now = ZonedDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy, hh:mm a z", Locale.ENGLISH);
        String formattedDateTime = now.format(formatter);

        String banner = "\n██████╗ ███████╗██╗\n" +
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
        while (true) {
            String command = scanner.nextLine();
            System.out.println(DIVIDER);

            if (command.equals("bye")) {
                System.out.println("Bye. Hope to see you again soon!");
                System.out.println(DIVIDER);
                break;
            }

            System.out.println(" " + command);
            System.out.println(DIVIDER);
        }
    }
}
