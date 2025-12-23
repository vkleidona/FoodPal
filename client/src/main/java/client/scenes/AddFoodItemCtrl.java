package client.scenes;

import client.utils.ServerUtils;
import jakarta.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import recipes.FoodItem;

public class AddFoodItemCtrl {

    @FXML
    private Label sceneTitle;
    @FXML
    private TextField foodItemName;
    @FXML
    private TextField kcalValue;
    @FXML
    private TextField carbsValue;
    @FXML
    private TextField proteinsValue;
    @FXML
    private TextField fatsValue;
    @FXML
    private Button cancelButton;
    @FXML
    private Button confirmButton;

    private final MainCtrl mainCtrl;
    private final ServerUtils serverUtils;

    private FoodItem editingItem;

    private String ingredientName;

    // these booleans indicate where the Add Food Item was accessed from
    private boolean cameFromShoppingList = false;
    private boolean cameFromAddRecipeToShoppingList = false;

    @Inject
    public AddFoodItemCtrl(MainCtrl mainCtrl, ServerUtils serverUtils) {
        this.mainCtrl = mainCtrl;
        this.serverUtils = serverUtils;
    }

    public void initialize () {
        if (ingredientName!=null) {
            foodItemName.setText(ingredientName);
        }
    }

    @FXML
    public void confirmChanges(){
        try {
            FoodItem newItem = new FoodItem(
                    foodItemName.getText(),
                    Integer.parseInt(kcalValue.getText()),
                    Double.parseDouble(carbsValue.getText()),
                    Double.parseDouble(proteinsValue.getText()),
                    Double.parseDouble(fatsValue.getText())
            );

            // Clear inputs
            foodItemName.clear();
            kcalValue.clear();
            carbsValue.clear();
            proteinsValue.clear();
            fatsValue.clear();

            // Save to server
            if(editingItem == null) {
                serverUtils.addFoodItem(newItem);
            } else {
                serverUtils.updateFoodItem(editingItem.getName(), newItem);
            }

            confirmButton.setText("Confirm");
            sceneTitle.setText("Adding Food Item");

            if (mainCtrl.getFoodItemMainCtrl() != null) {
                mainCtrl.getFoodItemMainCtrl().refreshFoodItems();
            }

            // if we came from "Create Recipe" (draft exists), we go back there.
            if (mainCtrl.getDraftRecipe() != null) {
                mainCtrl.showCreateRecipe();
                // the initialize() method in CreateRecipeCtrl will pick up the draft
                // and reload the food list (finding the new item).
            } else if (cameFromShoppingList) {
                mainCtrl.showShoppingListDialogBox(ingredientName);
            }else if (cameFromAddRecipeToShoppingList) {
                mainCtrl.showAddRecipeToShoppingListDialogBox(ingredientName);
            }
            else{
                // otherwise, go to the Food Item Management screen
                mainCtrl.showFoodItemMain();
            }

        } catch (NumberFormatException e) {
            Alert invalidInput = new Alert(Alert.AlertType.WARNING);
            invalidInput.setTitle("Error");
            invalidInput.setHeaderText("Invalid Input");
            invalidInput.setContentText("Please enter a valid number as shown in the examples.");
            invalidInput.showAndWait();
        }
    }


    @FXML
    public void cancelChanges() {
        // 1. Reset the UI state
        editingItem = null;
        foodItemName.clear();
        kcalValue.clear();
        carbsValue.clear();
        proteinsValue.clear();
        fatsValue.clear();

        confirmButton.setText("Confirm");
        sceneTitle.setText("Adding Food Item");

        // if a draft exists, we go back to the Create Recipe screen.
        // if not, we go back to the Food Item list.
        if (mainCtrl.getDraftRecipe() != null) {
            mainCtrl.showCreateRecipe();
        } else {
            mainCtrl.showFoodItemMain();
        }
    }


    public void setEditingItem(FoodItem item){
        this.editingItem = item;

        foodItemName.setText(item.getName());
        kcalValue.setText(String.valueOf(item.getKcal()));
        carbsValue.setText(String.valueOf(item.getCarbs()));
        proteinsValue.setText(String.valueOf(item.getProteins()));
        fatsValue.setText(String.valueOf(item.getFats()));

        confirmButton.setText("Save");
        sceneTitle.setText("Editing Food Item");
    }

    /**
     * Sets up the label for FoodItem name
     * Also confirms it is accessed from Shopping List
     * @param foodItemString - the String entered from the user
     */
    public void accessFromTheShoppingList (String foodItemString){
        cameFromShoppingList = true;
        ingredientName = foodItemString;
        if (foodItemName != null) {
            foodItemName.setText(ingredientName);
        }
    }

    /**
     * Sets up the label for FoodItem name
     * Also confirms it is accessed from Add Recipe To Shopping List
     * @param foodItemString - the String entered from the user
     */
    public void accessFromAddRecipeToTheShoppingList(String foodItemString) {
        cameFromAddRecipeToShoppingList = true;
        ingredientName = foodItemString;
        if (foodItemName != null) {
            foodItemName.setText(ingredientName);
        }
    }


}
