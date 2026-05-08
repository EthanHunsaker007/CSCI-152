import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

public class NameGenerator {
    private static final Random random = new Random();
    private final List<String> names;

    public NameGenerator() throws IOException {
        names = Files.readAllLines(
            Paths.get("src/names.txt")
        );
    }

    public String getRandomName() {
        return names.get(
            random.nextInt(names.size())
        );
    }
}