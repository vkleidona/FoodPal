package client.scenes;

import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import recipes.Recipe;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import client.i18n.LanguageManager;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;

public class EditInstructionsCtrl {

    @FXML
    public ListView<String> instructionList;
    private ObservableList<String> steps;
    private Recipe currentRecipe;

    @FXML
    private MenuButton languageButton;

    @FXML
    private ResourceBundle resources;

    @FXML
    public void initialize() {
        setupLanguageMenu();
    }

    private void setupLanguageMenu() {
        languageButton.getItems().clear();

        MenuItem enItem = new MenuItem("English");
        enItem.setOnAction(e -> changeLanguage(new Locale("en")));

        MenuItem nlItem = new MenuItem("Nederlands");
        nlItem.setOnAction(e -> changeLanguage(new Locale("nl")));

        MenuItem roItem = new MenuItem("Română");
        roItem.setOnAction(e -> changeLanguage(new Locale("ro")));

        MenuItem bgItem = new MenuItem("Български");
        bgItem.setOnAction(e -> changeLanguage(new Locale("bg")));

        languageButton.getItems().addAll(enItem, nlItem, roItem, bgItem);

        Locale current = LanguageManager.getLocale();
        switch (current.getLanguage()) {
            case "nl" -> languageButton.setText("Nederlands");
            case "ro" -> languageButton.setText("Română");
            case "bg" -> languageButton.setText("Български");
            default -> languageButton.setText("English");
        }
    }

    private void changeLanguage(Locale newLocale) {
        LanguageManager.setLocale(newLocale);
        switch (newLocale.getLanguage()) {
            case "nl" -> languageButton.setText("Nederlands");
            case "ro" -> languageButton.setText("Română");
            case "bg" -> languageButton.setText("Български");
            default -> languageButton.setText("English");
        }
        mainCtrl.reloadScenesForNewLanguage();
    }



    // ServerUtils to communicate with the backend
    private final client.utils.ServerUtils server;
    private final MainCtrl mainCtrl;

    /**
     * Constructor
     * @param server
     * @param mainCtrl
     */
    @Inject
    public EditInstructionsCtrl(client.utils.ServerUtils server, MainCtrl mainCtrl) {
        this.server = server;
        this.mainCtrl = mainCtrl;
    }



   

    /**
     *
     * @param recipe : the recipe we are currently working on
     */
    public void initializeData(Recipe recipe) {
        currentRecipe = recipe;

        // Handle case where instructions list is null in the database
        List<String> existingInstructions = recipe.getInstructions();
        if (existingInstructions == null) {
            existingInstructions = new ArrayList<>();
        }

        // Pass the steps from the existing instructions to the Observable List so that the screen updates automatically
        steps = FXCollections.observableArrayList(existingInstructions);
        instructionList.setItems(steps);
    }

    //Button actions

    /**
     * The method behind the add button
     */
    @FXML
    public void addInstruction() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(resources.getString("editInstructions.dialog.add.title"));
        dialog.setHeaderText(resources.getString("editInstructions.dialog.add.header"));
        dialog.setContentText(resources.getString("editInstructions.dialog.add.content"));

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(step -> {
            if (!step.trim().isEmpty()) {
                steps.add(step.trim());
            }
        });
    }


    /**
     * The method behind the edit button
     */
    @FXML
    public void editInstruction() {
        int selectedIdx = instructionList.getSelectionModel().getSelectedIndex();
        if (selectedIdx < 0) {
            showAlert(resources.getString("editInstructions.alert.noSelection.title"),
                    resources.getString("editInstructions.alert.noSelection.edit"));
            return;
        }

        String currentStep = steps.get(selectedIdx);
        TextInputDialog dialog = new TextInputDialog(currentStep);
        dialog.setTitle(resources.getString("editInstructions.dialog.edit.title"));
        dialog.setHeaderText(resources.getString("editInstructions.dialog.edit.header"));

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newStep -> {
            if (!newStep.trim().isEmpty()) {
                steps.set(selectedIdx, newStep.trim());
            }
        });
    }


    /**
     * The method behind the delete button
     */
    @FXML
    public void deleteInstruction() {
        int selectedIdx = instructionList.getSelectionModel().getSelectedIndex();
        if (selectedIdx >= 0) {
            steps.remove(selectedIdx);
        } else {
            showAlert(resources.getString("editInstructions.alert.noSelection.title"),
                    resources.getString("editInstructions.alert.noSelection.delete"));
        }
    }

    /**
     * The method behind the save changes button
     */
    @FXML
    public void saveChanges() {
        try {
            // Convert ObservableList back to a regular List and save it to the recipe object
            currentRecipe.setInstructions(new ArrayList<>(steps));
            // Send the updated recipe to the server
            server.updateRecipe(currentRecipe);


            returnToMainUI();

        } catch (Exception e) {
            showAlert(resources.getString("editInstructions.alert.error.title"),
                    resources.getString("editInstructions.alert.error.saveFailed") + " " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Refreshes the content
     * @param recipe - the recipe's current
     */
    public void refreshContent (Recipe recipe) {
        Platform.runLater(() -> {
            this.currentRecipe = recipe;
            instructionList.getItems().setAll(recipe.getInstructions());
        });
    }


    @FXML
    private void returnToMainUI() {
        mainCtrl.showMainMenu();
    }

    /**
     *
     * @param title
     * @param content
     */
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}