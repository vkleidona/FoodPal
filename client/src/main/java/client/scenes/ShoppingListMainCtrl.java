package client.scenes;

import client.utils.ServerUtils;
import jakarta.inject.Inject;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Pair;
import recipes.FoodItem;
import recipes.Ingredient;
import shopping.ShoppingList;

import java.util.*;


public class ShoppingListMainCtrl {

    @FXML
    private Button resetButton;

    @FXML
    private Button returnToMainButton;

    @FXML
    private Button printButton;

    @FXML
    private Button addIngredientButton;

    @FXML
    private ListView <Ingredient> ingredientsListView;
    private ObservableList<Ingredient> ingredientsObservableList;

    private ShoppingList s;

    private final ServerUtils server;
    private final MainCtrl mainCtrl;

    @Inject
    public ShoppingListMainCtrl (ServerUtils server, MainCtrl mainCtrl) {
        this.server = server;
        this.mainCtrl = mainCtrl;
    }

    public void initialize() {
        s = ShoppingList.getShoppingList();
        List <Ingredient> ingredientList = s.getIngredients();
        if (ingredientList==null) {
           ingredientList = new ArrayList<>();
        }
        ingredientsObservableList = FXCollections.observableArrayList(ingredientList);
        ingredientsListView.setItems(ingredientsObservableList);
    }

    @FXML
    public void reset () {
        ingredientsListView.getItems().clear();
    }

    @FXML
    public void print () {

    }

    /**
     * Responsible for directly adding ingredients to the ShoppingList
     */
    @FXML
    public void addIngredient () {
        Dialog <Ingredient> dialogBox = new Dialog<>();
        dialogBox.setTitle("Add ingredient");


        //TODO: Replace with permanent custom buttons
        dialogBox.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        TextField quantityField = new TextField();
        quantityField.setPromptText("Enter quantity:");

        TextField foodItemField = new TextField();
        foodItemField.setPromptText("Enter food type:");

        //TODO: Configure for new enum
        TextField unitField = new TextField();
        unitField.setPromptText("Enter unit:");

        HBox ingredientInput = new HBox();
        ingredientInput.setSpacing(15);
        ingredientInput.setPadding(new Insets(20));
        ingredientInput.getChildren().addAll(foodItemField, quantityField, unitField);
        dialogBox.getDialogPane().setContent(ingredientInput);

        dialogBox.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                String enteredFoodType = foodItemField.getText().trim();
                FoodItem foodItem = server.searchForFoodItem(enteredFoodType);

                //TODO: Error management
                double quantity = Double.parseDouble(quantityField.getText());

                String measurementType = unitField.getText();
                return new Ingredient(foodItem, quantity, measurementType);
            }
            return null;
        });

        dialogBox.showAndWait().ifPresent(result -> {
            if (result != null) {
                s.addIngredient(result);
                ingredientsObservableList.add(result);
            }else{
                mainCtrl.showMainMenu();
            }
        });

    }

    /**
     * Returns to the MainUI menu
     */
    @FXML
    public void returnToMain () {
        mainCtrl.showMainMenu();
    }

    /**
     * Sends a request to the server
     */
    public void searchForFoodItem () {

    }
}
