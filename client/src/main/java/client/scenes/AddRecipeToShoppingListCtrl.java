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
import recipes.Recipe;
import shopping.ShoppingList;

import java.util.ArrayList;
import java.util.List;

public class AddRecipeToShoppingListCtrl {

    @FXML
    private Button backButton;

    @FXML
    private Button confirmButton;

    @FXML
    private Button plusButton;

    @FXML
    private ListView <Ingredient> recipeIngredientsListView;
    private ObservableList <Ingredient> recipeIngredientsObservableList;

    private ServerUtils serverUtils;
    private MainCtrl mainCtrl;

    private Recipe recipeToAdd;

    private ShoppingList shoppingList;

    /**
     * Constructor for the AddRecipeToShoppingList controller class
     * @param serverUtils - the single instance of server utilities
     * @param mainCtrl - the main controller class
     */
    @Inject
    public AddRecipeToShoppingListCtrl (ServerUtils serverUtils, MainCtrl mainCtrl) {
        this.serverUtils = serverUtils;
        this.mainCtrl = mainCtrl;
    }

    public void setRecipe (Recipe recipe) {
        recipeToAdd = recipe;
        List <Ingredient> ingredientList;
        if (recipeToAdd.getIngredients() == null) {
            ingredientList = new ArrayList<>();
        }else{
            ingredientList = recipeToAdd.getIngredients();
        }
        recipeIngredientsObservableList = FXCollections.observableArrayList(ingredientList);
        recipeIngredientsListView.setItems(recipeIngredientsObservableList);
    }

