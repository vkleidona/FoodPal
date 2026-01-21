package client.scenes;

import client.utils.ServerUtils;
import jakarta.inject.Inject;
import client.i18n.LanguageManager;
import java.util.Locale;
import java.util.Objects;

import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
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

    private FoodItem editingItem;

    private String ingredientName;

    // these booleans indicate where the Add Food Item was accessed from
    private boolean cameFromShoppingList = false;
    private boolean cameFromAddRecipeToShoppingList = false;

    /**
     * A constructor for the AddFoodItemCtrl class using dependency injection
     * @param mainCtrl - the instance of the Main Controller class
     * @param serverUtils - the instance of the Server Utilities
     */
    @Inject
    public AddFoodItemCtrl(MainCtrl mainCtrl, ServerUtils serverUtils) {
        this.mainCtrl = mainCtrl;
        this.serverUtils = serverUtils;
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
     * A method which sets up the icons for the language menu
     * @param name - the name of the language we are adding
     * @param langCode - the locale code of the language
     * @param iconFileName - the encoding for the name of the icon file
     * @return MenuItem - returns the ready-made MenuItem with the proper icon
     */
    public MenuItem setupMenuItemIcons (String name, String langCode, String iconFileName) {
        MenuItem menuItem = new MenuItem(name);

        Image flagImage = new Image(Objects.requireNonNull
                (getClass().getResourceAsStream("/flags/" + iconFileName + "-flag.jpg")));

        ImageView icon = new ImageView(flagImage);
        icon.setFitWidth(16);
        icon.setFitHeight(16);

        Circle clip = new Circle(8, 8, 8);
        icon.setClip(clip);

        menuItem.setGraphic(icon);

        menuItem.setOnAction(_ -> changeLanguage(Locale.of(langCode)));

        return menuItem;
    }

    /**
     * A method which updates the flag icon of the language button when necessary
     * @param iconFileName - the encoding for the name of the icon file
     */
    public void updateButtonGraphic(String iconFileName) {
        Image img = new Image (Objects.requireNonNull
                (getClass().getResourceAsStream("/flags/" + iconFileName + "-flag.jpg")));
        ImageView imageView = new ImageView(img);

        imageView.setFitWidth(16);
        imageView.setFitHeight(16);

        Circle clip = new Circle(8, 8, 8);
        imageView.setClip(clip);

        // Set the graphic to the MenuButton itself
        languageButton.setGraphic(imageView);
    }

    /**
     * A setup method for the language menu -
     * adds the constructed MenuItems using the helper methods with the appropriate icon
     */
    public void setupLanguageMenu() {
        languageButton.getItems().clear();

        MenuItem enItem = setupMenuItemIcons("English", "en", "english");
        MenuItem nlItem = setupMenuItemIcons("Nederlands", "nl", "dutch");
        MenuItem bgItem = setupMenuItemIcons("Български", "bg", "bulgarian");
        MenuItem roItem = setupMenuItemIcons("Română", "ro", "romanian");
        MenuItem skItem = setupMenuItemIcons("Slovenčina", "sk", "slovak");
        MenuItem deItem = setupMenuItemIcons("Deutsch", "de", "german");

        languageButton.getItems().addAll(enItem, nlItem, bgItem, roItem, skItem, deItem);

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
            case "sk" -> {
                languageButton.setText("Slovenčina");
                updateButtonGraphic("slovak");
            }
            case "de" -> {
                languageButton.setText("Deutsch");
                updateButtonGraphic("german");
            }
            default -> {
                languageButton.setText("English");
                updateButtonGraphic("english");
            }
        }
    }

    /**
     * A method for changing the language of the menus to a new Locale
     * @param newLocale - the new language we want to set the menus to
     */
    public void changeLanguage(Locale newLocale) {
        LanguageManager.setLocale(newLocale);
        switch (newLocale.getLanguage()) {
            case "nl" -> languageButton.setText("Nederlands");
            case "ro" -> languageButton.setText("Română");
            case "bg" -> languageButton.setText("Български");
            case "sk" -> languageButton.setText("Slovenčina");
            case "de" -> languageButton.setText("Deutsch");
            default -> languageButton.setText("English");
        }
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
            if (editingItem == null) {
                saveAddFoodItem(newItem);
            } else {
                saveEditFoodItem(newItem);
            }

            confirmButton.setText("Confirm");
            sceneTitle.setText("Adding Food Item");

            if (mainCtrl.getFoodItemMainCtrl() != null) {
                mainCtrl.getFoodItemMainCtrl().refreshFoodItems();
            }

            if(mainCtrl.isCameFromEditIngredients()){
                mainCtrl.showEditIngredients(mainCtrl.getDraftRecipe());
                mainCtrl.setCameFromEditIngredients(false);
            } else if (mainCtrl.getDraftRecipe() != null) {
                mainCtrl.showCreateRecipe();
            } else if (cameFromShoppingList) {
                mainCtrl.showShoppingListDialogBox(ingredientName);
            }else if (cameFromAddRecipeToShoppingList) {
                mainCtrl.showAddRecipeToShoppingListDialogBox(ingredientName);
            }
            else{
                // otherwise, go to the Food Item Management screen
                mainCtrl.showFoodItemMain();
            }

        } catch(NumberFormatException e){
            Alert invalidInput = new Alert(Alert.AlertType.WARNING);
            invalidInput.setTitle(LanguageManager.getString("alert.warning.title"));
            invalidInput.setHeaderText(LanguageManager.getString("alert.warning.invalid.header"));
            invalidInput.setContentText(LanguageManager.getString("alert.warning.invalid.content"));
            invalidInput.showAndWait();
        }
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
