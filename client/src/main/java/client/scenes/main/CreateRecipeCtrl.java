package client.scenes.main;

import client.utils.MainCtrl;
import client.service.LanguageService;
import client.utils.ServerUtils;
import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import recipes.Recipe;
import recipes.Ingredient;
import recipes.FoodItem;
import java.util.*;
import client.i18n.LanguageManager;
import java.util.Locale;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;

@SuppressWarnings("unchecked")
public class CreateRecipeCtrl {

    /**
     * container for the ingredients
     */
    @FXML
    private VBox ingredientsVBox;

    @FXML
    private MenuButton languageButton;

    @FXML
    private TextField servingsTextField;

    @FXML
    private VBox preparationVBox;

    @FXML
    private TextField nameTextField;

    // keeps track of the selected row
    private HBox selectedRow = null;

    // these will help us change the unit from imperial to metric and
    // the other way around
    @FXML
    private RadioButton metricRadioButton;

    @FXML
    private RadioButton imperialRadioButton;

    @FXML
    private RadioButton kitchenRadioButton;

    @FXML
    private ToggleGroup systemGroup;

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

    private Locale currentLocale;

    /**
     * helper method to display an alert dialog to the user.
     * @param type    type of alert (e.g., ERROR, WARNING, INFORMATION).
     * @param titleKey   title text of the alert window.
     * @param contentKey main message text to display in the alert.
     */
    private void showAlert(Alert.AlertType type, String titleKey, String contentKey) {
        Alert alert = new Alert(type);
        alert.setTitle(LanguageManager.getString(titleKey));
        alert.setHeaderText(null);
        alert.setContentText(LanguageManager.getString(contentKey));
        alert.showAndWait();
    }


    private final ServerUtils server;
    private final MainCtrl mainCtrl;
    private final LanguageService languageService;