    /**
     * Load the cell factory for the ingredients
     */
    public void initialize () {
        recipeIngredientsListView.setCellFactory(view -> new ListCell<>() {
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

    /**
     * Handles the functionality for adding / changing an ingredient
     * depending on the button clicked
     * @param ingredient - Ingredient to be edited
     */
    public void showDialog(Ingredient ingredient) {
        boolean editing = !(ingredient==null);
        Dialog <Ingredient> dialogBox = new Dialog<>();
        dialogBox.setTitle("Change ingredient");

        // Adds an OK button and a Cancel button to the dialog box
        dialogBox.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Button okButton = (Button) dialogBox.getDialogPane().lookupButton(ButtonType.OK);

        // The OK Button is initially disabled
        okButton.setDisable(true);

        // Setting up the three parameters the user has to enter
        TextField foodItemField = new TextField();
        TextField quantityField = new TextField();
        ChoiceBox <String> unitChoice = new ChoiceBox<>();
        loadItemsIntoChoiceBox(unitChoice);

        // If the user is not editing, we show an empty text box with prompts
        // If they are editing, we fetch the ingredient's data
        if (!editing) {
            foodItemField.setPromptText("Enter food type:");
            quantityField.setPromptText("Enter quantity:");
        }else{
            foodItemField.setText(ingredient.getFoodName());
            quantityField.setText(String.valueOf(ingredient.getQuantity()));
            unitChoice.setValue(String.valueOf(ingredient.getMeasurement()));
        }

        // Sets up the horizontal box with all components
        HBox ingredientInput = new HBox();
        ingredientInput.setSpacing(15);
        ingredientInput.setPadding(new Insets(20));
        ingredientInput.getChildren().addAll(foodItemField, quantityField, unitChoice);
        dialogBox.getDialogPane().setContent(ingredientInput);

        ChangeListener <String> inputValidator = (obs, oldVal, newVal) -> {
            String foodText = foodItemField.getText().trim();
            String quantityText = quantityField.getText().trim();
            String measurementType = unitChoice.getValue();
            Double quantity = null;
            try {
                quantity = Double.parseDouble(quantityText);
            } catch (NumberFormatException ignored) {}

            if (!editing) {
                okButton.setDisable(foodText.isEmpty() ||
                        quantityText.isEmpty() || measurementType == null);
            }else{
                boolean disableCondition =
                        (foodText.isEmpty() || foodText.equals(ingredient.getFoodName())) &&
                                (quantityText.isEmpty() || quantity == null ||
                                        quantity == ingredient.getQuantity()) &&
                                (measurementType == null ||
                                        measurementType.equals(ingredient.getMeasurement()));
                okButton.setDisable(disableCondition);
            }
        };

        // we listen to the clicking of any buttons
        foodItemField.textProperty().addListener(inputValidator);
        quantityField.textProperty().addListener(inputValidator);
        unitChoice.valueProperty().addListener(inputValidator);
        dialogBox.setResultConverter(button -> {
            if (button == ButtonType.OK) {

                // Searches the server for the entered FoodItem
                String input = foodItemField.getText().trim();
                FoodItem foodItem = serverUtils.searchForFoodItem(input);

                // If the FoodItem hasn't been found, show an alert
                if (foodItem == null) {
                    Alert nonExistentItemAlert = new Alert(Alert.AlertType.ERROR);
                    nonExistentItemAlert.setTitle("Food item not found.");
                    nonExistentItemAlert.
                            setContentText("Food Item does not exist in the database.");
                    nonExistentItemAlert.showAndWait();
                    return null;
                }

                // Catches non-number inputs in the quantity field
                double quantity;
                try {
                    quantity = Double.parseDouble(quantityField.getText());
                }catch (NumberFormatException e) {
                    Alert wrongNumberInput = new Alert(Alert.AlertType.ERROR);
                    wrongNumberInput.setTitle("Wrong input");
                    wrongNumberInput.setContentText("Invalid input of a decimal number.");
                    wrongNumberInput.showAndWait();
                    return null;
                }

                String measurementType = unitChoice.getValue();
                // If the user is editing, we edit the currently selected ingredient
                if (editing) {
                    ingredient.setFoodType(foodItem);
                    ingredient.setQuantity(quantity);
                    ingredient.setMeasurement(measurementType);
                    return ingredient;
                }
                // Otherwise, a new ingredient with the data is returned
                return new Ingredient(foodItem, quantity, measurementType);
            }
            return null;
        });

        // We edit the ingredients in the list itself
        int indexClicked = recipeIngredientsObservableList.indexOf(ingredient);

        dialogBox.showAndWait().ifPresent(result -> {
            if (editing) {
                if (indexClicked >= 0) {
                    recipeIngredientsObservableList.set(indexClicked, ingredient);
                }
            }else{
                addIngredientToList(result);
            }
        });
    }

    /**
     * Adds an ingredient to the observable list of ingredients
     * @param ingredient - the Ingredient to be added
     */
    public void addIngredientToList (Ingredient ingredient) {
        //TODO: Convert everything into grams
        recipeIngredientsObservableList.add(ingredient);
    }

    /**
     * Deletes an ingredient from the Ingredients observable list
     * @param ingredient - the Ingredient to be deleted
     */
    public void deleteIngredient (Ingredient ingredient) {
        recipeIngredientsObservableList.remove(ingredient);
    }

    /**
     * Adds a new Ingredient to the temporary list through the + button
     */
    public void addIngredientThroughPlusButton () {
        showDialog(null);
    }

    /**
     * Returns the user to the Main UI
     */
    @FXML
    private void returnToMainUI () {
        mainCtrl.showMainMenu();
    }

    /**
     * Adds the ingredients to the Shopping List
     */
    @FXML
    public void confirmAction () {
        shoppingList = ShoppingList.getShoppingList();
        for (Ingredient ingredient : recipeIngredientsObservableList) {
            shoppingList.addIngredient(ingredient);
        }
        shoppingList.storeFile();
        mainCtrl.showShoppingList();
    }

    /**
     * A method for loading all possible measurement types into the choice box
     * @param choiceBox - the choice box where we load the items
     */
    public void loadItemsIntoChoiceBox (ChoiceBox <String> choiceBox) {
        List <String> availableMeasurementTypes =
                List.of("g", "kg", "L", "mL", "oz", "lb", "fl oz", "cup", "tbsp", "tsp",
                        "slice", "clove", "stick", "pinch", "dash", "jar", "-");
        choiceBox.getItems().addAll(availableMeasurementTypes);
    }
}
