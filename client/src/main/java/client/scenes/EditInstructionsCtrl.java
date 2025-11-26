package client.scenes;

import com.google.inject.Inject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import recipes.Recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EditInstructionsCtrl {

    // UI Elements defined in your FXML
    @FXML
    private ListView<String> instructionList;

    // Data structures
    private ObservableList<String> steps;
    private Recipe currentRecipe;

    // Injected ServerUtils to communicate with the backend
    // (Assuming you have a class named ServerUtils in client.utils)
    private final client.utils.ServerUtils server;
    private final MainCtrl mainCtrl;

    @Inject
    public EditInstructionsCtrl(client.utils.ServerUtils server, MainCtrl mainCtrl) {
        this.server = server;
        this.mainCtrl = mainCtrl;
    }

    /**
     * INITIALIZATION
     * This method is called by MainCtrl when you switch to this screen.
     * It loads the existing instructions into the list.
     */
    public void initData(Recipe recipe) {
        this.currentRecipe = recipe;

        // Handle case where instructions list is null in the database
        List<String> existingInstructions = recipe.getInstructions();
        if (existingInstructions == null) {
            existingInstructions = new ArrayList<>();
        }

        // Create an ObservableList so the UI updates automatically
        this.steps = FXCollections.observableArrayList(existingInstructions);
        instructionList.setItems(steps);
    }

    // --- BUTTON ACTIONS ---

    @FXML
    public void addInstruction() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Instruction");
        dialog.setHeaderText("Enter a new step:");
        dialog.setContentText("Step:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(step -> {
            if (!step.trim().isEmpty()) {
                steps.add(step.trim());
            }
        });
    }

    @FXML
    public void editInstruction() {
        int selectedIdx = instructionList.getSelectionModel().getSelectedIndex();
        if (selectedIdx < 0) {
            showAlert("No Selection", "Please select a step to edit.");
            return;
        }

        String currentStep = steps.get(selectedIdx);
        TextInputDialog dialog = new TextInputDialog(currentStep);
        dialog.setTitle("Edit Instruction");
        dialog.setHeaderText("Edit the selected step:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newStep -> {
            if (!newStep.trim().isEmpty()) {
                steps.set(selectedIdx, newStep.trim());
            }
        });
    }

    @FXML
    public void deleteInstruction() {
        int selectedIdx = instructionList.getSelectionModel().getSelectedIndex();
        if (selectedIdx >= 0) {
            steps.remove(selectedIdx);
        } else {
            showAlert("No Selection", "Please select a step to delete.");
        }
    }

    @FXML
    public void saveChanges() {
        try {
            // 1. Update the local recipe object with the new list
            // We convert ObservableList back to a regular List
            currentRecipe.setInstructions(new ArrayList<>(steps));
            // 2. Send the updated recipe to the server
            // Note: You need to implement 'updateRecipe' in your ServerUtils
            server.updateRecipe(currentRecipe);

            // 3. Close the window or go back
            // Assuming this is a popup/secondary stage, we close it
            // Or use mainCtrl.showRecipeDetails(currentRecipe);
            closeWindow();

        } catch (Exception e) {
            showAlert("Error", "Failed to save instructions: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void cancel() {
        closeWindow();
    }

    private void closeWindow() {
        // Gets the stage (window) this scene is on and closes it
        Stage stage = (Stage) instructionList.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}