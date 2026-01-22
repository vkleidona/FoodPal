package client.i18n;

import client.configuration.ConfigManager;
import java.util.*;

public final class LanguageManager {

    private LanguageManager() {}

    public static Locale getCurrentLocale() {
        String lang = ConfigManager.getConfig().getLanguage();
        return Locale.of(lang);
    }

    public static void setLocale(Locale locale) {
        ConfigManager.getConfig().setLanguage(locale.getLanguage());
        ConfigManager.save();
    }

    public static String getString(String key) {
        ResourceBundle bundle = ResourceBundle.getBundle(
                "client.scenes.i18n.messages",
                getCurrentLocale(),
                new Utf8Control()
        );
        return bundle.getString(key);
    }

    /**
     * Loads filters for the languages
     * @return List <Locale> - returns a set of all Locales
     */
    public static List<Locale> loadFilterLocales () {
        return ConfigManager.getConfig().getFilteredLocales().stream().map(Locale::of).toList();
    }

    /**
     * Responsible for storing the information on locale filters
     * @param locales - the list of locales to be saved
     */
    public static void saveFilterLocales (List <Locale> locales) {
        List<String> localeStrings = locales.stream()
                .map(Locale::getLanguage)
                .toList();

        ConfigManager.getConfig().setFilteredLocales(localeStrings);
        ConfigManager.save();
    }
}
