package rei;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests the interface-independent command processing provided by Rei. */
public class ReiTest {
    @TempDir
    private Path testDirectory;

    @Test
    public void getResponse_addAndListTask_returnsExpectedMessages() {
        Rei rei = new Rei(testDirectory.resolve("tasks.txt"));

        Rei.CommandResult addResult = rei.getResponse("todo read book");
        Rei.CommandResult listResult = rei.getResponse("list");

        assertTrue(addResult.response().contains("Okay, I've added: [T][ ] read book"));
        assertTrue(listResult.response().contains("1.[T][ ] read book"));
        assertFalse(addResult.shouldExit());
    }

    @Test
    public void getResponse_byeCommand_requestsExit() {
        Rei rei = new Rei(testDirectory.resolve("tasks.txt"));

        Rei.CommandResult result = rei.getResponse("bye");

        assertTrue(result.response().contains("Bye! Hope to see you again soon!"));
        assertTrue(result.shouldExit());
    }

    @Test
    public void getResponse_findKeyword_preservesOriginalIndexesAndOrder() {
        Rei rei = new Rei(testDirectory.resolve("tasks.txt"));
        rei.getResponse("todo unrelated");
        rei.getResponse("todo Read book");
        rei.getResponse("todo buy milk");
        rei.getResponse("todo book tickets");

        String matches = rei.getResponse("find BOOK").response();

        assertTrue(matches.contains("2.[T][ ] Read book"));
        assertTrue(matches.contains("4.[T][ ] book tickets"));
        assertTrue(matches.indexOf("Read book") < matches.indexOf("book tickets"));
        assertFalse(matches.contains("unrelated"));
        assertFalse(matches.contains("buy milk"));
    }

    @Test
    public void getResponse_findMissingKeyword_reportsNoMatches() {
        Rei rei = new Rei(testDirectory.resolve("tasks.txt"));
        String emptyResult = rei.getResponse("find book").response();
        rei.getResponse("todo buy milk");

        assertTrue(emptyResult.contains("No matching tasks"));
        assertTrue(rei.getResponse("find book").response().contains("No matching tasks"));
    }

    @Test
    public void getResponse_invalidCommand_preservesExistingTasks() {
        Rei rei = new Rei(testDirectory.resolve("tasks.txt"));
        rei.getResponse("todo alpha");

        Rei.CommandResult invalidResult = rei.getResponse("delete first");
        Rei.CommandResult listResult = rei.getResponse("list");

        assertTrue(invalidResult.response().contains("task number must be a whole number"));
        assertTrue(listResult.response().contains("1.[T][ ] alpha"));
    }
}
