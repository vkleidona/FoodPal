package client.scenes;

import client.utils.ServerUtils;
import jakarta.inject.Inject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import recipes.FoodItem;

import java.util.List;
import java.util.Optional;

public class FoodItemMainCtrl {

    @FXML
    private ListView<FoodItem> foodItemList;
    @FXML
    private Label foodNameLabel;
    @FXML
    private Label nutritionLabel;
    @FXML
    private Label kcalLabel;
    @FXML
    private Label carbsLabel;
    @FXML
    private Label proteinsLabel;
    @FXML
    private Label fatsLabel;
    @FXML
    private Button mainMenuButton;
    @FXML
    private Label recipeUsages;

    private final MainCtrl mainCtrl;
    private final ServerUtils serverUtils;

    @Inject
    public FoodItemMainCtrl(MainCtrl mainCtrl, ServerUtils serverUtils) {
        this.mainCtrl = mainCtrl;
        this.serverUtils = serverUtils;
    }

    @FXML
    public void initialize() {
        try {
            refreshFoodItems();
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*
        Here, the behaviour of how each cell is shown in the UI is overridden as we only want to show the name.
        First super.updateItem is called so nothing else goes wrong.
        AI was used for help here
         */
        foodItemList.setCellFactory(param -> new ListCell<FoodItem>() {
            @Override
            protected void updateItem(FoodItem item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });

        foodItemList.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue)
                        -> updateFoodDetails(newValue));

        updateFoodDetails(null);
    }

    @FXML
    public void updateFoodDetails(FoodItem item) {
        if (item == null){
            foodNameLabel.setText("Select food item to see details");
            recipeUsages.setText("");
            nutritionLabel.setText("");
            kcalLabel.setText("");
            carbsLabel.setText("");
            proteinsLabel.setText("");
            fatsLabel.setText("");
        } else {
            foodNameLabel.setText("Food Name: " + item.getName());
            recipeUsages.setText("Used in " + serverUtils.getFoodItemRecipeUsage(item) + " recipe(s)");
            nutritionLabel.setText("Nutritional values per 100g:");
            kcalLabel.setText("Kcal: " + item.getKcal());
            carbsLabel.setText("Carbohydrates: " + item.getCarbs());
            proteinsLabel.setText("Proteins: " + item.getProteins());
            fatsLabel.setText("Fats: " + item.getFats());
        }
    }

    @FXML
    public void addFoodItem() {
        mainCtrl.showAddFoodItem();
    }

    @FXML
    public void deleteFoodItem() {
        FoodItem selectedFoodItem = foodItemList.getSelectionModel().getSelectedItem();
        int recipeUsages = serverUtils.getFoodItemRecipeUsage(selectedFoodItem);

        if(selectedFoodItem == null) {
            Alert noSelection = new Alert(Alert.AlertType.WARNING);
            noSelection.setTitle("Nothing Selected");
            noSelection.setHeaderText("No food item was selected");
            noSelection.setContentText("Please select a food item before deleting.");
            noSelection.showAndWait();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Deleting Selected Food Item");
        alert.setHeaderText("Are you sure?");
        alert.setContentText("Do you want to delete the item " + selectedFoodItem.getName() + " ?");

        Optional<ButtonType> result = alert.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK){
            if(recipeUsages > 0) {
                Alert alertUsedInRecipe = new Alert(Alert.AlertType.CONFIRMATION);
                alertUsedInRecipe.setTitle("Item Deleted is used in recipe(s)");
                alertUsedInRecipe.setHeaderText("The item you want to delete is used in one or more recipes. Are you really sure you want to delete it?");
                alertUsedInRecipe.setContentText("Do you want to delete the item " + selectedFoodItem.getName() + " which is used in one or more recipes?");

                Optional<ButtonType> result2 = alertUsedInRecipe.showAndWait();
                if(result2.isPresent() && result2.get() == ButtonType.OK){
                    serverUtils.deleteFoodItem(selectedFoodItem.getName());
                    refreshFoodItems();
                }
            } else {
                serverUtils.deleteFoodItem(selectedFoodItem.getName());
                refreshFoodItems();
            }
        }

    }

    @FXML
    public void editFoodItem() {
        FoodItem selectedItem = foodItemList.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            Alert noSelection = new Alert(Alert.AlertType.WARNING);
            noSelection.setTitle("Nothing Selected");
            noSelection.setHeaderText("No food item was selected");
            noSelection.setContentText("Please select a food item before editing.");
            noSelection.showAndWait();
            return;
        }

        mainCtrl.showAddFoodItem();
        mainCtrl.getAddFoodItemCtrl().setEditingItem(selectedItem);
    }

    @FXML
    public void showMainUI() {
        mainCtrl.showMainMenu();
    }

    @FXML
    public void refreshFoodItems(){
        List<FoodItem> allFoodItems = serverUtils.getAllFoodItems();
        ObservableList<FoodItem> items = FXCollections.observableArrayList(allFoodItems);
        foodItemList.setItems(items);
    }

}
