package client.scenes;

import client.utils.ServerUtils;
import jakarta.inject.Inject;
import client.i18n.LanguageManager;
import java.util.Locale;
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

    @Inject
    public AddFoodItemCtrl(MainCtrl mainCtrl, ServerUtils serverUtils) {
        this.mainCtrl = mainCtrl;
        this.serverUtils = serverUtils;
    }

    public void initialize () {
        if (ingredientName!=null) {
            foodItemName.setText(ingredientName);
        }
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

            // Clear inputs
            foodItemName.clear();
            kcalValue.clear();
            carbsValue.clear();
            proteinsValue.clear();
            fatsValue.clear();

            // Save to server
            if (editingItem == null) {
                try {
                    serverUtils.addFoodItem(newItem);
                } catch (jakarta.ws.rs.ClientErrorException e) {
                    if (e.getResponse().getStatus() == HttpStatus.CONFLICT.value()) {
                        Alert invalidInput = new Alert(Alert.AlertType.ERROR);
                        invalidInput.setTitle("Duplicate Name");
                        invalidInput.setContentText("A food item with this name already exists.");
                        invalidInput.showAndWait();
                    }
                }
            } else {
                try {
                    FoodItem updated = serverUtils.updateFoodItem(editingItem.getId(), newItem);
                } catch (jakarta.ws.rs.ClientErrorException e) {
                    if (e.getResponse().getStatus() == 409) {
                        Alert invalidInput = new Alert(Alert.AlertType.ERROR);
                        invalidInput.setTitle("Duplicate Name");
                        invalidInput.setContentText("A food item with the same already exists");
                        invalidInput.showAndWait();
                    } else {
                        e.printStackTrace();
                    }
                }
            }

            confirmButton.setText("Confirm");
            sceneTitle.setText("Adding Food Item");

            if (mainCtrl.getFoodItemMainCtrl() != null) {
                mainCtrl.getFoodItemMainCtrl().refreshFoodItems();
            }

            // if we came from "Create Recipe" (draft exists), we go back there.
            if (mainCtrl.getDraftRecipe() != null) {
                mainCtrl.showCreateRecipe();
                // the initialize() method in CreateRecipeCtrl will pick up the draft
                // and reload the food list (finding the new item).
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
            invalidInput.setTitle("Error");
            invalidInput.setHeaderText("Invalid Input");
            invalidInput.setContentText("Please enter a valid number as shown in the examples.");
            invalidInput.showAndWait();
        }
    }


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
        if (mainCtrl.getDraftRecipe() != null) {
            mainCtrl.showCreateRecipe();
        } else {
            mainCtrl.showFoodItemMain();
        }
    }


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
