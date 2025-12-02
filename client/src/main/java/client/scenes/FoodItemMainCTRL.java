package client.scenes;

import client.utils.ServerUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import recipes.FoodItem;

import java.io.IOException;
import java.util.List;

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

    private MainCtrl mainCtrl = new MainCtrl();
    private final ServerUtils serverUtils = new ServerUtils();

    @FXML
    public void initialize() {
        /*
        Making the listViewer populated with some food items for testing purposes.
        This should be changed later with the actual food items in the database.
        */
        List<FoodItem> allFoodItems = serverUtils.getAllFoodItems();
        ObservableList<FoodItem> items = FXCollections.observableArrayList(allFoodItems);
        foodItemList.setItems(items);

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

        System.out.println("Adding food item");

        // TO DO: Adding the created item to the ListView and Database.
        // Now it just pops up the adding ui
    }

    @FXML
    public void deleteFoodItem() {
        // TO DO: Deleting a food item from the DB and from the ListView
        System.out.println("Deleting food item");
    }

    @FXML
    public void editFoodItem() {
        FoodItem selectedItem = foodItemList.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            System.out.println("No food item selected");
            return;
        }

        System.out.println("editing item");
        // TO DO: Editing the values of the selected item
    }

    @FXML
    public void showMainUI() {
        try {
            // Edit the path of this line to the correct one after the main scene is added
            Parent root = FXMLLoader.load(getClass().getResource("/client/scenes/Main.fxml"));

            // Get current window from the button
            Stage stage = (Stage) mainMenuButton.getScene().getWindow();

            stage.setTitle("Main Menu");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void showAddFoodItemUI() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/client/scenes/AddFoodItem.fxml"));

            Stage stage = (Stage) mainMenuButton.getScene().getWindow();

            stage.setTitle("Main Menu");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
