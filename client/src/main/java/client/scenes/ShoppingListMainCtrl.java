package client.scenes;

import client.utils.ServerUtils;
import jakarta.inject.Inject;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
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

    /**
     * Initializes all data for the ShoppingList
     */
    public void initialize() {
        s = ShoppingList.getShoppingList();
        s.loadFile();

        List<Ingredient> ingredientList = s.getIngredients();
        if (ingredientList == null) {
            s.setIngredients(new ArrayList<>());
            ingredientList = s.getIngredients();
        }
        if (ingredientsObservableList == null) {
            ingredientsObservableList = FXCollections.observableArrayList(ingredientList);
            ingredientsListView.setItems(ingredientsObservableList);
        }

        ingredientsObservableList.clear();
        ingredientsObservableList.addAll(s.getIngredients());

        ingredientsListView.setCellFactory(view -> new ListCell<>() {
            @Override
            protected void updateItem(Ingredient ingredient, boolean empty) {
                super.updateItem(ingredient, empty);

                if (empty || ingredient == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label nameLabel = new Label(ingredient.toString());

                    Button editButton = new Button("Edit");
                    Button deleteButton = new Button("Delete");

                    editButton.setOnAction(e -> showDialog(ingredient));
                    deleteButton.setOnAction(e -> deleteIngredient(ingredient));

                    HBox container = new HBox(10, nameLabel, editButton, deleteButton);
                    container.setAlignment(Pos.CENTER_LEFT);

                    setGraphic(container);
                }
            }
        });
    }

    @FXML
    public void reset () {
        s.getIngredients().clear();
        ingredientsObservableList.clear();
    }

    @FXML
    public void print () {

    }

    /**
     * Responsible for directly adding ingredients to the ShoppingList
     */
    @FXML
    public void addIngredient () {
        showDialog(null);
    }

    /**
     * Returns to the MainUI menu
     */
    @FXML
    public void returnToMain () {
        mainCtrl.showMainMenu();
    }

    /**
     * Adds new ingredient to the list, as well as the options to remove / edit it.
     * @param ingredient - Ingredient to be added
     */
    public void addIngredientToShoppingList (Ingredient ingredient) {
        ingredientsObservableList.add(ingredient);
        ShoppingList.getShoppingList().addIngredient(ingredient);
    }

    /**
     * Handles the functionality for adding / changing an ingredient
     * depending on the button clicked
     * @param ingredient - Ingredient to be edited
     */
    public void showDialog(Ingredient ingredient) {
        boolean editing = !(ingredient==null);
        Dialog <Ingredient> dialogBox = new Dialog<>();
        dialogBox.setTitle("Change ingredient");

        dialogBox.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Button okButton = (Button) dialogBox.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);

        TextField foodItemField = new TextField();
        TextField quantityField = new TextField();
        TextField unitField = new TextField();

        if (!editing) {
            foodItemField.setPromptText("Enter food type:");
            quantityField.setPromptText("Enter quantity:");
            unitField.setPromptText("Enter unit field:");
        }else{
            foodItemField.setText(ingredient.getFoodName());
            quantityField.setText(String.valueOf(ingredient.getQuantity()));
            //TODO: Configure validation for new enum
            unitField.setText(ingredient.getMeasurement());
        }

        HBox ingredientInput = new HBox();
        ingredientInput.setSpacing(15);
        ingredientInput.setPadding(new Insets(20));
        ingredientInput.getChildren().addAll(foodItemField, quantityField, unitField);
        dialogBox.getDialogPane().setContent(ingredientInput);

        // Disables the OK Button until all items have been entered
        ChangeListener <String> inputValidator = (obs, oldVal, newVal) -> {
            String foodText = foodItemField.getText().trim();
            String quantityText = quantityField.getText().trim();
            String measurementText = unitField.getText().trim();
            boolean condition = !foodText.isEmpty() && !quantityText.isEmpty()
                    && !measurementText.isEmpty();

            okButton.setDisable(!condition);
        };

        foodItemField.textProperty().addListener(inputValidator);
        quantityField.textProperty().addListener(inputValidator);
        unitField.textProperty().addListener(inputValidator);

        dialogBox.setResultConverter(button -> {
            if (button == ButtonType.OK) {

                String input = foodItemField.getText().trim();
                FoodItem foodItem = server.searchForFoodItem(input);

                if (foodItem == null) {
                    Alert nonExistentItemAlert = new Alert(Alert.AlertType.ERROR);
                    nonExistentItemAlert.setTitle("Food item not found.");
                    nonExistentItemAlert.
                            setContentText("Food Item does not exist in the database.");
                    nonExistentItemAlert.showAndWait();
                    return null;
                }

                double quantity = 0;
                try {
                    quantity = Double.parseDouble(quantityField.getText());
                }catch (NumberFormatException e) {
                    Alert wrongNumberInput = new Alert(Alert.AlertType.ERROR);
                    wrongNumberInput.setTitle("Wrong input");
                    wrongNumberInput.setContentText("Food Item does not exist in the database.");
                    wrongNumberInput.showAndWait();
                    return null;
                }

                String measurementType = unitField.getText();
                if (editing) {
                    ingredient.setFoodType(foodItem);
                    ingredient.setQuantity(quantity);
                    ingredient.setMeasurement(measurementType);
                    return ingredient;
                }
                return new Ingredient(foodItem, quantity, measurementType);
            }
            return null;
        });

        int indexClicked = ingredientsObservableList.indexOf(ingredient);

        dialogBox.showAndWait().ifPresent(result -> {
            if (editing) {
                if (indexClicked >= 0) {
                    ingredientsObservableList.set(indexClicked, ingredient);
                }
            }else{
                addIngredientToShoppingList(result);
            }
        });
    }

    /**
     * Deletes ingredient from the ShoppingList
     * @param ingredient - Ingredient to be deleted from the shopping list
     */
    public void deleteIngredient(Ingredient ingredient) {
        s.removeIngredient(ingredient);
        ingredientsObservableList.remove(ingredient);
    }
}
