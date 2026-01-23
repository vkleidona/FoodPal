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
import javafx.stage.Stage;
import recipes.FoodItem;
import recipes.Ingredient;

import client.configuration.LanguageManager;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
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
    private MenuButton languageButton;

    @FXML
    private ResourceBundle resources;

    @FXML
    private ListView <Ingredient> ingredientsListView;

    private final ServerUtils server;
    private final MainCtrl mainCtrl;
    private final LanguageService languageService;
    private final ShoppingListService shoppingListService;

    private boolean editing;

    private int ogIndex;

    /**
     * A constructor which uses dependency injection
     * @param server - an instance of the ServerUtils class with useful methods for
     *               server access
     * @param mainCtrl - an instance of the Main controller which allows the user
     *                 to switch scenes
     * @param shoppingListService - the instance of the ShoppingList service class
     * @param languageService - the instance of the language service class
     */
    @Inject
    public ShoppingListMainCtrl (ServerUtils server, MainCtrl mainCtrl,
                                 ShoppingListService shoppingListService,
                                 LanguageService languageService) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.shoppingListService = shoppingListService;
        this.languageService = languageService;
    }



    /**
     * Initializes all data for the ShoppingList
     */
    public void initialize() {
        // loads the shopping list from the file
        ingredientsListView.setItems(shoppingListService.getIngredientsObservableList());

        // refreshes the interface to match the state of the shopping list
        refreshInterface();

        // sets up a cell factory for the list of ingredients - to display an Edit and Delete button
        ingredientsListView.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(Ingredient ingredient, boolean empty) {
                super.updateItem(ingredient, empty);

                if (empty || ingredient == null) {
                    // for empty slots of the list, display nothing
                    setText(null);
                    setGraphic(null);
                } else {
                    // set up a label (containing each Ingredient), and Edit and Delete buttons
                    Label nameLabel = new Label((getIndex() + 1) + ". " +
                            ingredientFormatter(ingredient));

                    Button editButton = new Button("Edit");
                    Button deleteButton = new Button("Delete");

                    // the edit button shows the dialog box for changing an ingredient
                    editButton.setOnAction(_ -> {
                        showDialog(ingredient);
                        editing = true;
                        ogIndex = indexProperty().get();
                    });

                    // the delete button deletes the ingredient
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
        shoppingListService.reloadList();
        shoppingListService.getIngredientsObservableList().setAll(shoppingListService.
                getShoppingList().getIngredients());
        ingredientsListView.setItems(shoppingListService.getIngredientsObservableList());
    }

    /**
     * A method for refreshing the ingredients
     */
    public void refreshInterface () {
        // I added this method, as now the ShoppingList is accessed from 2 places
        // and I need to refresh the interface to display the most recent version
        shoppingListService.reloadList();
    }

    /**
     * Clears the ShoppingList of Ingredients
     */
    @FXML
    public void reset () {
        shoppingListService.reset();
    }

    /**
     * Creates a printable version of the ShoppingList
     */
    @FXML
    public void print () throws IOException {
        Stage stage = (Stage) addIngredientButton.getScene().getWindow();
        shoppingListService.print(stage);
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
        shoppingListService.saveShoppingList();
        mainCtrl.showMainMenu();
    }

    /**
     * Adds new ingredient to the list, as well as the options to remove / edit it.
     * @param ingredient - Ingredient to be added
     */
    public void addIngredientToShoppingList (Ingredient ingredient) {
        shoppingListService.getIngredientsObservableList().add(ingredient);
        shoppingListService.getShoppingList().addIngredient(ingredient);
    }

    /**
     * Handles the propagation of an ingredient to the Shopping List
     * @param ingredient - the ingredient to be displayed
     */
    public void handleIngredientEdit (Ingredient ingredient) {
        ObservableList<Ingredient> list = shoppingListService.getIngredientsObservableList();

        int index = ogIndex;

        if (index == -1) {
            index = ingredientsListView.getSelectionModel().getSelectedIndex();
        }

        if (index != -1) {
            shoppingListService.getIngredientsObservableList().set(index, ingredient);
        } else {
            addIngredientToShoppingList(ingredient);
        }
        shoppingListService.saveShoppingList();
        editing = false;
    }

    /**
     * Handles the functionality for adding / changing an ingredient
     * depending on the button clicked
     * @param ingredient - Ingredient to be edited
     */
    public void showDialog(Ingredient ingredient) {
        boolean helperEditing = editing;
        editing = (ingredient != null);

        Dialog<Ingredient> dialogBox = setupIngredientDialog(ingredient);

        dialogBox.showAndWait().ifPresent(result -> {
            if (helperEditing) {
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
                server.getAllFoodItems().stream().map(FoodItem::getName).toList()));
        foodItemField.setEditable(true);

        TextField quantityField = new TextField();
        ChoiceBox<String> unitChoice = new ChoiceBox<>();
        shoppingListService.loadItemsIntoChoiceBox(unitChoice);

        if (editing) {
            if (ingredient.getFoodName() != null) {
                foodItemField.setValue(ingredient.getFoodName());
            }
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
        FoodItem foodItem = server.getFoodItemByName(inputName);

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
        shoppingListService.getShoppingList().removeIngredient(ingredient);
        shoppingListService.getIngredientsObservableList().remove(ingredient);
        shoppingListService.saveShoppingList();
    }



    /**
     * If the user wants to add a Food Item which is not in the database,
     * they get redirected to this method which asks for confirmation to add the food item
     * @param ingredient - the Ingredient to be added as a FoodItem
     * @param foodItemEntry - what the user has entered for food item
     */
    public void askToAddFoodItem (Ingredient ingredient, String foodItemEntry) {
        Dialog <ButtonType> dialogBox = new Dialog<>();
        dialogBox.setTitle(resources.getString("shopping.dialog.notFound.title"));
        dialogBox.setContentText(resources.getString("shopping.dialog.notFound.content"));

        dialogBox.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Optional <ButtonType> buttonType = dialogBox.showAndWait();
        if (buttonType.isPresent() && buttonType.get() == ButtonType.OK) {
            mainCtrl.showAddFoodItemFromShoppingList(foodItemEntry);
            mainCtrl.setCameFromShoppingList(true);
        }else{
            mainCtrl.showShoppingList();
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
        currentIngredient.setFoodType(server.getFoodItemByName(ingredientName));
        mainCtrl.showShoppingList();
        showDialog(currentIngredient);
    }
}
