import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/** Exercises storage boundary cases without requiring an external test library. */
public class StorageEdgeCaseTest {
    public static void main(String[] args) throws IOException {
        Path testDirectory = Files.createTempDirectory("rei-storage-test-");
        try {
            testMissingFolderAndRoundTrip(testDirectory);
            testEmptyList(testDirectory);
            testCorruptedRecords(testDirectory);
            testInvalidStoragePaths(testDirectory);
            System.out.println("All storage edge-case tests passed.");
        } finally {
            deleteRecursively(testDirectory);
        }
    }

    /** Verifies folder creation and lossless saving of every task type and state. */
    private static void testMissingFolderAndRoundTrip(Path testDirectory) throws IOException {
        Path dataFile = testDirectory.resolve("missing").resolve("nested").resolve("tasks.txt");
        Storage storage = new Storage(dataFile);
        assertEquals(0, storage.load().tasks().size(), "A missing file should load as an empty list");

        Task todo = new Task("read | revise 雷");
        todo.markAsDone();
        List<Task> original = List.of(
                todo,
                new Deadlines("submit report", LocalDateTime.of(2026, 8, 28, 18, 0)),
                new Events("project meeting", LocalDateTime.of(2026, 8, 29, 10, 0),
                        LocalDateTime.of(2026, 8, 29, 12, 0)));
        storage.save(original);

        Storage.LoadResult result = storage.load();
        assertEquals(0, result.skippedLines(), "A valid file should not contain skipped records");
        assertEquals(original.size(), result.tasks().size(), "Every task should be restored");
        for (int i = 0; i < original.size(); i++) {
            Task expected = original.get(i);
            Task actual = result.tasks().get(i);
            assertEquals(expected.getTaskType(), actual.getTaskType(), "Task type should survive loading");
            assertEquals(expected.toString(), actual.toString(), "Task details should survive loading");
            assertEquals(expected.isDone(), actual.isDone(), "Task status should survive loading");
        }
    }

    /** Verifies that saving no tasks truncates a previously populated file. */
    private static void testEmptyList(Path testDirectory) throws IOException {
        Storage storage = new Storage(testDirectory.resolve("empty-list.txt"));
        storage.save(List.of(new Task("temporary")));
        storage.save(List.of());
        assertEquals(0, storage.load().tasks().size(), "An empty saved list should remain empty");
    }

    /** Verifies that valid records survive alongside several forms of corruption. */
    private static void testCorruptedRecords(Path testDirectory) throws IOException {
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
        assertEquals(1, result.tasks().size(), "A valid record should still load");
        assertEquals(4, result.skippedLines(), "Every corrupted record should be counted");
    }

    /** Verifies that unusable paths report I/O errors instead of crashing unpredictably. */
    private static void testInvalidStoragePaths(Path testDirectory) throws IOException {
        Path regularFile = testDirectory.resolve("regular-file");
        Files.writeString(regularFile, "content", StandardCharsets.UTF_8);
        Storage storage = new Storage(regularFile.resolve("tasks.txt"));
        try {
            storage.save(List.of(new Task("cannot save")));
            throw new AssertionError("Saving below a regular file should fail");
        } catch (IOException expected) {
            // Expected: the parent path cannot be used as a directory.
        }
    }

    /** Removes only the temporary directory created by this test run. */
    private static void deleteRecursively(Path directory) throws IOException {
        try (var paths = Files.walk(directory)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + ": expected " + expected + ", got " + actual);
        }
    }
}
