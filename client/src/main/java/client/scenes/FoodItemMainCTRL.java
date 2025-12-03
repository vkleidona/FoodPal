package client.scenes;

import client.utils.ServerUtils;
import jakarta.inject.Inject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import recipes.FoodItem;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class FoodItemMainCTRL {

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

    private MainCtrl mainCtrl;
    private final ServerUtils serverUtils = new ServerUtils();

    @Inject
    public FoodItemMainCTRL(MainCtrl mainCtrl) {
        this.mainCtrl = mainCtrl;
    }

    @FXML
    public void initialize() {
        /*
        Making the listViewer populated with some food items for testing purposes.
        This should be changed later with the actual food items in the database.
        */
        refreshFoodItems();
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
            nutritionLabel.setText("");
            kcalLabel.setText("");
            carbsLabel.setText("");
            proteinsLabel.setText("");
            fatsLabel.setText("");
        } else {
            foodNameLabel.setText("Food Name: " + item.getName());
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
            serverUtils.deleteFoodItem(selectedFoodItem.getName());
            refreshFoodItems();
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
