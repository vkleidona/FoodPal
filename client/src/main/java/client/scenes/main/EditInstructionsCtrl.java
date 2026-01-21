package client.scenes.main;

import client.utils.MainCtrl;
import client.service.LanguageService;
import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import recipes.Recipe;

import java.util.*;

import client.i18n.LanguageManager;

public class EditInstructionsCtrl {

    @FXML
    private ListView<String> instructionList;
    private ObservableList<String> steps;
    private Recipe currentRecipe;

    @FXML
    private MenuButton languageButton;
    @FXML
    private Label recipeName;

    @FXML
    private ResourceBundle resources;

    private final client.utils.ServerUtils server;
    private final MainCtrl mainCtrl;
    private final LanguageService languageService;

    /**
     * Initializer method - sets up the language method,
     * as well as the currently existing ingredients
     * @param recipe - the currently edited recipe
     */
    @FXML
    public void initialize(Recipe recipe) {
        currentRecipe = recipe;
        recipeName.setText(currentRecipe.getName());

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
     * A method which updates the UI of the language button
     * @param locale - the locale of the language
     */
    public void updateLanguageButtonUI (Locale locale) {
        String langCode = locale.getLanguage();

        languageButton.setText(languageService.getDisplayName(langCode));
        languageButton.setGraphic(languageService.renderIcon(langCode, 16.0));
    }


    /**
     * A method which sets up the icons for the language menu
     * @param name - the name of the language we are adding
     * @param langCode - the locale code of the language
     * @return MenuItem - returns the ready-made MenuItem with the proper icon
     */
    public MenuItem setupMenuItemIcons (String name, String langCode) {
        MenuItem menuItem = new MenuItem(name);
        menuItem.setGraphic(languageService.renderIcon(langCode, 16.0));
        menuItem.setOnAction(_ -> changeLanguage(Locale.of(langCode)));
        return menuItem;
    }

    /**
     * A setup method for the language menu -
     * adds the constructed MenuItems using the helper methods with the appropriate icon
     */
    public void setupLanguageMenu() {
        languageButton.getItems().clear();

        List<String> codes = List.of("en", "nl", "bg", "ro", "sk", "de");

        for (String code : codes) {
            String displayName = languageService.getDisplayName(code);
            languageButton.getItems().add(setupMenuItemIcons(displayName, code));
        }

        updateLanguageButtonUI(LanguageManager.getLocale());
    }

    /**
     * A method for changing the language of the menus to a new Locale
     * @param newLocale - the new language we want to set the menus to
     */
    public void changeLanguage(Locale newLocale) {
        LanguageManager.setLocale(newLocale);
        updateLanguageButtonUI(newLocale);
        mainCtrl.reloadScenesForNewLanguage();
    }

    /**
     * Constructor for the EditInstructions class using Dependency Injection
     * @param server - the injected instance of the ServerUtils class
     * @param mainCtrl - the injected instance of the MainCtrl class
     * @param languageService - the injected instance of the Language Service
     */
    @Inject
    public EditInstructionsCtrl(client.utils.ServerUtils server, MainCtrl mainCtrl,
                                LanguageService languageService) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.languageService=languageService;
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