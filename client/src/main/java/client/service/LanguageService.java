package client.service;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class LanguageService {

    /**
     * A method for rendering flag icons from a language code
     * @param langCode - the language code for the flag icon
     * @param size - the radius of the circle
     * @return ImageView - an icon with the necessary flag
     */
    public ImageView renderIcon (String langCode, double size) {
        String iconFileName = reformatCodeIntoFile(langCode);
        Image flagImage = new Image(Objects.requireNonNull
                (getClass().getResourceAsStream("/flags/" + iconFileName + "-flag.jpg")));

        ImageView icon = new ImageView(flagImage);
        icon.setFitWidth(size);
        icon.setFitHeight(size);

        Circle clip = new Circle(size / 2.0, size / 2.0, size / 2.0);
        icon.setClip(clip);
        return icon;
    }

    /**
     * Reformats the language code into a file name
     * @param langCode - the language code
     * @return String - the necessary flag name
     */
    public String reformatCodeIntoFile (String langCode) {
        return switch (langCode) {
            case "nl" -> "dutch";
            case "ro" -> "romanian";
            case "bg" -> "bulgarian";
            case "sk" -> "slovak";
            case "de" -> "german";
            default -> "english";
        };
    }

    /**
     * A method which provides the display name for the language
     * from the language code
     * @param langCode - the code of the language
     * @return String - the value of the name to be displayed
     */
    public String getDisplayName (String langCode) {
        return switch (langCode) {
            case "nl" -> "Nederlands";
            case "ro" -> "Română";
            case "bg" -> "Български";
            case "sk" -> "Slovenčina";
            case "de" -> "Deutsch";
            default -> "English";
        };
    }

    /**
     * A method for formatting the ingredient inside a Shopping List
     * @param sourceName - the name of the recipe
     * @param langCode - the language code for the Locale of the recipe
     * @return String - a formatted String for display in the Shopping List
     */
    public String ingredientFormat (String sourceName, String langCode) {
        return switch (langCode) {
            case "nl" -> " (voor het voorbereiden " + sourceName + ")";
            case "ro" -> " (pentru pregătire " + sourceName + ")";
            case "bg" -> " (за приготвяне на " + sourceName + ")";
            case "sk" -> " (na prípravu receptu " + sourceName + ")";
            case "de" -> " (um " + sourceName + " vorzubereiten)";
            default -> " (for preparing " + sourceName + ")";
        };
    }

}
