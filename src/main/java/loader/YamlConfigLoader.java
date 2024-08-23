package loader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class YamlConfigLoader {

    private static final String CONFIG_PATH = "src/test/resources/config.yaml";

    public static String loadBaseUri() {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try {
            JsonNode rootNode = mapper.readTree(Files.newBufferedReader(Paths.get(CONFIG_PATH)));
            JsonNode baseUriNode = rootNode.get("base-uri");
            if (baseUriNode == null) {
                throw new RuntimeException("base_uri not found in " + CONFIG_PATH);
            }
            return baseUriNode.asText();
        } catch (IOException e) {
            System.err.println("Failed to load configuration from " + CONFIG_PATH);
            e.printStackTrace();
            throw new RuntimeException("Failed to load configuration", e);
        }
    }
}