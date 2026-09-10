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
    public void getResponse_markUnmarkAndDeleteBoundaryTasks_updatesCorrectTasks() {
        Rei rei = new Rei(testDirectory.resolve("tasks.txt"));
        rei.getResponse("  todo alpha  ");
        rei.getResponse("todo beta");

        rei.getResponse("mark 1");
        rei.getResponse("mark 2");
        rei.getResponse("unmark 1");
        String tasks = rei.getResponse("list").response();
        assertTrue(tasks.contains("1.[T][ ] alpha"));
        assertTrue(tasks.contains("2.[T][X] beta"));

        rei.getResponse("delete 2");
        assertFalse(rei.getResponse("list").response().contains("beta"));
        rei.getResponse("delete 1");
        assertFalse(rei.getResponse("list").response().contains("alpha"));
    }

    @Test
    public void getResponse_outOfBoundsTaskNumbers_returnsErrors() {
        Rei rei = new Rei(testDirectory.resolve("tasks.txt"));
        rei.getResponse("todo alpha");

        assertTrue(rei.getResponse("mark 0").response().contains("at least 1"));
        assertTrue(rei.getResponse("unmark 2").response().contains("does not exist"));
        assertTrue(rei.getResponse("delete 2").response().contains("does not exist"));
        assertTrue(rei.getResponse("list").response().contains("1.[T][ ] alpha"));
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
