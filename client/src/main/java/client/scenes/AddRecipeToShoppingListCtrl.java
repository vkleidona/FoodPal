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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import recipes.FoodItem;
import recipes.Ingredient;
import recipes.Recipe;
import shopping.ShoppingList;
import client.i18n.LanguageManager;
import java.util.Locale;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AddRecipeToShoppingListCtrl {

    @FXML
    private Button backButton;

    @FXML
    private Button confirmButton;

    @FXML
    private MenuButton languageButton;

    @FXML
    private Button plusButton;

    @FXML
    private ListView <Ingredient> recipeIngredientsListView;
    private ObservableList <Ingredient> recipeIngredientsObservableList;

    private ServerUtils serverUtils;
    private MainCtrl mainCtrl;

    private Recipe recipeToAdd;

    private ShoppingList shoppingList;

    private Ingredient currentIngredient;
    private int selectedIndex;

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

        setupLanguageMenu();
    }

    private MenuItem setupMenuItemIcons (String name, String langCode, String iconFileName) {
        MenuItem menuItem = new MenuItem(name);

        Image flagImage = new Image
                (getClass().getResourceAsStream("/flags/" + iconFileName + "-flag.jpg"));

        ImageView icon = new ImageView(flagImage);
        icon.setFitWidth(16);
        icon.setFitHeight(16);

        Circle clip = new Circle(8, 8, 8);
        icon.setClip(clip);

        menuItem.setGraphic(icon);

        menuItem.setOnAction(e -> changeLanguage(new Locale(langCode)));

        return menuItem;
    }

    private void updateButtonGraphic(String iconFileName) {
        Image img = new Image
                (getClass().getResourceAsStream("/flags/" + iconFileName + "-flag.jpg"));
        ImageView imageView = new ImageView(img);

        imageView.setFitWidth(16);
        imageView.setFitHeight(16);

        Circle clip = new Circle(8, 8, 8);
        imageView.setClip(clip);

        // Set the graphic to the MenuButton itself
        languageButton.setGraphic(imageView);
    }

    private void setupLanguageMenu() {
        languageButton.getItems().clear();

        MenuItem enItem = setupMenuItemIcons("English", "en", "english");
        MenuItem nlItem = setupMenuItemIcons("Nederlands", "nl", "dutch");
        MenuItem roItem = setupMenuItemIcons("Română", "ro", "romanian");
        MenuItem bgItem = setupMenuItemIcons("Български", "bg", "bulgarian");

        languageButton.getItems().addAll(enItem, nlItem, roItem, bgItem);

        Locale current = LanguageManager.getLocale();



        switch (current.getLanguage()) {
            case "nl" -> {
                languageButton.setText("Nederlands");
                updateButtonGraphic("dutch");
            }
            case "ro" -> {
                languageButton.setText("Română");
                updateButtonGraphic("romanian");
            }
            case "bg" -> {
                languageButton.setText("Български");
                updateButtonGraphic("bulgarian");
            }
            default -> {
                languageButton.setText("English");
                updateButtonGraphic("english");
            }
        }
    }

    private void changeLanguage(Locale newLocale) {
        LanguageManager.setLocale(newLocale);
        switch (newLocale.getLanguage()) {
            case "nl" -> languageButton.setText("Nederlands");
            case "ro" -> languageButton.setText("Română");
            case "bg" -> languageButton.setText("Български");
            default -> languageButton.setText("English");
        }
        mainCtrl.reloadScenesForNewLanguage();
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
                FoodItem foodItem = serverUtils.getFoodItemByName(input);

                // If the FoodItem hasn't been found, show an alert
                if (foodItem == null) {
                    if (ingredient == null) {

                    }else{
                        askToAddFoodItem(ingredient, input);
                    }
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
        if (!addingNewFoodItem) {
            selectedIndex = indexClicked;
        }

        dialogBox.showAndWait().ifPresent(result -> {
            if (editing) {
                if (indexClicked >= 0) {
                    recipeIngredientsObservableList.set(indexClicked, ingredient);
                }
                if (indexClicked == -1) {
                    recipeIngredientsObservableList.set(selectedIndex, ingredient);
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
        Optional<ButtonType> buttonType = dialogBox.showAndWait();
        if (buttonType.isPresent() && buttonType.get() == ButtonType.OK) {
            if (ingredient == null) {
                mainCtrl.showAddFoodItemFromAddRecipeToShoppingList(foodItemEntry);
            }else{
                saveCurrentIngredient(ingredient, foodItemEntry);
            }
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
        mainCtrl.showAddFoodItemFromAddRecipeToShoppingList(foodItemEntry);
    }

    /**
     * Restores the ingredient
     * (and sets the name to the new food item which was added to the database)
     * @param ingredientName - the new Food Item added to the database
     */
    public void restoreIngredient(String ingredientName) {
        currentIngredient = new Ingredient(0.0, "g");
        currentIngredient.setFoodType(serverUtils.getFoodItemByName(ingredientName));
        mainCtrl.showAddRecipeToShoppingList();
        showDialog(currentIngredient);
    }
}
