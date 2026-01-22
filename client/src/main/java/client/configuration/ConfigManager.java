package client.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;

public class ConfigManager {
    private static File configFile;
    private static Config config;
    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * An initializer for the configuration
     * @param path - the path on which to initialize the configuration
     */
    public static void init(String path) {
        if (path == null) {
            path = System.getProperty("user.home") + "/.foodpal/config.json";
        }
        configFile = new File(path);

        File dir = configFile.getParentFile();
        if (dir != null && !dir.exists()) {
            if (dir.mkdirs()) {
                System.out.println("Directory was successfully created");
            }
        }

        load();
    }

    /**
     * A method for loading the configuration
     */
    public static void load() {
        try {
            if (configFile.exists()) {
                String json = FileUtils.readFileToString(configFile, StandardCharsets.UTF_8);
                config = mapper.readValue(json, Config.class);
            } else {
                config = new Config();
            }
        } catch (Exception e) {
            config = new Config();
        }
    }

    /**
     * A method which is used to save the configuration file
     */
    public static void save() {
        try {
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(config);
            FileUtils.writeStringToFile(configFile, json, StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("Exception when saving the configuration");
        }
    }

    public static Config getConfig() {
        return config;
    }
}

