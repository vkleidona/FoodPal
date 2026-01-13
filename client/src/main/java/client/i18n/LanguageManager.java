package client.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;

public final class LanguageManager {

    private static final String KEY_LANGUAGE = "language";
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
        return new Locale(lang);
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
            e.printStackTrace();
        }
    }
}
