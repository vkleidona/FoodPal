package client.scenes;

import com.google.inject.Inject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import recipes.Recipe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EditInstructionsCtrl {

    @FXML
    public ListView<String> instructionList;
    private ObservableList<String> steps;
    private Recipe currentRecipe;

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
     * TO DO: Modify MainCtrl so that it calls the following method when the user switches to the edit instructions screen
     * It loads the existing instructions into the list.
     * @param recipe : the recipe we are currently working on
     */

    public void initData(Recipe recipe) {
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

    /**
     * The method behind the edit button
     */
    @FXML
    public void editInstruction() {
        int selectedIdx = instructionList.getSelectionModel().getSelectedIndex(); //get the row number selected
        if (selectedIdx < 0) {
            showAlert("No Selection", "Please select a step to edit.");
            return;
        }

        String currentStep = steps.get(selectedIdx); //get the row
        TextInputDialog dialog = new TextInputDialog(currentStep);
        dialog.setTitle("Edit Instruction");
        dialog.setHeaderText("Edit the selected step:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newStep -> {
            if (!newStep.trim().isEmpty()) {
                steps.set(selectedIdx, newStep.trim()); //set the new row
            }
        });
    }

    /**
     * The method behind the delete button
     */
    @FXML
    public void deleteInstruction() {
        int selectedIdx = instructionList.getSelectionModel().getSelectedIndex(); //get the row number selected
        if (selectedIdx >= 0) {
            steps.remove(selectedIdx);  //remove the selected row from the steps
        } else {
            showAlert("No Selection", "Please select a step to delete.");
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
            showAlert("Error", "Failed to save instructions: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void cancel() {
        returnToMainUI();
    }

    /**
     * This method is used to close the edit instructions screen
     * TO DO: make it change to the main ui and not close everything
     */
    private void returnToMainUI() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/client.scenes/MainUI.fxml"));

            // this builds the UI, meaning any previous user input will be reset
            Parent mainRoot = loader.load();

            Stage stage = (Stage) instructionList.getScene().getWindow();
            Scene mainScene = new Scene(mainRoot);

            // the current scene is replaced with the new one
            stage.setScene(mainScene);
            stage.show();
        } catch (IOException e){
        }
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