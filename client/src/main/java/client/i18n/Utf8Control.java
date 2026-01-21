package client.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class Utf8Control extends ResourceBundle.Control {

    /**
     * A method for creating the correct resource bundle for displaying Romanian
     * @param baseName
     *        the base bundle name of the resource bundle, a fully
     *        qualified class name
     * @param locale
     *        the locale for which the resource bundle should be
     *        instantiated
     * @param format
     *        the resource bundle format to be loaded
     * @param loader
     *        the {@code ClassLoader} to use to load the bundle
     * @param reload
     *        the flag to indicate bundle reloading; {@code true}
     *        if reloading an expired resource bundle,
     *        {@code false} otherwise
     * @return ResourceBundle - returns a new resource bundle with all UTF-8 characters
     * @throws IllegalAccessException - an Exception which might arise if a non-privileged
     *        user alters the configuration
     * @throws InstantiationException - for any Instantiation Exception
     * @throws IOException - for any IO Exceptions
     */
    @Override
    public ResourceBundle newBundle(
            String baseName,
            Locale locale,
            String format,
            ClassLoader loader,
            boolean reload
    ) throws IllegalAccessException, InstantiationException, IOException {

        // Lăsăm celelalte formate la implementarea de bază
        if (!"java.properties".equals(format)) {
            return super.newBundle(baseName, locale, format, loader, reload);
        }

        String bundleName = toBundleName(baseName, locale);
        String resourceName = toResourceName(bundleName, "properties");

        InputStream stream;
        if (reload) {
            var url = loader.getResource(resourceName);
            if (url == null) {
                return null;
            }
            var connection = url.openConnection();
            connection.setUseCaches(false);
            stream = connection.getInputStream();
        } else {
            stream = loader.getResourceAsStream(resourceName);
        }

        if (stream == null) {
            return null;
        }

        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            return new PropertyResourceBundle(reader);
        }
    }
}
