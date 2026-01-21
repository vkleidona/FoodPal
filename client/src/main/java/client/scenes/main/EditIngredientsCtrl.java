package client.scenes;


import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import recipes.FoodItem;
import recipes.Ingredient;
import recipes.Recipe;
import client.i18n.LanguageManager;

import java.util.*;

import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;

public class EditIngredientsCtrl{

    @FXML
    private TableView<Ingredient> listOfIngredients;

    @FXML
    private TableColumn<Ingredient, String> ingredientNameColumn;
    @FXML
    private TableColumn<Ingredient, Double> quantityColumn;
    @FXML
    private TableColumn<Ingredient, String> measurementColumn;

    @FXML
    private MenuButton languageButton;
    @FXML
    private Label recipeName;

    @FXML
    private ResourceBundle resources;


    @FXML
    private ComboBox<String> inputFoodName;
    @FXML
    private TextField inputQuantity;
    @FXML
    private ComboBox<String> inputMeasurement;
    @FXML
    private RadioButton metric;
    @FXML
    private RadioButton imperial;
    @FXML
    private RadioButton common;

    private Recipe recipe;
    private Ingredient lastClickedIngredient = null;

    private final MainCtrl mainCtrl;
    private final client.utils.ServerUtils server;

    private static final List<String> METRIC_UNITS = List.of("g", "kg", "ml", "l");
    private static final List<String> IMPERIAL_UNITS = List.of
            ("oz", "lb", "fl oz", "cup", "tbsp", "tsp");
    private static final List<String> COMMON_UNITS = List.of
            ("pinch", "dash", "stick", "clove", "slice", "can", "jar");

    private static final Map<String, Double> UNIT_TO_GRAMS = new HashMap<>();
    static {
        // Metric
        UNIT_TO_GRAMS.put("g", 1.0);
        UNIT_TO_GRAMS.put("kg", 1000.0);
        UNIT_TO_GRAMS.put("ml", 1.0);      // approx
        UNIT_TO_GRAMS.put("l", 1000.0);

        // Imperial / US
        UNIT_TO_GRAMS.put("oz", 28.35);
        UNIT_TO_GRAMS.put("lb", 453.59);
        UNIT_TO_GRAMS.put("fl oz", 29.57);
        UNIT_TO_GRAMS.put("cup", 240.0);   // standard US cup approx
        UNIT_TO_GRAMS.put("tbsp", 15.0);
        UNIT_TO_GRAMS.put("tsp", 5.0);

        // Common / Kitchen Units
        UNIT_TO_GRAMS.put("pinch", 0.36);
        UNIT_TO_GRAMS.put("dash", 0.72);
        UNIT_TO_GRAMS.put("stick", 113.0);
        UNIT_TO_GRAMS.put("clove", 5.0);
        UNIT_TO_GRAMS.put("slice", 30.0);
        UNIT_TO_GRAMS.put("can", 400.0);
        UNIT_TO_GRAMS.put("jar", 450.0);
    }

    /**
     * A constructor for the EditIngredients Ctrl class using Dependency Injection
     * @param mainCtrl - the instance of the main controller class
     * @param server - the instance of the server utilities class
     */
    @Inject
    public EditIngredientsCtrl(MainCtrl mainCtrl, client.utils.ServerUtils server) {
        this.mainCtrl = mainCtrl;
        this.server = server;
    }

    /**
     * Can set the displayed recipe of the controller.
     * Currently unused: recipe should be provided in the constructor.
     * @param recipe - new recipe to be displayed.
     */
    public void setRecipe(Recipe recipe){
        this.recipe = recipe;
    }

