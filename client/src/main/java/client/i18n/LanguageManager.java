package client.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public final class LanguageManager {

    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_LANGUAGE_FILTERS = "filter_locales";
    private static final Path CONFIG_PATH =
            Path.of(System.getProperty("user.home"), ".foodpal", "config.properties");

    private static Locale currentLocale = loadLocaleFromConfig();

    private LanguageManager() {}

    public static Locale getLocale() {
        return currentLocale;
    }

    public static void setLocale(Locale locale) {
        currentLocale = locale;
        saveLocaleToConfig(locale);
    }

    public static String getString(String key) {
        ResourceBundle bundle = ResourceBundle.getBundle(
                "client.scenes.i18n.messages",
                currentLocale,
                new Utf8Control()
        );
        return bundle.getString(key);
    }



    private static Locale loadLocaleFromConfig() {
        Properties props = new Properties();

        if (Files.exists(CONFIG_PATH)) {
            try (InputStream in = Files.newInputStream(CONFIG_PATH)) {
                props.load(in);
            } catch (IOException ignored) {
                // fall back to default
            }
        }

        String lang = props.getProperty(KEY_LANGUAGE, "en"); // default EN
        return Locale.of(lang);
    }

    private static void saveLocaleToConfig(Locale locale) {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());

            Properties props = new Properties();
            props.setProperty(KEY_LANGUAGE, locale.getLanguage());

            try (OutputStream out = Files.newOutputStream(CONFIG_PATH)) {
                props.store(out, "FoodPal user settings");
            }
        } catch (IOException e) {
            System.err.println("Preferred language could not be saved to recipe; " +
                    "Config file not found");
        }
    }

    /**
     * Loads filters for the languages
     * @return Set <Locale> - returns a set of all Locales
     */
    public static Set<Locale> loadFilterLocales () {
        Properties properties = new Properties();
        if (Files.exists(CONFIG_PATH)) {
            try (InputStream inputStream = Files.newInputStream(CONFIG_PATH)) {
                properties.load(inputStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        String filters = properties.getProperty(KEY_LANGUAGE_FILTERS, "");
        if (filters.isEmpty()) {
            return new HashSet<>();
        }

        return Arrays.stream(filters.split(",")).
                map(Locale::of).collect(Collectors.toSet());
    }

    /**
     * Responsible for storing the information on locale filters
     * @param locales - the list of locales to be saved
     */
    public static void saveFilterLocales (List <Locale> locales) {
        Properties properties = new Properties();
        if (Files.exists(CONFIG_PATH)) {
            try (InputStream inputStream = Files.newInputStream(CONFIG_PATH)) {
                properties.load(inputStream);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        String filterStr = locales.stream().map(Locale::getLanguage).
                collect(Collectors.joining(","));

        properties.setProperty(KEY_LANGUAGE_FILTERS, filterStr);
        try (OutputStream out = Files.newOutputStream(CONFIG_PATH)) {
            Files.createDirectories(CONFIG_PATH.getParent());
            properties.store(out, "FoodPal user settings");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
