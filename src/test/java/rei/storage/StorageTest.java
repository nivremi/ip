package rei.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import rei.task.Deadlines;
import rei.task.Events;
import rei.task.Task;

/** Tests saving, loading, and recovery behavior of {@link Storage}. */
public class StorageTest {
    @TempDir
    Path testDirectory;

    @Test
    public void load_missingFile_returnsEmptyResult() throws IOException {
        Storage storage = new Storage(testDirectory.resolve("missing").resolve("tasks.txt"));

        Storage.LoadResult result = storage.load();

        assertEquals(0, result.tasks().size());
        assertEquals(0, result.skippedLines());
    }

    @Test
    public void saveAndLoad_allTaskTypesAndStates_restoresEveryField() throws IOException {
        Storage storage = new Storage(testDirectory.resolve("nested").resolve("tasks.txt"));
        Task todo = new Task("read | revise 雷");
        todo.markAsDone();
        List<Task> original = List.of(
                todo,
                new Deadlines("submit report", LocalDateTime.of(2026, 8, 28, 18, 0)),
                new Events("project meeting", LocalDateTime.of(2026, 8, 29, 10, 0),
                        LocalDateTime.of(2026, 8, 29, 12, 0)));

        storage.save(original);
        Storage.LoadResult result = storage.load();

        assertEquals(0, result.skippedLines());
        assertEquals(original.size(), result.tasks().size());
        for (int i = 0; i < original.size(); i++) {
            Task expected = original.get(i);
            Task actual = result.tasks().get(i);
            assertEquals(expected.getTaskType(), actual.getTaskType());
            assertEquals(expected.getDescription(), actual.getDescription());
            assertEquals(expected.toString(), actual.toString());
            assertEquals(expected.isDone(), actual.isDone());
        }
    }

    @Test
    public void save_emptyList_truncatesExistingTasks() throws IOException {
        Storage storage = new Storage(testDirectory.resolve("tasks.txt"));
        storage.save(List.of(new Task("temporary")));

        storage.save(List.of());

        assertEquals(0, storage.load().tasks().size());
    }

    @Test
    public void load_mixedValidAndCorruptedRecords_skipsOnlyCorruptedRecords() throws IOException {
        Path dataFile = testDirectory.resolve("corrupted.txt");
        Storage storage = new Storage(dataFile);
        storage.save(List.of(new Task("valid task")));
        String validRecord = Files.readString(dataFile, StandardCharsets.UTF_8).trim();
        Files.write(dataFile, List.of(
                validRecord,
                "not a task record",
                "T | 9 | dmFsaWQ=",
                "D | 0 | c2hvcnQ=",
                "T | 0 | "), StandardCharsets.UTF_8);

        Storage.LoadResult result = storage.load();

        assertEquals(1, result.tasks().size());
        assertEquals("valid task", result.tasks().get(0).getDescription());
        assertEquals(4, result.skippedLines());
    }

    @Test
    public void save_parentPathIsAFile_throwsIOException() throws IOException {
        Path regularFile = testDirectory.resolve("regular-file");
        Files.writeString(regularFile, "content", StandardCharsets.UTF_8);
        Storage storage = new Storage(regularFile.resolve("tasks.txt"));

        assertThrows(IOException.class, () -> storage.save(List.of(new Task("cannot save"))));
    }
}