    /**
     * The initialize method for the class
     * @param recipe - the recipe which these ingredients belong to
     */
    public void initialize(Recipe recipe) {

        this.recipe = recipe; // 1. Save the recipe

        //Clear old data
        listOfIngredients.getItems().clear();
        recipeName.setText(recipe.getName());
        listOfIngredients.getItems().addAll(recipe.getIngredients());

        ingredientNameColumn.setCellValueFactory(new PropertyValueFactory<>("foodName"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        measurementColumn.setCellValueFactory(new PropertyValueFactory<>("measurement"));

        List<FoodItem> items = server.getAllFoodItems();

        inputFoodName.getItems().clear();

        for (FoodItem item : items) {
            inputFoodName.getItems().add(item.getName());
        }

        String createItemLabel = resources.getString("combo.createFoodItem");
        inputFoodName.getItems().add(createItemLabel);

        inputFoodName.setOnAction(_ -> {
            if (createItemLabel.equals(inputFoodName.getValue())) {
                handleCreateNewFoodItem();
            }
        });


        ToggleGroup unitGroup = new ToggleGroup();
        metric.setToggleGroup(unitGroup);
        imperial.setToggleGroup(unitGroup);
        common.setToggleGroup(unitGroup);
        setupLanguageMenu();


        inputFoodName.setEditable(false);
        inputMeasurement.setEditable(false);
    }

    /**
     * Saves the current list of ingredients and redirects to the add food item UI.
     */
    public void handleCreateNewFoodItem(){
        List<Ingredient> currentIngredients = new ArrayList<>(listOfIngredients.getItems());


        this.recipe.setIngredients(currentIngredients);

        Platform.runLater(() -> inputFoodName.getSelectionModel().clearSelection());

        if (mainCtrl != null){
            mainCtrl.setCameFromEditIngredients(true);
            mainCtrl.setDraftRecipe(this.recipe);
            mainCtrl.showAddFoodItem();
        }
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
     * Changes the system in which the units are displayed
     */
    @FXML
    public void handleSystemChange(){
        if (metric.isSelected()) {
            inputMeasurement.getItems().clear();
            inputMeasurement.getItems().addAll("g", "kg", "ml", "l");
            inputMeasurement.setValue("g");
        } else if (imperial.isSelected()) {
            inputMeasurement.getItems().clear();
            inputMeasurement.getItems().
                    addAll("oz", "lb", "fl oz", "cup", "tbsp", "tsp");
            inputMeasurement.setValue("oz");
        } else if(common.isSelected()) {
            inputMeasurement.getItems().clear();
            inputMeasurement.getItems().
                    addAll("slice", "clove", "stick", "pinch", "dash", "jar", "other");
            inputMeasurement.setValue("stick");
        }
    }

    /**
     * Resets the Edit Ingredients screen with null values
     */
    @FXML
    public void resetForm() {
        inputFoodName.setValue(null);
        inputQuantity.clear();
        inputMeasurement.setValue(null);
        inputFoodName.setDisable(false); // Enable it so we can add new items
        lastClickedIngredient = null;
        listOfIngredients.getSelectionModel().clearSelection();
    }


    /**
     * When an ingredient in the listOfIngredients is clicked.
     * Updates the input fields and last clicked ingredient.
     */
    @FXML
    void rowClicked() {
        lastClickedIngredient = listOfIngredients.getSelectionModel().getSelectedItem();
        String unit = null;
        // Show values of the clicked ingredient in the input fields.
        if (lastClickedIngredient != null && lastClickedIngredient.getFoodType() != null) {
            inputFoodName.setValue(lastClickedIngredient.getFoodType().getName());
            inputQuantity.setText(String.valueOf(lastClickedIngredient.getQuantity()));
            unit = lastClickedIngredient.getMeasurement();
        }

        inputMeasurement.getItems().clear();

        if(METRIC_UNITS.contains(unit)){
            metric.setSelected(true);
            inputMeasurement.getItems().addAll("g", "kg", "ml", "l");
        }else if(IMPERIAL_UNITS.contains(unit)){
            imperial.setSelected(true);
            inputMeasurement.getItems().addAll("oz", "lb", "fl oz", "cup", "tbsp", "tsp");
        }else if(COMMON_UNITS.contains(unit)){
            common.setSelected(true);
            inputMeasurement.getItems().
                    addAll("slice", "clove", "stick", "pinch", "dash", "jar", "other");
        }
        inputMeasurement.setValue(unit);
        inputFoodName.setDisable(true);
    }

    /**
     * Submit the changes made to the ingredient values to the table.
     */
    @FXML
    void submitEdit() {
        // Is any ingredient selected?
        if(lastClickedIngredient == null){
            showAlert("editIngredients.alert.noSelection.title", "editIngredients.alert.noSelection.content");
            return;
        }

        // Find the lastClickedIngredient in the table and update the values to match user input.
        ObservableList<Ingredient> currentTable = listOfIngredients.getItems();

        for (Ingredient ingredient : currentTable) {
            if (ingredient.equals(lastClickedIngredient)) {
                try {
                    // If the food quantity can't be parsed to a double output an error message.
                    ingredient.setQuantity(Double.parseDouble(inputQuantity.getText()));
                } catch (NumberFormatException e) {
                    showAlert("editIngredients.alert.invalidNumber.title", "editIngredients.alert.invalidNumber.content");
                    return;
                }
                ingredient.setMeasurement(inputMeasurement.getValue());

                // Refresh the table with the updates values.
                listOfIngredients.refresh();
                break;
            }
        }
        resetForm();
    }

    /**
     * Refreshes the recipe content after a server-pushed update
     * @param recipe - the recipe's current
     */
    public void refreshContent (Recipe recipe) {
        Platform.runLater(() -> {
            this.recipe = recipe;
            listOfIngredients.getItems().setAll(recipe.getIngredients());
        });
    }

    @FXML
    public void addIngredient() {
        if(inputFoodName.getValue() == null || inputQuantity.getText() == null ||
                inputQuantity.getText().trim().isEmpty() ||
                inputMeasurement.getValue() == null ){
            showAlert("editIngredients.alert.missingField.title", "editIngredients.alert.missingField.content");
            return;
        }

        // If the ingredient is already in the list don't add it again.
        for(Ingredient ingredient : listOfIngredients.getItems()){
            if (inputFoodName.getValue().equals(ingredient.getFoodType().getName())) {
                showAlert("editIngredients.alert.duplicate.title", "editIngredients.alert.duplicate.content");
                return;
            }
        }


        // Try making the new ingredient.
        try {
            double quantity = Double.parseDouble(inputQuantity.getText());

            String foodName = inputFoodName.getValue();
            FoodItem foodItem = server.getFoodItemByName(foodName);
            if (foodItem==null) {
                foodItem = new FoodItem(foodName);
            }
            String measurement = inputMeasurement.getValue();

            Ingredient ingredient = new Ingredient(foodItem, quantity, measurement);
            recipe.addIngredient(ingredient);
            ingredient.setRecipe(recipe);
            // to be removed
            listOfIngredients.getItems().add(ingredient);

            listOfIngredients.refresh();
        } catch (NumberFormatException e) {
            // If the given quantity cannot be parsed to a double give error.
            showAlert("editIngredients.alert.invalidNumber.title", "editIngredients.alert.invalidNumber.content");
        }

        resetForm();
    }

    /**
     * Removes the selected ingredient from the list.
     */
    @FXML
    void removeIngredient() {
        if(lastClickedIngredient == null){
            showAlert("editIngredients.alert.noSelection.title", "editIngredients.alert.noSelection.content");
            return;
        }
        listOfIngredients.getItems().remove(lastClickedIngredient);
        listOfIngredients.refresh();
        resetForm();
    }

    /**
     * Cancel editing of the ingredients.
     * does not save any changes made.
     */
    @FXML
    public void cancelEdit() {
        // Just return to the last UI without sending the changes to the server.
        closeUI();
    }

    /**
     * Apply changes to the ingredients.
     * Save the updated list of ingredients to the server.
     */
    @FXML
    public void applyEdit() {
        try {
            server.updateIngredients(recipe, listOfIngredients.getItems());
            // Return to the mainUI
            closeUI();
        } catch (Exception e) {
            // Exceptions
            System.err.println("Error: Could not save ingredient list");
        }
    }


    private void closeUI() {
        mainCtrl.showMainMenu();
    }

    /**
     * A helper method for showing alerts
     * @param title - the title of the alert to be shown
     * @param content - the content of the alert
     */
    public void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(resources.getString(title));
        alert.setContentText(resources.getString(content));
        alert.showAndWait();
    }
}