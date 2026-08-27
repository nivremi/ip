package rei.storage;

import rei.task.Deadlines;
import rei.task.Events;
import rei.task.Task;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/** Saves and loads Rei tasks using a file relative to the project directory. */
public class Storage {
    private static final String FIELD_SEPARATOR = " | ";
    private final Path filePath;

    /** Creates storage that uses the supplied data file. */
    public Storage(Path filePath) {
        this.filePath = filePath;
    }

    /**
     * Load all valid tasks and ignore corrupted records.
     * Missing files are treated as an empty task list.
     */
    public LoadResult load() throws IOException {
        List<Task> tasks = new ArrayList<>();
        int skippedLines = 0;
        if (!Files.exists(filePath)) {
            return new LoadResult(tasks, skippedLines);
        }

        for (String line : Files.readAllLines(filePath, StandardCharsets.UTF_8)) {
            try {
                tasks.add(deserialise(line));
            } catch (IllegalArgumentException exception) {
                skippedLines++;
            }
        }
        return new LoadResult(tasks, skippedLines);
    }

    /** Writes the complete task list, creating its parent folder when needed. */
    public void save(List<Task> tasks) throws IOException {
        Path parent = filePath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        List<String> lines = tasks.stream().map(this::serialise).toList();
        Files.write(filePath, lines, StandardCharsets.UTF_8);
    }

    /** Converts one task into a delimiter-separated record with safely encoded text. */
    private String serialise(Task task) {
        List<String> fields = new ArrayList<>();
        fields.add(task.getTaskType());
        fields.add(task.isDone() ? "1" : "0");
        fields.add(encode(task.getDescription()));
        if (task instanceof Deadlines deadline) {
            fields.add(encode(deadline.getBy().toString()));
        } else if (task instanceof Events event) {
            fields.add(encode(event.getStart().toString()));
            fields.add(encode(event.getEnd().toString()));
        }
        return String.join(FIELD_SEPARATOR, fields);
    }

    /** Reconstructs one task and rejects records that do not match the expected format. */
    private Task deserialise(String line) {
        String[] fields = line.split(" \\| ", -1);
        if (fields.length < 3 || !(fields[1].equals("0") || fields[1].equals("1"))) {
            throw new IllegalArgumentException("Invalid task record");
        }

        Task task = switch (fields[0]) {
        case "T" -> {
            requireLength(fields, 3);
            yield new Task(decodeRequired(fields[2]));
        }
        case "D" -> {
            requireLength(fields, 4);
            yield new Deadlines(decodeRequired(fields[2]),
                    LocalDateTime.parse(decodeRequired(fields[3])));
        }
        case "E" -> {
            requireLength(fields, 5);
            yield new Events(decodeRequired(fields[2]), LocalDateTime.parse(decodeRequired(fields[3])),
                    LocalDateTime.parse(decodeRequired(fields[4])));
        }
        default -> throw new IllegalArgumentException("Unknown task type");
        };
        if (fields[1].equals("1")) {
            task.markAsDone();
        }
        return task;
    }

    /** Ensures a record has exactly the number of fields required by its task type. */
    private void requireLength(String[] fields, int expectedLength) {
        if (fields.length != expectedLength) {
            throw new IllegalArgumentException("Wrong number of fields");
        }
    }

    private String encode(String text) {
        return Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String text) {
        return new String(Base64.getDecoder().decode(text), StandardCharsets.UTF_8);
    }

    /** Decodes a required text field and rejects empty or whitespace-only values. */
    private String decodeRequired(String text) {
        String decoded = decode(text);
        if (decoded.isBlank()) {
            throw new IllegalArgumentException("Required task field is empty");
        }
        return decoded;
    }

    /** Contains loaded tasks and the number of malformed records that were skipped. */
    public record LoadResult(List<Task> tasks, int skippedLines) {
    }
}
