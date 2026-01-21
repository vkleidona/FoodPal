package client.scenes;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import recipes.Recipe;

import java.util.*;

import client.i18n.LanguageManager;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;

public class EditInstructionsCtrl {

    @FXML
    private ListView<String> instructionList;
    private ObservableList<String> steps;
    private Recipe currentRecipe;

    @FXML
    private MenuButton languageButton;

    @FXML
    private ResourceBundle resources;

    private final client.utils.ServerUtils server;
    private final MainCtrl mainCtrl;

    /**
     * Initializer method - sets up the language method,
     * as well as the currently existing ingredients
     * @param recipe - the currently edited recipe
     */
    @FXML
    public void initialize(Recipe recipe) {
        currentRecipe = recipe;

        // Handle case where instructions list is null in the database
        List<String> existingInstructions = recipe.getInstructions();
        if (existingInstructions == null) {
            existingInstructions = new ArrayList<>();
        }

        // Pass the steps from the existing instructions to the Observable List
        // so that the screen updates automatically
        steps = FXCollections.observableArrayList(existingInstructions);
        instructionList.setItems(steps);
        setupLanguageMenu();
    }

    public ListView<String> getInstructionList() {
        return instructionList;
    }

    /**
     * A method which sets up the icons for the language menu
     * @param name - the name of the language we are adding
     * @param langCode - the locale code of the language
     * @param iconFileName - the encoding for the name of the icon file
     * @return MenuItem - returns the ready-made MenuItem with the proper icon
     */
    public MenuItem setupMenuItemIcons (String name, String langCode, String iconFileName) {
        MenuItem menuItem = new MenuItem(name);

        Image flagImage = new Image(Objects.requireNonNull
                (getClass().getResourceAsStream("/flags/" + iconFileName + "-flag.jpg")));

        ImageView icon = new ImageView(flagImage);
        icon.setFitWidth(16);
        icon.setFitHeight(16);

        Circle clip = new Circle(8, 8, 8);
        icon.setClip(clip);

        menuItem.setGraphic(icon);

        menuItem.setOnAction(_ -> changeLanguage(Locale.of(langCode)));

        return menuItem;
    }

    /**
     * A method which updates the flag icon of the language button when necessary
     * @param iconFileName - the encoding for the name of the icon file
     */
    public void updateButtonGraphic(String iconFileName) {
        Image img = new Image (Objects.requireNonNull
                (getClass().getResourceAsStream("/flags/" + iconFileName + "-flag.jpg")));
        ImageView imageView = new ImageView(img);

        imageView.setFitWidth(16);
        imageView.setFitHeight(16);

        Circle clip = new Circle(8, 8, 8);
        imageView.setClip(clip);

        // Set the graphic to the MenuButton itself
        languageButton.setGraphic(imageView);
    }

    /**
     * A setup method for the language menu -
     * adds the constructed MenuItems using the helper methods with the appropriate icon
     */
    public void setupLanguageMenu() {
        languageButton.getItems().clear();

        MenuItem enItem = setupMenuItemIcons("English", "en", "english");
        MenuItem nlItem = setupMenuItemIcons("Nederlands", "nl", "dutch");
        MenuItem bgItem = setupMenuItemIcons("Български", "bg", "bulgarian");
        MenuItem roItem = setupMenuItemIcons("Română", "ro", "romanian");
        MenuItem skItem = setupMenuItemIcons("Slovenčina", "sk", "slovak");
        MenuItem deItem = setupMenuItemIcons("Deutsch", "de", "german");

        languageButton.getItems().addAll(enItem, nlItem, bgItem, roItem, skItem, deItem);

        Locale current = LanguageManager.getLocale();

        switch (current.getLanguage()) {
            case "nl" -> {
                languageButton.setText("Nederlands");
                updateButtonGraphic("dutch");
            }
            case "ro" -> {
                languageButton.setText("Română");
                updateButtonGraphic("romanian");
            }
            case "bg" -> {
                languageButton.setText("Български");
                updateButtonGraphic("bulgarian");
            }
            case "sk" -> {
                languageButton.setText("Slovenčina");
                updateButtonGraphic("slovak");
            }
            case "de" -> {
                languageButton.setText("Deutsch");
                updateButtonGraphic("german");
            }
            default -> {
                languageButton.setText("English");
                updateButtonGraphic("english");
            }
        }
    }

