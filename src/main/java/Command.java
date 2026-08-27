/**
 * Represents a command recognised by Rei.
 */
public enum Command {
    TODO("todo"),
    DEADLINE("deadline"),
    EVENT("event"),
    LIST("list"),
    FIND("find"),
    DELETE("delete"),
    MARK("mark"),
    UNMARK("unmark"),
    BYE("bye");

    private final String keyword;

    Command(String keyword) {
        this.keyword = keyword;
    }

    /** Returns the text users enter to invoke this command. */
    public String getKeyword() {
        return keyword;
    }

    /**
     * Finds the command matching the supplied text.
     *
     * @param text command text entered by the user
     * @return the matching command, or {@code null} when the text is unknown
     */
    public static Command fromText(String text) {
        for (Command command : values()) {
            if (command.keyword.equals(text)) {
                return command;
            }
        }
        return null;
    }
}
