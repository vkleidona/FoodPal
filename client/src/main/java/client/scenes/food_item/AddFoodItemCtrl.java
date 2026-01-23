package client.scenes.food_item;

import client.utils.MainCtrl;
import client.service.LanguageService;
import client.utils.ServerUtils;
import jakarta.inject.Inject;
import client.configuration.LanguageManager;

import java.util.List;
import java.util.Locale;

import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.springframework.http.HttpStatus;
import recipes.FoodItem;

public class AddFoodItemCtrl {

    @FXML
    private Label sceneTitle;
    @FXML
    private TextField foodItemName;
    @FXML
    private TextField kcalValue;
    @FXML
    private TextField carbsValue;
    @FXML
    private TextField proteinsValue;
    @FXML
    private TextField fatsValue;
    @FXML
    private Button cancelButton;
    @FXML
    private Button confirmButton;
    @FXML
    private MenuButton languageButton;

    private final MainCtrl mainCtrl;
    private final ServerUtils serverUtils;
    private final LanguageService languageService;

    private FoodItem editingItem;

    private String ingredientName;

    // these booleans indicate where the Add Food Item was accessed from
    private boolean cameFromShoppingList = false;
    private boolean cameFromAddRecipeToShoppingList = false;

    /**
     * A constructor for the AddFoodItemCtrl class using dependency injection
     * @param mainCtrl - the instance of the Main Controller class
     * @param serverUtils - the instance of the Server Utilities
     * @param languageService - the instance of the Language service
     */
    @Inject
    public AddFoodItemCtrl(MainCtrl mainCtrl, ServerUtils serverUtils,
                           LanguageService languageService) {
        this.mainCtrl = mainCtrl;
        this.serverUtils = serverUtils;
        this.languageService = languageService;
    }

    /**
     * On initialization, this method sets the new FoodItem's name
     * to what has been passed on from the ShoppingList (if anything has been)
     */
    public void initialize () {
        if (ingredientName!=null) {
            foodItemName.setText(ingredientName);
        }
        setupLanguageMenu();
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
    }

    /**
     * A method for confirming all changes to the FoodItem
     */
    @FXML
    public void confirmChanges() {
        try {
            FoodItem newItem = new FoodItem(
                    foodItemName.getText(),
                    Integer.parseInt(kcalValue.getText()),
                    Double.parseDouble(carbsValue.getText()),
                    Double.parseDouble(proteinsValue.getText()),
                    Double.parseDouble(fatsValue.getText())
            );

            foodItemName.clear();
            kcalValue.clear();
            carbsValue.clear();
            proteinsValue.clear();
            fatsValue.clear();

            // Save to server
            if (editingItem == null || cameFromAddRecipeToShoppingList || cameFromShoppingList) {
                saveAddFoodItem(newItem);
            } else {
                saveEditFoodItem(newItem);
            }

            confirmButton.setText("Confirm");
            sceneTitle.setText("Adding Food Item");

            handleReturnToScene();

        } catch(NumberFormatException e){
            Alert invalidInput = new Alert(Alert.AlertType.WARNING);
            invalidInput.setTitle(LanguageManager.getString("alert.warning.title"));
            invalidInput.setHeaderText(LanguageManager.getString("alert.warning.invalid.header"));
            invalidInput.setContentText(LanguageManager.getString("alert.warning.invalid.content"));
            invalidInput.showAndWait();
        }
    }

    /**
     * A helper method which distributes the client to the correct scene
     */
    public void handleReturnToScene () {
        if(mainCtrl.isCameFromEditIngredients()){
            mainCtrl.showEditIngredients(mainCtrl.getDraftRecipe());
        } else if (mainCtrl.isCameFromCreateRecipe()) {
            mainCtrl.showCreateRecipeFromFoodItem();
        } else if (mainCtrl.isCameFromShoppingList()) {
            mainCtrl.showShoppingListDialogBox(ingredientName);
        } else if (mainCtrl.isCameFromAddRecipeToShoppingList()) {
            mainCtrl.showAddRecipeToShoppingListDialogBox(ingredientName);
        } else if (mainCtrl.isCameFromFoodItemMain()) {
            mainCtrl.showFoodItemMain();
        } else {
            // otherwise, go to the Food Item Management screen
            mainCtrl.showFoodItemMain();
        }

        mainCtrl.resetOriginFlags();
    }


