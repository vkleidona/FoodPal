package client.scenes.shopping_list;

import client.service.ShoppingListService;
import client.utils.MainCtrl;
import client.service.LanguageService;
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
import client.configuration.LanguageManager;

import java.util.*;

import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;

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
    private ResourceBundle resources;

    @FXML
    private ListView <Ingredient> recipeIngredientsListView;
    private ObservableList <Ingredient> recipeIngredientsObservableList;

    private final ServerUtils serverUtils;
    private final MainCtrl mainCtrl;
    private final LanguageService languageService;

    private Recipe recipeToAdd;

    private final ShoppingListService shoppingListService;

    /**
     * Constructor for the AddRecipeToShoppingList controller class
     * @param serverUtils - the single instance of server utilities
     * @param mainCtrl - the main controller class
     * @param shoppingListService - the instance of the ShoppingList service, injected
     * @param languageService - the instance of the language service class
     */
    @Inject
    public AddRecipeToShoppingListCtrl(ServerUtils serverUtils, MainCtrl mainCtrl,
                                       ShoppingListService shoppingListService,
                                       LanguageService languageService) {
        this.serverUtils = serverUtils;
        this.mainCtrl = mainCtrl;
        this.shoppingListService = shoppingListService;
        this.languageService = languageService;
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
        recipeIngredientsObservableList.forEach(i -> i.setRecipe(recipeToAdd));
        recipeIngredientsObservableList.forEach(i -> i.setRecipeName(recipeToAdd.getName()));

        recipeIngredientsListView.setItems(recipeIngredientsObservableList);
    }

    /**
     * Loads the cell factory for the ingredients
     */
    public void initialize () {
        recipeIngredientsListView.setItems(recipeIngredientsObservableList);

        recipeIngredientsListView.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(Ingredient ingredient, boolean empty) {
                super.updateItem(ingredient, empty);

                if (empty || ingredient == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label nameLabel = new Label(ingredientFormatter(ingredient));

                    Button editButton = new Button("Edit");
                    Button deleteButton = new Button("Delete");

                    editButton.setOnAction(_ -> showDialog(ingredient, false));
                    deleteButton.setOnAction(_ -> deleteIngredient(ingredient));

                    HBox container = new HBox(10, nameLabel, editButton, deleteButton);
                    container.setAlignment(Pos.CENTER_LEFT);

                    setGraphic(container);
                }
            }
        });
        setupLanguageMenu();
    }


    /**
     * A method which displays the proper visualization of the ingredient
     * @param ingredient - the Ingredient currently being visualized
     * @return String - the correct addition to the ShoppingList
     */
    public String ingredientFormatter (Ingredient ingredient) {
        Locale currentLocale = LanguageManager.getCurrentLocale();
        String sourceName = "";
        if (ingredient.getRecipeName() != null) {
            sourceName = ingredient.getRecipeName();
        }
        return ingredient + languageService.ingredientFormat(sourceName,
                currentLocale.getLanguage());

    }

    /**
     * A method which updates the UI of the language button
     * @param locale - the locale of the language
     */
    public void updateLanguageButtonUI (Locale locale) {
        String langCode = locale.getLanguage();

        languageButton.setText(languageService.getDisplayName(langCode));
        languageButton.setGraphic(languageService.renderIcon(langCode, 16.0));
    }


    /**
     * A method which sets up the icons for the language menu
     * @param name - the name of the language we are adding
     * @param langCode - the locale code of the language
     * @return MenuItem - returns the ready-made MenuItem with the proper icon
     */
    public MenuItem setupMenuItemIcons (String name, String langCode) {
        MenuItem menuItem = new MenuItem(name);
        menuItem.setGraphic(languageService.renderIcon(langCode, 16.0));
        menuItem.setOnAction(_ -> changeLanguage(Locale.of(langCode)));
        return menuItem;
    }

    /**
     * A setup method for the language menu -
     * adds the constructed MenuItems using the helper methods with the appropriate icon
     */
    public void setupLanguageMenu() {
        languageButton.getItems().clear();

        List<String> codes = List.of("en", "nl", "bg", "ro", "sk", "de");

        for (String code : codes) {
            String displayName = languageService.getDisplayName(code);
            languageButton.getItems().add(setupMenuItemIcons(displayName, code));
        }

        updateLanguageButtonUI(LanguageManager.getCurrentLocale());
    }

    /**
     * A method for changing the language of the menus to a new Locale
     * @param newLocale - the new language we want to set the menus to
     */
    public void changeLanguage(Locale newLocale) {
        LanguageManager.setLocale(newLocale);
        updateLanguageButtonUI(newLocale);
        mainCtrl.reloadScenesForNewLanguage();
        recipeIngredientsListView.setItems(recipeIngredientsObservableList);
    }



    /**
     * Adds new ingredient to the list, as well as the options to remove / edit it.
     * @param ingredient - Ingredient to be added
     */
    public void addIngredientToShoppingList (Ingredient ingredient) {
        recipeIngredientsObservableList.add(ingredient);
    }

    /**
     * Handles the propagation of an ingredient to the Shopping List
     * @param ingredient - the ingredient to be displayed
     */
    public void handleIngredientEdit (Ingredient ingredient) {

        int index = recipeIngredientsObservableList.indexOf(ingredient);

        if (index != -1) {
            recipeIngredientsObservableList.set(index, ingredient);
        } else {
            int selected = recipeIngredientsListView.getSelectionModel().getSelectedIndex();
            if (selected >= 0) {
                recipeIngredientsObservableList.set(selected, ingredient);
            }else{
                addIngredientToShoppingList(ingredient);
            }
        }
        shoppingListService.saveShoppingList();
    }

    /**
     * Handles the functionality for adding / changing an ingredient
     * depending on the button clicked
     * @param ingredient - Ingredient to be edited
     * @param addedFoodItem - a boolean whether a food item has been added
     */
    public void showDialog(Ingredient ingredient, boolean addedFoodItem) {
        boolean isEditing = (ingredient != null);

        Dialog<Ingredient> dialogBox = setupIngredientDialog(ingredient);

        dialogBox.showAndWait().ifPresent(result -> {
            if (isEditing && !addedFoodItem) {
                handleIngredientEdit(result);
            } else {
                addIngredientToShoppingList(result);
            }
        });
    }

    /**
     * A helper method for constructing a dialog box
     * @param ingredient - the ingredient which the dialog box returns
     * @return Dialog <Ingredient> - a ready-made dialog box
     */
    public Dialog<Ingredient> setupIngredientDialog(Ingredient ingredient) {
        boolean editing = (ingredient != null);
        Dialog<Ingredient> dialog = new Dialog<>();
        dialog.setTitle(resources.getString("shopping.dialog.change.title"));
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ComboBox<String> foodItemField = new ComboBox<>(FXCollections.observableArrayList(
                serverUtils.getAllFoodItems().stream().map(FoodItem::getName).toList()));
        foodItemField.setEditable(true);

        TextField quantityField = new TextField();
        ChoiceBox<String> unitChoice = new ChoiceBox<>();
        shoppingListService.loadItemsIntoChoiceBox(unitChoice);

        if (editing) {
            foodItemField.setValue(ingredient.getFoodName());
            quantityField.setText(String.valueOf(ingredient.getQuantity()));
            unitChoice.setValue(ingredient.getMeasurement());
        }

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(!editing);

        setupValidator(okButton, foodItemField, quantityField, unitChoice);

        HBox container = new HBox(15, foodItemField, quantityField, unitChoice);
        container.setPadding(new Insets(20));
        dialog.getDialogPane().setContent(container);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                return processDialogResult(foodItemField, quantityField, unitChoice, ingredient);
            }
            return null;
        });

        return dialog;
    }

    private void setupValidator(Button ok, ComboBox<String> food, TextField qty,
                                ChoiceBox<String> unit) {
        ChangeListener<Object> validator = (_, _, _) -> {
            boolean empty = food.getValue() == null ||
                    qty.getText().isEmpty() || unit.getValue() == null;
            ok.setDisable(empty);
        };
        food.valueProperty().addListener(validator);
        qty.textProperty().addListener(validator);
        unit.valueProperty().addListener(validator);
    }

    private Ingredient processDialogResult(ComboBox<String> food, TextField qty,
                                           ChoiceBox<String> unit, Ingredient existing) {
        String inputName = food.getValue().trim();
        FoodItem foodItem = serverUtils.getFoodItemByName(inputName);

        shoppingListService.setQuantity(Double.parseDouble(qty.getText()));
        shoppingListService.setMeasurement(unit.getValue());

        if (foodItem == null) {
            askToAddFoodItem(existing, inputName);
            return null;
        }

        try {
            double quantity = Double.parseDouble(qty.getText());
            if (existing != null) {
                existing.setFoodType(foodItem);
                existing.setQuantity(quantity);
                existing.setMeasurement(unit.getValue());
                return existing;
            }
            return new Ingredient(foodItem, quantity, unit.getValue());
        } catch (NumberFormatException e) {
            Alert wrongNumberInput = new Alert(Alert.AlertType.ERROR);
            wrongNumberInput.setTitle(LanguageManager.getString("alert.decimal.title"));
            wrongNumberInput.setContentText(LanguageManager.getString
                    ("alert.decimal.content"));
            wrongNumberInput.showAndWait();
            return null;
        }
    }


    /**
     * Deletes ingredient from the ShoppingList
     * @param ingredient - Ingredient to be deleted from the shopping list
     */
    public void deleteIngredient(Ingredient ingredient) {
        recipeIngredientsObservableList.remove(ingredient);
    }

    /**
     * Adds a new Ingredient to the temporary list through the + button
     */
    public void addIngredientThroughPlusButton () {
        showDialog(null, false);
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
        for (Ingredient ingredient : recipeIngredientsObservableList) {
            ingredient.setRecipeName(recipeToAdd.getName());
            shoppingListService.getShoppingList().addIngredient(ingredient);
        }
        shoppingListService.saveShoppingList();
        mainCtrl.showShoppingList();
    }

    /**
     * If the user wants to add a Food Item which is not in the database,
     * they get redirected to this method which asks for confirmation to add the food item
     * @param ingredient - the Ingredient to be added as a FoodItem
     * @param foodItemEntry - what the user has entered for food item
     */
    public void askToAddFoodItem (Ingredient ingredient, String foodItemEntry) {
        Dialog<ButtonType> dialogBox = new Dialog<>();
        dialogBox.setTitle(LanguageManager.getString("dialog.foodNotFound.title"));
        dialogBox.setContentText(LanguageManager.getString("dialog.foodNotFound.content"));


        dialogBox.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Optional<ButtonType> buttonType = dialogBox.showAndWait();
        if (buttonType.isPresent() && buttonType.get() == ButtonType.OK) {
            if (ingredient == null) {
                mainCtrl.showAddFoodItemFromAddRecipeToShoppingList(foodItemEntry);
            }
        }else{
            mainCtrl.showAddRecipeToShoppingList();
        }
    }

    /**
     * Restores the ingredient
     * (and sets the name to the new food item which was added to the database)
     * @param ingredientName - the new Food Item added to the database
     */
    public void restoreIngredient(String ingredientName) {
        Ingredient currentIngredient = new Ingredient(shoppingListService.getQuantity(),
                shoppingListService.getMeasurement());
        currentIngredient.setFoodType(serverUtils.getFoodItemByName(ingredientName));
        mainCtrl.showAddRecipeToShoppingList();
        showDialog(currentIngredient, true);
    }
}
