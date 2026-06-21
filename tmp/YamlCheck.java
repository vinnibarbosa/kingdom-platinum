import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.yaml.snakeyaml.Yaml;

public final class YamlCheck {
    public static void main(String[] args) throws Exception {
        for (String value : args) {
            Path path = Path.of(value);
            try (InputStream input = Files.newInputStream(path)) {
                new Yaml().load(input);
            }
            System.out.println("YAML valido: " + path);
        }
    }
}