    /**
     * A Constructor for the CreateRecipeCtrl class
     * @param server - the instance of the ServerUtils class
     * @param mainCtrl - the instance of the MainCtrl class
     * @param languageService - the instance of the language service class
     */
    @Inject
    public CreateRecipeCtrl(ServerUtils server, MainCtrl mainCtrl,
                            LanguageService languageService) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.languageService = languageService;
    }

    /**
     * Updates ONLY the currently selected row to the chosen system
     * Clears quantity and resets unit options for that row
     */
    @FXML
    public void handleSystemChange() {
        if (selectedRow == null) return; // no row selected, does nothing

        // gets the controls from the selected row
        TextField quantityField = (TextField) selectedRow.getChildren().get(1);


        ChoiceBox<String> unitChoice = (ChoiceBox<String>) selectedRow.getChildren().get(2);

        // clear quantity
        quantityField.clear();
        unitChoice.getItems().clear();

        // repopulate based on selection
        if (metricRadioButton.isSelected()) {
            unitChoice.getItems().addAll("g", "kg", "ml", "l");
            unitChoice.setValue("g");
        } else if (imperialRadioButton.isSelected()) {
            unitChoice.getItems().addAll("oz", "lb", "fl oz", "cup", "tbsp", "tsp");
            unitChoice.setValue("oz");
        } else {
            unitChoice.getItems().addAll("slice", "clove", "stick", "pinch", "dash", "jar");
            unitChoice.setValue("stick");
        }
    }

    /**
     * main handler for the Apply button
     * coordinates validation, object creation, and saving
     */
    @FXML
    public void handleApply() {
        String recipeName = nameTextField.getText().trim();
        if (recipeName.isEmpty()) {
            showAlert(Alert.AlertType.WARNING,
                    "alert.recipe.missingName.title",
                    "alert.recipe.missingName.content");
            return;
        }

        String servingsText = servingsTextField.getText().trim();
        Integer servings = null;
        if (!servingsText.isEmpty()) {
            try {
                servings = Integer.parseInt(servingsText);
                if (servings <= 0) {
                    showAlert(Alert.AlertType.ERROR,
                            "alert.recipe.invalidServings.title",
                            "alert.recipe.invalidServings.positive");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR,
                        "alert.recipe.invalidServings.title",
                        "alert.recipe.invalidServings.whole");
                return;
            }
        }

        if (!validateRecipeCompleteness()) return;

        saveRecipe(recipeName, servings);

    }

    /**
     * Sends a request to the database to save the recipe
     * @param recipeName - the recipe name
     * @param servings - the servings entered by the user
     */
    public void saveRecipe (String recipeName, Integer servings) {
        try {
            Recipe recipe = new Recipe(recipeName);

            // build Ingredients List (throws Exception if invalid)
            List<Ingredient> ingredients = buildIngredientsList(recipe);
            if (ingredients == null) return; // validation failed inside helper
            recipe.setIngredients(ingredients);

            if (servings != null) {
                recipe.setServings(servings);
            }

            // build Instructions List
            List<String> steps = buildInstructionsList();
            if (steps == null) return; // validation failed inside helper
            recipe.setInstructions(steps);
            recipe.setLocale(currentLocale);

            // save
            server.sendRecipe(recipe);
            goToMainUI();
            resetUI();

        } catch (Exception e) {
            System.err.println("Error: could not build recipe.");
        }
    }

    /**
     * resets all UI fields to their default state.
     * clears text fields, removes ingredients/instructions, and resets radio buttons.
     */
    private void resetUI() {

        nameTextField.clear();
        servingsTextField.clear();

        ingredientsVBox.getChildren().clear();
        preparationVBox.getChildren().clear();

        metricRadioButton.setSelected(true);

        selectedRow = null;
    }


    /**
     * Helper: checks if lists are empty and asks for confirmation
     * @return true if it's safe to proceed, false if user cancelled
     */
    private boolean validateRecipeCompleteness() {
        boolean hasIngredients = !ingredientsVBox.getChildren().isEmpty();
        boolean hasInstructions = !preparationVBox.getChildren().isEmpty();

        if (!hasIngredients || !hasInstructions) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(LanguageManager.getString("alert.recipe.incomplete.title"));
            alert.setHeaderText(LanguageManager.getString("alert.recipe.incomplete.header"));
            alert.setContentText(LanguageManager.getString("alert.recipe.incomplete.content"));

            Optional<ButtonType> result = alert.showAndWait();
            return result.isPresent() && result.get() == ButtonType.OK;
        }
        return true;
    }

    /**
     * Initializer method - if the user is returning from the Create FoodItem UI,
     * they will pick up from where they left off - the called method restores the
     * draft recipe saved beforehand
     */
    @FXML
    public void initialize() {
        if (mainCtrl != null && mainCtrl.getDraftRecipe() != null) {
            restoreDraft(mainCtrl.getDraftRecipe());
            handleRefresh();
        }
        currentLocale = LanguageManager.getLocale();
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

        updateLanguageButtonUI(LanguageManager.getLocale());
    }

    /**
     * A method for changing the language of the menus to a new Locale
     * @param newLocale - the new language we want to set the menus to
     */
    public void changeLanguage(Locale newLocale) {
        LanguageManager.setLocale(newLocale);
        updateLanguageButtonUI(newLocale);
        currentLocale = newLocale;
        mainCtrl.reloadScenesForNewLanguage();
    }

    /**
     * Restore the UI state from a saved draft
     * @param draft - the draft recipe from which we need to restore
     * the current version of the draft
     */
    public void restoreDraft(Recipe draft) {
        // restore Name & Servings
        mainCtrl.showCreateRecipe();

        nameTextField.setText(draft.getName());
        if (draft.getServings() != null) {
            servingsTextField.setText(String.valueOf(draft.getServings()));
        }

        // Clear existing rows
        ingredientsVBox.getChildren().clear();

        // Collect ingredients and sort alphabetically by name
        List<Ingredient> sortedIngredients = new ArrayList<>(draft.getIngredients());
        String createItemLabel = LanguageManager.getString("combo.createFoodItem");

        sortedIngredients.sort(Comparator.comparing(ing -> {
            String name = ing.getFoodType().getName();
            return createItemLabel.equals(name) ? "" : name.toLowerCase();
        }));

        // Recreate rows in sorted order
        for (Ingredient ing : sortedIngredients) {
            handleAddIngredient();

            // get the last added row
            HBox lastRow = (HBox) ingredientsVBox.getChildren().getLast();

            ComboBox<String> nameBox = (ComboBox<String>) lastRow.getChildren().get(0);
            TextField qtyField = (TextField) lastRow.getChildren().get(1);
            ChoiceBox<String> unitChoice = (ChoiceBox<String>) lastRow.getChildren().get(2);

            String savedName = ing.getFoodType().getName();
            if (createItemLabel.equals(savedName)) {
                nameBox.setValue(null);
                javafx.application.Platform.runLater(() -> {
                    nameBox.getSelectionModel().clearSelection();
                    nameBox.getEditor().clear();
                });
            } else {
                nameBox.setValue(savedName);
            }

            qtyField.setText(String.valueOf(ing.getQuantity()));
            unitChoice.setValue(ing.getMeasurement());
        }

        mainCtrl.setDraftRecipe(null);
        handleRefresh();
    }

    /**
     * helper: iterates through UI rows to build the Ingredient list
     * Displays alerts directly if validation fails
     * @return List of ingredients, or null if validation failed
     */
    private List<Ingredient> buildIngredientsList(Recipe recipe) {
        List<Ingredient> list = new ArrayList<>();

        for (Node node : ingredientsVBox.getChildren()) {
            if (node instanceof HBox row) {
                ComboBox<String> nameField = (ComboBox<String>) row.getChildren().get(0);
                TextField quantityField = (TextField) row.getChildren().get(1);
                ChoiceBox<String> unitChoice = (ChoiceBox<String>) row.getChildren().get(2);

                String name = nameField.getValue().trim();
                String qtyStr = quantityField.getText().trim();

                if (name.isEmpty() || qtyStr.isEmpty()) {
                    showAlert(Alert.AlertType.ERROR,
                            "alert.ingredient.incomplete.title",
                            "alert.ingredient.incomplete.content");
                    return null;
                }

                double quantity;
                try {
                    quantity = Double.parseDouble(qtyStr);
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR,
                            "alert.ingredient.invalidQuantity.title",
                            "alert.ingredient.invalidQuantity.content");
                    return null;
                }

                // Calculate Unit & Grams
                String selectedUnit = unitChoice.getValue();
                double gramsPerUnit = UNIT_TO_GRAMS.getOrDefault(selectedUnit, 0.0);

                Ingredient ing = new Ingredient(quantity, selectedUnit, quantity * gramsPerUnit);
                ing.setFoodType(new FoodItem(name, 0, 0, 0, 0));
                ing.setRecipe(recipe);
                list.add(ing);
            }
        }
        return list;
    }

    /**
     * helper: builds instructions list.
     * @return List of strings, or null if validation failed.
     */
    private List<String> buildInstructionsList() {
        List<String> steps = new ArrayList<>();
        for (Node node : preparationVBox.getChildren()) {
            if (node instanceof HBox row) {
                TextField stepField = (TextField) row.getChildren().getFirst();
                String text = stepField.getText().trim();

                if (text.isEmpty()) {
                    showAlert(Alert.AlertType.ERROR,
                            "alert.instruction.empty.title",
                            "alert.instruction.empty.content");
                    return null;
                }
                steps.add(text);
            }
        }
        return steps;
    }


    /**
     * all the entered information(name, ingredients, preparation steps)
     * will be deleted, therefore not saved.
     * the user will be directed to the main UI
     */
    @FXML
    public void handleCancel(){

        // clear the fields
        nameTextField.clear();
        ingredientsVBox.getChildren().clear();
        preparationVBox.getChildren().clear();
        goToMainUI();
        resetUI();
    }

    private void goToMainUI() {
        mainCtrl.showMainMenu();
    }

    /**
     * when the add button is pressed, '+', near 'Preparation:' a new step
     * of the recipe will be added.
     * the rows will also contain a delete button.
     */
    @FXML
    public void handleAddPreparation(){

        TextField stepField = new TextField();
        stepField.setPromptText(LanguageManager.getString("prompt.step.description"));
        Button deleteButton = new Button("x");
        HBox row = new HBox(10, stepField, deleteButton);
        deleteButton.setOnAction(_ ->
                preparationVBox.getChildren().remove(row));

        preparationVBox.getChildren().add(row);
    }

    /**
     * Normalizes a food name to lower case and singular form (basic heuristic).
     * "Eggs " -> "egg", "Apple" -> "apple"
     */
    private String normalizeName(String input) {
        if (input == null) return "";
        String normalized = input.trim().toLowerCase();
        if (normalized.endsWith("s") && normalized.length() > 1) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }


    /**
     * When the add button is pressed, '+', near 'Ingredients:' a new 'ingredient row' will be
     * added, where the user will have to add the name, quantity, unit.
     * the rows will also contain a delete button.
     */
    @FXML
    public void handleAddIngredient() {
        // creating ComboBox for Food Items
        ComboBox<String> foodItemBox = new ComboBox<>();
        foodItemBox.setEditable(false);
        foodItemBox.setPromptText(LanguageManager.getString("prompt.ingredient.select"));


        // fetch and Populate Items
        List<FoodItem> items = server.getAllFoodItems();

        // sorting alphabetically
        items.sort(Comparator.comparing(FoodItem::getName));

        for (FoodItem item : items) {
            foodItemBox.getItems().add(item.getName());
        }

        String createItemLabel = LanguageManager.getString("combo.createFoodItem");
        foodItemBox.getItems().add(createItemLabel);

        // make sure the food item is added
        javafx.application.Platform.runLater(() ->
                foodItemBox.getSelectionModel().clearSelection());

        foodItemBox.setOnAction(_ -> {
            if (LanguageManager.getString("combo.createFoodItem").equals(foodItemBox.getValue())) {
                handleCreateNewFoodItem();
            }
        });

        TextField quantityField = new TextField();
        quantityField.setPromptText(LanguageManager.getString("prompt.ingredient.qty"));
        quantityField.setPrefWidth(60);

        ChoiceBox<String> unitChoice = configureChoiceBox();

        Button deleteButton = new Button("x");

        HBox row = new HBox(10, foodItemBox, quantityField, unitChoice, deleteButton);

        foodItemBox.focusedProperty().addListener((_, _, n) -> { if(n) selectedRow = row; });
        quantityField.focusedProperty().addListener((_, _, n) -> { if(n) selectedRow = row; });

        deleteButton.setOnAction(_ -> ingredientsVBox.getChildren().remove(row));
        ingredientsVBox.getChildren().add(row);
        selectedRow = row;
    }

    /**
     * Configures the Choice Box of a unit for every ingredient
     * @return ChoiceBox <String> - returns the correct ChoiceBox
     */
    public ChoiceBox<String> configureChoiceBox () {
        ChoiceBox<String> unitChoice = new ChoiceBox<>();
        if (metricRadioButton.isSelected()) {
            unitChoice.getItems().addAll("g", "kg", "ml", "l");
            unitChoice.setValue("g");
        } else if (imperialRadioButton.isSelected()) {
            unitChoice.getItems().addAll("oz", "lb", "fl oz", "cup", "tbsp", "tsp");
            unitChoice.setValue("oz");
        } else {
            unitChoice.getItems().addAll("slice", "clove", "stick", "pinch", "dash", "jar");
            unitChoice.setValue("stick");
        }
        return unitChoice;
    }

    /**
     * Saves current progress and redirects to Add Food Item screen
     */
    public void handleCreateNewFoodItem() {
        // create a temporary recipe object to hold the draft data
        Recipe draft = new Recipe(nameTextField.getText(), currentLocale);

        try {
            if (!servingsTextField.getText().isEmpty()) {
                draft.setServings(Integer.parseInt(servingsTextField.getText()));
            }
        } catch (NumberFormatException ignored) {
            System.err.println("Incorrect number entered; Servings can only be an integer");
        }

        List<Ingredient> partialIngredients = new ArrayList<>();

        // loop through existing UI rows to save them
        for (Node node : ingredientsVBox.getChildren()) {
            partialIngredients.add(updateUIRow(node));
        }
        draft.setIngredients(partialIngredients);

        // save to MainCtrl and Switch Scene
        if (mainCtrl != null) {
            mainCtrl.setDraftRecipe(draft);
            mainCtrl.showAddFoodItem();

            Platform.runLater(this::handleRefresh);
        } else {
            System.err.println("MainCtrl is null, cannot redirect!");
        }
    }

    /**
     * A helper method for updating each UI row
     * @param node - the potential row we have to update in the vertical list
     * @return Ingredient - returns an Ingredient to be added to the ingredient list
     * of the restored recipe
     */
    public Ingredient updateUIRow (Node node) {
        if (node instanceof HBox row) {
            // Get controls
            ComboBox<String> nameBox = (ComboBox<String>) row.getChildren().get(0);
            TextField qtyField = (TextField) row.getChildren().get(1);
            ChoiceBox<String> unitChoice = (ChoiceBox<String>) row.getChildren().get(2);

            String name = nameBox.getValue();
            String qtyStr = qtyField.getText();
            String unit = unitChoice.getValue();

            if (name != null && !name.isEmpty()) {
                double qty = 0.0;
                if (!qtyStr.isEmpty()) {  // Skip parse if empty
                    try {
                        qty = Double.parseDouble(qtyStr);
                    } catch (NumberFormatException e) {
                        System.err.println("Could not save quantity: non-numeric input '"
                                + qtyStr + "'");
                    }
                }

                // Create ingredient just to hold data
                Ingredient ing = new Ingredient(qty, unit, 0);
                ing.setFoodType(new FoodItem(name));

                String createItemLabel = LanguageManager.getString("combo.createFoodItem");
                if (createItemLabel.equals(name)) {
                    javafx.application.Platform.runLater(() -> nameBox.setValue(null));
                }
                return ing;
            }
        }
        return null;
    }





    /**
     * A helper method to build recipes from inside tests
     * @param name - the name of the recipe
     * @param ingredientNames - a list of ingredients in the recipe
     * @param ingredientQuantities a list of quantities for each individual ingredient
     * @param ingredientUnits - a list of ingredient units which can be used
     * @param instructions - a list of instructions
     * @return Recipe - returns the newly constructed recipe
     */
    public Recipe buildRecipe(
            String name,
            List<String> ingredientNames,
            List<Integer> ingredientQuantities,
            List<String> ingredientUnits,
            List<String> instructions) {

        Recipe recipe = new Recipe(name);

        List<Ingredient> ingredients = new ArrayList<>();
        for (int i = 0; i < ingredientNames.size(); i++) {
            FoodItem foodItem = new FoodItem(ingredientNames.get(i),
                    0, 0, 0, 0); // placeholder nutrition
            Ingredient ingredient = new Ingredient(
                    ingredientQuantities.get(i),
                    ingredientUnits.get(i),
                    0.0 // default for tests
            );
            ingredient.setFoodType(foodItem);
            ingredient.setRecipe(recipe);
            ingredients.add(ingredient);
        }
        recipe.setIngredients(ingredients);
        recipe.setInstructions(instructions);
        recipe.setLocale(currentLocale);

        return recipe;
    }

    /**
     * A method which handles the refreshing of the list of FoodItems
     * inside the dropdown menu after a new FoodItem has been added
     */
    @FXML
    public void handleRefresh() {
        // fetch latest data
        List<FoodItem> freshList = server.getAllFoodItems();
        freshList.sort(Comparator.comparing(FoodItem::getName));

        for (Node node : ingredientsVBox.getChildren()) {
            if (node instanceof HBox row && row.getChildren().getFirst() instanceof ComboBox) {
                ComboBox<String> box = (ComboBox<String>) row.getChildren().getFirst();

                String currentSelection = box.getValue();

                if (box.getValue() != null) {
                    box.getSelectionModel().clearSelection();
                    box.setValue(null);
                }

                box.getItems().clear();

                for (FoodItem item : freshList) {
                    box.getItems().add(item.getName());
                }
                box.getItems().add(LanguageManager.getString("combo.createFoodItem"));

                if (currentSelection != null &&
                        box.getItems().contains(currentSelection)) {
                    box.setValue(currentSelection);
                }
            }
        }
    }

}