    /**
     * A helper method which adds a new FoodItem to the database
     * @param newItem - the new FoodItem being added to the database
     */
    public void saveAddFoodItem (FoodItem newItem) {
        try {
            serverUtils.addFoodItem(newItem);
        } catch (jakarta.ws.rs.ClientErrorException e) {
            if (e.getResponse().getStatus() == HttpStatus.CONFLICT.value()) {
                Alert invalidInput = new Alert(Alert.AlertType.ERROR);
                invalidInput.setTitle(LanguageManager.getString("alert.error.title"));
                invalidInput.setContentText(LanguageManager.getString
                        ("alert.error.duplicate.content"));
                invalidInput.showAndWait();
            }
        }
    }

    /**
     * A helper method which edits a FoodItem in the database
     * @param newItem - the FoodItem to be edited in the database
     */
    public void saveEditFoodItem (FoodItem newItem) {
        try {
            serverUtils.updateFoodItem(editingItem.getId(), newItem);
        } catch (jakarta.ws.rs.ClientErrorException e) {
            if (e.getResponse().getStatus() == 409) {
                Alert invalidInput = new Alert(Alert.AlertType.ERROR);
                invalidInput.setTitle(LanguageManager.getString("alert.error.title"));
                invalidInput.setContentText(LanguageManager.getString
                        ("alert.error.duplicate.edit.content"));
                invalidInput.showAndWait();
            } else {
                System.err.println("Edited FoodItem could not be saved.");
            }
        }
    }

    /**
     * A method for cancelling the changes to a FoodItem
     */
    @FXML
    public void cancelChanges() {
        // 1. Reset the UI state
        editingItem = null;
        foodItemName.clear();
        kcalValue.clear();
        carbsValue.clear();
        proteinsValue.clear();
        fatsValue.clear();

        confirmButton.setText("Confirm");
        sceneTitle.setText("Adding Food Item");

        // if a draft exists, we go back to the Create Recipe screen.
        // if not, we go back to the Food Item list.
        if(mainCtrl.isCameFromEditIngredients()){
            mainCtrl.showEditIngredients(mainCtrl.getDraftRecipe());
            mainCtrl.setCameFromEditIngredients(false);
        } else if (mainCtrl.getDraftRecipe() != null) {
            mainCtrl.showCreateRecipe();
        } else {
            mainCtrl.showFoodItemMain();
        }
    }

    /**
     * A method to prepare the UI to show the editing of a FoodItem
     * @param item - the FoodItem that has to be edited
     */
    public void setEditingItem(FoodItem item){
        this.editingItem = item;

        foodItemName.setText(item.getName());
        kcalValue.setText(String.valueOf(item.getKcal()));
        carbsValue.setText(String.valueOf(item.getCarbs()));
        proteinsValue.setText(String.valueOf(item.getProteins()));
        fatsValue.setText(String.valueOf(item.getFats()));

        confirmButton.setText("Save");
        sceneTitle.setText("Editing Food Item");
    }

    /**
     * Sets up the label for FoodItem name
     * Also confirms it is accessed from Shopping List
     * @param foodItemString - the String entered from the user
     */
    public void accessFromTheShoppingList (String foodItemString){
        cameFromShoppingList = true;
        ingredientName = foodItemString;
        if (foodItemName != null) {
            foodItemName.setText(ingredientName);
        }
    }

    /**
     * Sets up the label for FoodItem name
     * Also confirms it is accessed from Add Recipe To Shopping List
     * @param foodItemString - the String entered from the user
     */
    public void accessFromAddRecipeToTheShoppingList(String foodItemString) {
        cameFromAddRecipeToShoppingList = true;
        ingredientName = foodItemString;
        if (foodItemName != null) {
            foodItemName.setText(ingredientName);
        }
    }

    /**
     * Refreshes the content with the current FoodItem
     * @param updated - the current FoodItem
     */
    public void refreshContent (FoodItem updated) {
        setEditingItem(updated);
    }

}