    /**
     * A method for changing the language of the menus to a new Locale
     * @param newLocale - the new language we want to set the menus to
     */
    public void changeLanguage(Locale newLocale) {
        LanguageManager.setLocale(newLocale);
        switch (newLocale.getLanguage()) {
            case "nl" -> languageButton.setText("Nederlands");
            case "ro" -> languageButton.setText("Română");
            case "bg" -> languageButton.setText("Български");
            case "sk" -> languageButton.setText("Slovenčina");
            case "de" -> languageButton.setText("Deutsch");
            default -> languageButton.setText("English");
        }
        mainCtrl.reloadScenesForNewLanguage();
    }

    /**
     * Constructor for the EditInstructions class using Dependency Injection
     * @param server - the injected instance of the ServerUtils class
     * @param mainCtrl - the injected instance of the MainCtrl class
     */
    @Inject
    public EditInstructionsCtrl(client.utils.ServerUtils server, MainCtrl mainCtrl) {
        this.server = server;
        this.mainCtrl = mainCtrl;
    }

    /**
     * The method behind the add button
     */
    public void addInstruction() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(resources.getString("editInstructions.addDialog.title"));
        dialog.setHeaderText(resources.getString("editInstructions.addDialog.header"));
        dialog.setContentText(resources.getString("editInstructions.addDialog.content"));

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(step -> {
            if (!step.trim().isEmpty()) {
                steps.add(step.trim());
            }
        });
    }



    /**
     * A method responsible for editing a specific instruction
     */
    public void editInstruction() {
        int selectedIdx = instructionList.getSelectionModel().getSelectedIndex();
        if (selectedIdx < 0) {
            showAlert(
                    resources.getString("editInstructions.noSelectionAlert.title"),
                    resources.getString("editInstructions.noSelectionAlert.edit")
            );
            return;
        }

        String currentStep = steps.get(selectedIdx);
        TextInputDialog dialog = new TextInputDialog(currentStep);
        dialog.setTitle(resources.getString("editInstructions.editDialog.title"));
        dialog.setHeaderText(resources.getString("editInstructions.editDialog.header"));

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newStep -> {
            if (!newStep.trim().isEmpty()) {
                steps.set(selectedIdx, newStep.trim());
            }
        });
    }



    /**
     * A method responsible for deleting an instruction
     */
    public void deleteInstruction() {
        int selectedIdx = instructionList.getSelectionModel().getSelectedIndex();
        if (selectedIdx >= 0) {
            steps.remove(selectedIdx);
        } else {
            showAlert(
                    resources.getString("editInstructions.noSelectionAlert.title"),
                    resources.getString("editInstructions.noSelectionAlert.delete")
            );
        }
    }


    /**
     * The method behind the save changes button
     */
    @FXML
    public void saveChanges() {
        try {
            currentRecipe.setInstructions(new ArrayList<>(steps));
            server.updateRecipe(currentRecipe);
            returnToMainUI();
        } catch (Exception e) {
            showAlert(
                    resources.getString("editInstructions.saveErrorAlert.title"),
                    resources.getString("editInstructions.saveErrorAlert.message")
                            + " " + e.getMessage()
            );
            System.err.println("Error: Saving instructions failed.");
        }
    }


    /**
     * Refreshes the recipe content upon a server-pushed change
     * @param recipe - the recipe's current content
     */
    public void refreshContent (Recipe recipe) {
        Platform.runLater(() -> {
            this.currentRecipe = recipe;
            instructionList.getItems().setAll(recipe.getInstructions());
        });
    }

    /**
     * A method which returns to the Main UI
     */
    @FXML
    public void returnToMainUI() {
        mainCtrl.showMainMenu();
    }

    /**
     * A helper method for showing alerts
     * @param title - the title of the alert to be shown
     * @param content - the content of the alert
     */
    public void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}