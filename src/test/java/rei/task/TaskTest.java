package rei.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

/** Tests task state changes and scheduled-task display formatting. */
public class TaskTest {
    @Test
    public void markAndUnmark_updatesCompletionStateAndIcon() {
        Task task = new Task("read book");
        assertFalse(task.isDone());
        assertEquals(" ", task.getStatusIcon());

        task.markAsDone();
        assertTrue(task.isDone());
        assertEquals("X", task.getStatusIcon());

        task.markAsUndone();
        assertFalse(task.isDone());
        assertEquals(" ", task.getStatusIcon());
    }

    @Test
    public void toString_scheduledTasks_formatsTypedDatesForUsers() {
        Deadlines deadline = new Deadlines(
                "submit report", LocalDateTime.of(2026, 8, 28, 18, 0));
        Events event = new Events(
                "project meeting",
                LocalDateTime.of(2026, 8, 29, 10, 0),
                LocalDateTime.of(2026, 8, 29, 12, 0));

        assertEquals("submit report (by: Aug 28 2026, 6:00 PM)", deadline.toString());
        assertEquals("project meeting (from: Aug 29 2026, 10:00 AM "
                + "to: Aug 29 2026, 12:00 PM)", event.toString());
    }
}
