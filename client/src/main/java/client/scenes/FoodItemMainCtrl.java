package client.scenes;

import client.network.WebSocketService;
import client.utils.ServerUtils;
import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.messaging.simp.stomp.StompSession;
import recipes.FoodItem;

import java.util.List;
import java.util.Optional;

public class FoodItemMainCtrl {

    private FoodItem currentlySelected;

    private ObservableList<FoodItem> allFoodItems;

    private Long lastSubscribedID;

    private StompSession.Subscription foodItemSubscription;

    @FXML
    public ListView<FoodItem> foodItemListView;
    @FXML
    public Label foodNameLabel;
    @FXML
    public Label nutritionLabel;
    @FXML
    public Label kcalLabel;
    @FXML
    public Label carbsLabel;
    @FXML
    public Label proteinsLabel;
    @FXML
    public Label fatsLabel;
    @FXML
    private Button mainMenuButton;
    @FXML
    public Label recipeUsages;

    private final MainCtrl mainCtrl;
    private final ServerUtils serverUtils;
    private final WebSocketService webSocketService;

    @Inject
    public FoodItemMainCtrl(MainCtrl mainCtrl, ServerUtils serverUtils, WebSocketService webSocketService) {
        this.mainCtrl = mainCtrl;
        this.serverUtils = serverUtils;
        this.webSocketService = webSocketService;
    }

    @FXML
    public void initialize() {

        allFoodItems = webSocketService.getMasterFoodItemsList();

        allFoodItems.addListener((ListChangeListener <FoodItem>) c -> {
            Platform.runLater(this::updateListView);
        });
/**
        allFoodItems.addListener((ListChangeListener<FoodItem>) c -> {
            Platform.runLater(() -> {
                currentlySelected = foodItemListView.getSelectionModel().getSelectedItem();
                if (currentlySelected != null) {
                    allFoodItems.stream().
                            filter(f -> f.getId().equals(currentlySelected.getId()))
                            .findFirst()
                            .ifPresent(this::updateFoodDetails);
                }
            });
        });
**/
        try {
            loadFoodItems();
        } catch (Exception e) {
            e.printStackTrace();
        }

        foodItemListView.getSelectionModel().selectedItemProperty().
                addListener((obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        currentlySelected = newVal;
                        selectFoodItem(newVal);
                    }
                });

        /*
        Here, the behaviour of how each cell is shown in the UI is overridden as we only want to show the name.
        First super.updateItem is called so nothing else goes wrong.
        AI was used for help here
         */
        foodItemListView.setCellFactory(param -> new ListCell<FoodItem>() {
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
        updateFoodDetails(null);
    }

    /**
     * Updates the selectedFoodItem and subscribes to changes
     * @param foodItem - the FoodItem to be selected
     */
    public void selectFoodItem (FoodItem foodItem) {
        updateFoodDetails(foodItem);

        // If we are currently viewing another foodItem, unsubscribe from it
        if (lastSubscribedID == null || !lastSubscribedID.equals(currentlySelected.getId())) {
            unsubscribeFromFoodItem();
        }

        foodItemSubscription = webSocketService.
                subscribeToFoodItem(currentlySelected.getId(), updated -> {
                    Platform.runLater(() -> {
                        updateFoodDetails(updated);
                        for (int i = 0; i < allFoodItems.size(); i++) {
                            if (allFoodItems.get(i).getId().equals(updated.getId())) {
                                allFoodItems.set(i, updated);
                                break;
                            }
                        }
                    });
                });
        lastSubscribedID = currentlySelected.getId();
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
            if (item.getId() == null || item.getId() < 0) {
                recipeUsages.setText("Used in 0 recipes");
            }else{
                recipeUsages.setText("Used in " + serverUtils.getFoodItemRecipeUsage(item) + " recipe(s)");
            }
            nutritionLabel.setText("Nutritional values per 100g:");
            kcalLabel.setText("Kcal: " + item.getKcal());
            carbsLabel.setText("Carbohydrates: " + item.getCarbs());
            proteinsLabel.setText("Proteins: " + item.getProteins());
            fatsLabel.setText("Fats: " + item.getFats());
        }
    }

    public void updateListView () {
        // Only show all recipes if we are in "All Recipes" tab
        // Updates the sidebar when it senses changes in the database
        foodItemListView.getItems().setAll(allFoodItems);

        // Updates the recipe with all of its parameters
        if (currentlySelected != null) {
            allFoodItems.stream()
                    .filter(f -> f.getId().equals(currentlySelected.getId()))
                    .findFirst()
                    .ifPresent(updated -> {
                        this.currentlySelected = updated;
                        foodNameLabel.setText(updated.getName());
                    });
        }

    }

    @FXML
    public void addFoodItem() {
        mainCtrl.showAddFoodItem();
    }

    @FXML
    public void deleteFoodItem() {
        FoodItem selectedFoodItem = foodItemListView.getSelectionModel().getSelectedItem();
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
                    serverUtils.deleteFoodItem(selectedFoodItem.getId());
                    unsubscribeFromFoodItem();
                }
            } else {
                serverUtils.deleteFoodItem(selectedFoodItem.getId());
                unsubscribeFromFoodItem();
            }
        }

    }

    @FXML
    public void editFoodItem() {
        FoodItem selectedItem = foodItemListView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            Alert noSelection = new Alert(Alert.AlertType.WARNING);
            noSelection.setTitle("Nothing Selected");
            noSelection.setHeaderText("No food item was selected");
            noSelection.setContentText("Please select a food item before editing.");
            noSelection.showAndWait();
            return;
        }
        mainCtrl.handleFoodItemBeingUpdated(selectedItem);
        mainCtrl.showAddFoodItem();
        mainCtrl.getAddFoodItemCtrl().setEditingItem(selectedItem);
    }

    @FXML
    public void showMainUI() {
        mainCtrl.showMainMenu();
    }

    /**
     * Initially loads all FoodItems
     */
    public void loadFoodItems () {
        List<FoodItem> allFoodItems = serverUtils.getAllFoodItems();
        Platform.runLater(() -> webSocketService.
                getMasterFoodItemsList().setAll(allFoodItems));
    }

    @FXML
    public void refreshFoodItems(){
        List<FoodItem> allFoodItems = serverUtils.getAllFoodItems();
        ObservableList<FoodItem> items = FXCollections.observableArrayList(allFoodItems);
        foodItemListView.setItems(items);
    }

    /**
     * Handles unsubscribing from the former foodItem instance
     */
    public void unsubscribeFromFoodItem () {
        if (this.foodItemSubscription!= null) {
            foodItemSubscription.unsubscribe();
            foodItemSubscription = null;
        }
    }

}
