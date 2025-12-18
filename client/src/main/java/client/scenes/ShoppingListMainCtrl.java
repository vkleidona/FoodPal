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

import java.io.FileWriter;
import java.io.IOException;
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

    private Ingredient currentIngredient;
    private int selectedIndex;

    private final ServerUtils server;
    private final MainCtrl mainCtrl;

    /**
     * A constructor which uses dependency injection
     * @param server - an instance of the ServerUtils class with useful methods for
     *               server access
     * @param mainCtrl - an instance of the Main controller which allows the user
     *                 to switch scenes
     */
    @Inject
    public ShoppingListMainCtrl (ServerUtils server, MainCtrl mainCtrl) {
        this.server = server;
        this.mainCtrl = mainCtrl;
    }

    /**
     * A getter for the ShoppingList parameter - needed for access
     * from multiple places
     * @return - ShoppingList - returns the ShoppingList instance
     */
    public ShoppingList getS() {
        return s;
    }

    /**
     * Initializes all data for the ShoppingList
     */
    public void initialize() {
        // loads the shopping list from the file
        s = ShoppingList.getShoppingList();
        s.loadFile();

        // refreshes the interface to match the state of the shopping list
        refreshInterface();

        // sets up a cell factory for the list of ingredients - to display an Edit and Delete button
        ingredientsListView.setCellFactory(view -> new ListCell<>() {
            @Override
            protected void updateItem(Ingredient ingredient, boolean empty) {
                super.updateItem(ingredient, empty);

                if (empty || ingredient == null) {
                    // for empty slots of the list, display nothing
                    setText(null);
                    setGraphic(null);
                } else {
                    // set up a label (containing each Ingredient), and Edit and Delete buttons
                    Label nameLabel = new Label(ingredient.toString());

                    Button editButton = new Button("Edit");
                    Button deleteButton = new Button("Delete");

                    // the edit button shows the dialog box for changing an ingredient
                    editButton.setOnAction(e -> showDialog(ingredient));

                    // the delete button deletes the ingredient
                    deleteButton.setOnAction(e -> deleteIngredient(ingredient));

                    HBox container = new HBox(10, nameLabel, editButton, deleteButton);
                    container.setAlignment(Pos.CENTER_LEFT);

                    setGraphic(container);
                }
            }
        });
    }

    /**
     * Refreshes the interface to match the current state of the Shopping List
     */
    public void refreshInterface () {
        // I added this method, as now the ShoppingList is accessed from 2 places
        // and I need to refresh the interface to display the most recent version
        s.loadFile();
        List<Ingredient> ingredientList = s.getIngredients();

        // initializes the ingredients if uninitialized
        if (ingredientList == null) {
            s.setIngredients(new ArrayList<>());
            ingredientList = s.getIngredients();
        }
        // initializes the observable list if uninitialized
        if (ingredientsObservableList == null) {
            ingredientsObservableList = FXCollections.observableArrayList(ingredientList);
            ingredientsListView.setItems(ingredientsObservableList);
        }

        // clears the observable list and then shows all items
        ingredientsObservableList.clear();
        ingredientsObservableList.addAll(s.getIngredients());
    }

    /**
     * Clears the ShoppingList of Ingredients
     */
    @FXML
    public void reset () {
        s.getIngredients().clear();
        ingredientsObservableList.clear();
    }

    /**
     * Creates a printable version of the ShoppingList
     */
    @FXML
    public void print () throws IOException {
        try (FileWriter writer = new FileWriter("shopping_list.txt")) {
            for (Ingredient ingredient : ingredientsObservableList) {
                writer.write(ingredient.toString());
                writer.write("\n");
            }
        }
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
        s.storeFile();
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
        boolean addingNewFoodItem = !(currentIngredient == null);
        Dialog <Ingredient> dialogBox = new Dialog<>();
        dialogBox.setTitle("Change ingredient");

        // Adds an OK button and a Cancel button to the dialog box
        dialogBox.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Button okButton = (Button) dialogBox.getDialogPane().lookupButton(ButtonType.OK);

        // The OK Button is initially disabled
        okButton.setDisable(true);

        if (addingNewFoodItem) {
            okButton.setDisable(false);
        }

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

        // Disables the OK Button until all items have been entered
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
                FoodItem foodItem = server.searchForFoodItem(input);

                // If the FoodItem hasn't been found, show an alert
                if (foodItem == null) {
                    askToAddFoodItem(ingredient, input);
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
        int indexClicked = ingredientsObservableList.indexOf(ingredient);
        if (!addingNewFoodItem) {
            selectedIndex = indexClicked;
        }

        dialogBox.showAndWait().ifPresent(result -> {
            if (editing) {
                if (indexClicked >= 0) {
                    ingredientsObservableList.set(indexClicked, ingredient);
                    s.storeFile();
                    refreshInterface();
                }
                if (indexClicked == -1) {
                    ingredientsObservableList.set(selectedIndex, ingredient);
                    s.setIngredient(selectedIndex, ingredient);
                    s.storeFile();
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

    /**
     * If the user wants to add a Food Item which is not in the database,
     * they get redirected to this method which asks for confirmation to add the food item
     * @param ingredient - the Ingredient to be added as a FoodItem
     * @param foodItemEntry - what the user has entered for food item
     */
    public void askToAddFoodItem (Ingredient ingredient, String foodItemEntry) {
        Dialog <ButtonType> dialogBox = new Dialog<>();
        dialogBox.setTitle("Food Item not found");
        dialogBox.setContentText("""
                The ingredient you are adding is not known to us.
                
                Do you wish to add it?""");

        dialogBox.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Optional <ButtonType> buttonType = dialogBox.showAndWait();
        if (buttonType.isPresent() && buttonType.get() == ButtonType.OK) {
            saveCurrentIngredient(ingredient, foodItemEntry);
        }else{
            mainCtrl.showAddFoodItem();
        }
    }

    /**
     * Saves the current ingredient before jumping to AddFoodItemUI
     * @param ingredient - the ingredient to be saved
     * @param foodItemEntry - the FoodItem to be added
     */
    public void saveCurrentIngredient (Ingredient ingredient, String foodItemEntry) {
        if (ingredient.getMeasurement() == null) {
            ingredient.setMeasurement("");
        }
        currentIngredient = new Ingredient(ingredient.getQuantity(), ingredient.getMeasurement());
        mainCtrl.showAddFoodItem(foodItemEntry);
    }

    /**
     * Restores the ingredient
     * (and sets the name to the new food item which was added to the database)
     * @param ingredientName - the new Food Item added to the database
     */
    public void restoreIngredient(String ingredientName) {
        currentIngredient.setFoodType(server.searchForFoodItem(ingredientName));
        mainCtrl.showShoppingList();
        showDialog(currentIngredient);
    }
}
