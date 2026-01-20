package client.scenes;

import jakarta.inject.Inject;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import org.w3c.dom.Text;
import recipes.FoodItem;
import recipes.Ingredient;
import recipes.Recipe;
import client.i18n.LanguageManager;
import java.text.MessageFormat;
import java.util.*;

import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;

public class InformationCtrl {


    @FXML private Label caloriesLabel;
    @FXML private Label proteinsLabel;
    @FXML private Label carbsLabel;
    @FXML private Label fatsLabel;
    @FXML private TextField servingsField;
    @FXML private VBox ingredientsContainer;
    @FXML private TextField caloriesRecipe;

    @FXML
    private MenuButton languageButton;

    @FXML
    private ResourceBundle resources;

    @FXML
    private TextField scaleFactor ;


    private final MainCtrl mainCtrl;
    private Recipe originalRecipe;
    private Recipe currentRecipe;

    /**
     * Creates a deep copy of the given recipe, including its ingredients,
     * food items, and instructions, so that any scaling done in
     * InformationCtrl does not modify the original Recipe from MainUI.
     *
     * @param src the source Recipe to copy (may be null)
     * @return a new Recipe instance with copied data, or null if src is null
     */
    private Recipe copyRecipe(Recipe src) {
        if (src == null) return null;

        Recipe copy = new Recipe(src.getName());
        copy.setServings(src.getServings());
        copy.setLocale(src.getLocale());

        if (src.getIngredients() != null) {
            for (Ingredient ing : src.getIngredients()) {
                FoodItem f = ing.getFoodType();
                FoodItem fCopy = null;
                if (f != null) {
                    fCopy = new FoodItem(
                            f.getName(),
                            f.getKcal(),
                            f.getProteins(),
                            f.getCarbs(),
                            f.getFats()
                    );
                }

                Ingredient ingCopy = new Ingredient(
                        ing.getQuantity(),
                        ing.getMeasurement(),
                        ing.getQuantityInGrams()
                );
                ingCopy.setFoodType(fCopy);
                ingCopy.setRecipe(copy);
                copy.getIngredients().add(ingCopy);
            }
        }

        if (src.getInstructions() != null) {
            copy.setInstructions(new java.util.ArrayList<>(src.getInstructions()));
        }

        return copy;
    }


    /**
     * A constructor for the InformationCtrl class which uses Dependency Injection
     * @param mainCtrl - the instance of the MainCtrl class
     */
    @Inject
    public InformationCtrl(MainCtrl mainCtrl) {
        this.mainCtrl = mainCtrl;
    }

    /**
     * An initializer method - sets up the language menu
     */

    @FXML
    public void initialize() {
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

        menuItem.setOnAction(_ -> changeLanguage(new Locale(langCode)));

        return menuItem;
    }


    /**
     * Scales recipe by custom factor (double multiplier).
     * Updates servings, all ingredients, and refreshes nutrition.
     */
    @FXML
    public void handleScaling() {
        try {
            // Read from the TextField
            double factor = Double.parseDouble(scaleFactor.getText());
            if (factor <= 0) return;  // Ignore invalid

            // Update Servings (rounded)
            int newServings = (int) Math.round(currentRecipe.getServings() * factor);
            if (newServings < 1) newServings = 1;
            currentRecipe.setServings(newServings);
            servingsField.setText(String.valueOf(newServings));

            // Scale all ingredients
            if (currentRecipe.getIngredients() != null) {
                for (Ingredient ing : currentRecipe.getIngredients()) {
                    scaleIngredient(ing, factor);
                }
            }

            // Refresh everything
            renderIngredientsList();
            calculateAndShowNutrition();
            scaleFactor.clear();   // now this is the TextField

        } catch (NumberFormatException e) {
            System.err.println("Invalid scale factor");
            scaleFactor.clear();
            calculateAndShowNutrition();
        }
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
     * A method for returning to the MainUI
     */
    @FXML
    private void returnToMainUI() {
        mainCtrl.showMainMenu();
    }

    /**
     * A method for cancelling the editing (scaling) of information
     * Returns to the main UI
     */
    public void handleCancel(){
        returnToMainUI();
    }

    /**
     * Here we pass the data of the currently selected recipe
     * from the main UI to the InformationUI
     * @param recipe - the currently edited Recipe
     */
    public void setRecipe(Recipe recipe) {
        // Save original snapshot (for reset / discard)
        this.originalRecipe = copyRecipe(recipe);

        // Create working copy for this screen
        this.currentRecipe = copyRecipe(recipe);

        // 1. Set Servings Field
        if (currentRecipe.getServings() != null) {
            servingsField.setText(String.valueOf(currentRecipe.getServings()));
        } else {
            servingsField.setText("1");
            currentRecipe.setServings(1);
        }

        if (currentRecipe.getIngredients() != null) {
            for (Ingredient ing : currentRecipe.getIngredients()) {
                optimizeIngredientUnit(ing);
            }
        }

        if(originalRecipe.getServings() == null || originalRecipe.getServings() == 0 ){
            originalRecipe.setServings(1);
        }

        renderIngredientsList();
        calculateAndShowNutrition();
    }


    /**
     * Helper to redraw the ingredients list.
     * Separated, so we can call it after scaling without resetting the whole recipe object.
     */
    private void renderIngredientsList() {
        ingredientsContainer.getChildren().clear();

        if (currentRecipe.getIngredients() != null) {
            for (Ingredient ing : currentRecipe.getIngredients()) {
                HBox row = new HBox(15);

                // Name
                Label nameLbl = new Label(ing.getFoodType().getName());
                nameLbl.setPrefWidth(200);
                nameLbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

                // Quantity Input
                TextField qtyField = getQtyField(ing);

                // Unit
                Label unitLbl = new Label(ing.getMeasurement());
                unitLbl.setStyle("-fx-font-size: 14px;");

                row.getChildren().addAll(nameLbl, qtyField, unitLbl);
                ingredientsContainer.getChildren().add(row);
            }
        }
    }

    /**
     * A method for extracting the quantity from the field
     * @param ing - the Ingredient whose quantity we extract
     * @return TextField - a new TextField object with the quantity
     */
    public TextField getQtyField(Ingredient ing) {
        TextField qtyField = new TextField();
        if (ing.getQuantity() == (long) ing.getQuantity()) {
            qtyField.setText(String.format("%d", (long) ing.getQuantity()));
        } else {
            qtyField.setText(String.format("%.2f", ing.getQuantity()));
        }

        qtyField.setPrefWidth(80);
        qtyField.setStyle("-fx-font-size: 14px;");

        // when user presses ENTER on an ingredient quantity ---
        qtyField.setOnAction(_ -> handleIngredientChange(ing, qtyField.getText()));
        return qtyField;
    }

    /**
     * The back button will restore the state when the recipe
     * was opened
     */
    @FXML
    public void handleResetToOriginal() {
        if (originalRecipe == null) return;

        // Recreate working copy from original snapshot
        this.currentRecipe = copyRecipe(originalRecipe);

        // Reset servings field
        if (currentRecipe.getServings() != null) {
            servingsField.setText(String.valueOf(currentRecipe.getServings()));
        } else {
            currentRecipe.setServings(1);
            servingsField.setText("1");
        }

        // Redraw UI
        renderIngredientsList();
        calculateAndShowNutrition();

        // Clear scale field and calories field to match fresh state
        scaleFactor.clear();
        caloriesRecipe.setText(String.format("%.0f", recalculateTotalCalories()));
    }


    /**
     * A method which scales the ingredients according to the servings
     */
    @FXML
    public void handleServingsChange() {
        try {
            int newServings = Integer.parseInt(servingsField.getText());
            if (newServings < 1) return; // Ignore invalid values

            int oldServings = currentRecipe.getServings();
            double scaleFactor = (double) newServings / oldServings;

            // Update Recipe State
            currentRecipe.setServings(newServings);

            // Scale every ingredient
            for (Ingredient ing : currentRecipe.getIngredients()) {
                scaleIngredient(ing, scaleFactor);
            }

            // Refresh UI
            renderIngredientsList();
            calculateAndShowNutrition();

        } catch (NumberFormatException e) {
            System.err.println("Error: Could not save the servings of a recipe" +
                    " - non-integer input");
        }
    }

    /**
     * A method which scales the quantity of all ingredients and servings
     * to match the scaled quantity of the selected ingredient
     */
    private void handleIngredientChange(Ingredient targetIng, String newQtyStr) {
        try {
            double newQty = Double.parseDouble(newQtyStr);
            if (newQty <= 0) return;

            double oldQty = targetIng.getQuantity();
            double scaleFactor = newQty / oldQty;

            // 1. Update Servings (Rounded to nearest integer)
            int newServings = (int) Math.round(currentRecipe.getServings() * scaleFactor);
            if (newServings < 1) newServings = 1;

            currentRecipe.setServings(newServings);
            servingsField.setText(String.valueOf(newServings));

            // 2. Scale ALL ingredients (including the one changed, to keep precision sync)
            for (Ingredient ing : currentRecipe.getIngredients()) {
                scaleIngredient(ing, scaleFactor);
            }

            // Refresh UI
            renderIngredientsList();
            calculateAndShowNutrition();

        } catch (NumberFormatException e) {
            System.err.println("Error: Could not scale other ingredients - non-numeric input");
        }
    }

    /**
     *  Scaling logic: User changes Total Calories -> Scale Servings & Ingredients
     */
    @FXML
    public void handleCaloriesRecipe() {
        try {
            double newTotalCalories = Double.parseDouble(caloriesRecipe.getText());
            if (newTotalCalories < 0) return;

            // 1. Calculate current total calories to determine scale factor
            double currentTotalCalories = 0;
            if (currentRecipe.getIngredients() != null) {
                currentTotalCalories = recalculateTotalCalories();
            }

            if (currentTotalCalories == 0) return;

            // determine Scale Factor
            double scaleFactor = newTotalCalories / currentTotalCalories;

            // update Servings (Rounded)
            int newServings = (int) Math.round(currentRecipe.getServings() * scaleFactor);
            if (newServings < 1) newServings = 1;

            currentRecipe.setServings(newServings);
            servingsField.setText(String.valueOf(newServings));

            // scale all ingredients
            for (Ingredient ing : currentRecipe.getIngredients()) {
                scaleIngredient(ing, scaleFactor);
            }

            // refresh UI
            renderIngredientsList();
            calculateAndShowNutrition();

        } catch (NumberFormatException e) {
            // ignore invalid input
            calculateAndShowNutrition(); // reset field to correct value
        }
    }

    /**
     * A method which recalculates the total calories for one recipe
     * @return double - returns a double with the total calories for the recipe
     */
    public double recalculateTotalCalories () {
        double currentTotalCalories = 0;
        for (Ingredient ing : currentRecipe.getIngredients()) {
            FoodItem food = ing.getFoodType();
            if (food != null) {
                currentTotalCalories += (ing.getQuantityInGrams() / 100.0) * food.getKcal();
            }
        }
        return currentTotalCalories;
    }

    /**
     * Helper to scale a single ingredient's visual quantity and internal grams.
     */
    private void scaleIngredient(Ingredient ing, double scaleFactor) {
        // 1. Update visual quantity
        double newQty = ing.getQuantity() * scaleFactor;
        ing.setQuantity(newQty);

        // 2. Update internal grams (essential for nutrition calc)
        double newGrams = ing.getQuantityInGrams() * scaleFactor;
        ing.setQuantityInGrams(newGrams);

        // 3. Optimize Unit (Auto-scale g -> kg, etc.)
        optimizeIngredientUnit(ing);
    }

    /**
     * Calculates totals and displays nutritional info per 100g.
     */
    public void calculateAndShowNutrition() {

        double [] data = new double[]{0, 0, 0, 0, 0};
        if (currentRecipe.getIngredients() != null) {
            data = calculateNutrition();
        }

        double totalCal = data[0];
        double totalProt = data[1];
        double totalCarb = data[2];
        double totalFat = data[3];
        double totalWeightInGrams = data[4];

        caloriesRecipe.setText(String.format("%.0f", totalCal));

        // Calculate values per 100g of the final dish
        if (totalWeightInGrams > 0) {
            double factor = 100.0 / totalWeightInGrams;

            caloriesLabel.setText(MessageFormat.format(
                    resources.getString("info.caloriesPer100.value"),
                    totalCal * factor));

            proteinsLabel.setText(MessageFormat.format(
                    resources.getString("info.proteinsPer100.value"),
                    totalProt * factor));

            carbsLabel.setText(MessageFormat.format(
                    resources.getString("info.carbsPer100.value"),
                    totalCarb * factor));

            fatsLabel.setText(MessageFormat.format(
                    resources.getString("info.fatsPer100.value"),
                    totalFat * factor));
        } else {
            caloriesLabel.setText(MessageFormat.format(
                    resources.getString("info.caloriesPer100.value"), 0.0));
            proteinsLabel.setText(MessageFormat.format(
                    resources.getString("info.proteinsPer100.value"), 0.0));
            carbsLabel.setText(MessageFormat.format(
                    resources.getString("info.carbsPer100.value"), 0.0));
            fatsLabel.setText(MessageFormat.format(
                    resources.getString("info.fatsPer100.value"), 0.0));
        }

    }

    /**
     * A helper method which returns an array of all nutritional values
     * @return double[] - an array containing calories, proteins, carbs, fats
     * and the total weight
     */
    public double[] calculateNutrition () {
        double totalCal = 0;
        double totalProt = 0;
        double totalCarb = 0;
        double totalFat = 0;
        double totalWeightInGrams = 0;

        for (Ingredient ing : currentRecipe.getIngredients()) {
            FoodItem food = ing.getFoodType();

            // track total weight of the recipe to normalize to 100g later
            double grams = ing.getQuantityInGrams();
            totalWeightInGrams += grams;

            if (food != null) {
                // add actual total nutrients for this ingredient to the recipe totals
                // ratio: (Grams / 100) because FoodItem values are stored per 100g
                double ratio = grams / 100.0;

                totalCal += ratio * food.getKcal();
                totalProt += ratio * food.getProteins();
                totalCarb += ratio * food.getCarbs();
                totalFat += ratio * food.getFats();
            }
        }
        return new double[]{totalCal, totalCarb, totalProt, totalFat, totalWeightInGrams};
    }
    /**
     * Automatically converts units based on quantity thresholds.
     * Examples:
     * - 1500 g -> 1.5 kg
     * - 0.5 kg -> 500 g
     * @param ing - the ingredient whose gram value will be calculated
     */
    public void optimizeIngredientUnit(Ingredient ing) {
        String unit = ing.getMeasurement().toLowerCase();
        double qty = ing.getQuantity();

        // Metric Mass: g <-> kg
        if (unit.equals("g") && qty >= 1000) {
            ing.setMeasurement("kg");
            ing.setQuantity(qty / 1000.0);
        } else if (unit.equals("kg") && qty < 1) {
            ing.setMeasurement("g");
            ing.setQuantity(qty * 1000.0);
        }

        // Metric Volume: ml <-> l
        else if (unit.equals("ml") && qty >= 1000) {
            ing.setMeasurement("l");
            ing.setQuantity(qty / 1000.0);
        } else if (unit.equals("l") && qty < 1) {
            ing.setMeasurement("ml");
            ing.setQuantity(qty * 1000.0);
        }

        if ((unit.equals("oz") && qty >= 16) || (unit.equals("lb") && qty < 1)) {
            refactorImperial(ing);
        }
    }

    /**
     * A helper method for comparing imperial values
     * Implemented to avoid Checkstyle warnings.
     * @param ing - the Ingredient whose values should be refactored
     */
    public void refactorImperial (Ingredient ing) {

        String unit = ing.getMeasurement().toLowerCase();
        double qty = ing.getQuantity();

        // Imperial Mass: oz <-> lb (1 lb = 16 oz)
        if (unit.equals("oz") && qty >= 16) {
            ing.setMeasurement("lb");
            ing.setQuantity(qty / 16.0);
        } else if (unit.equals("lb") && qty < 1) {
            ing.setMeasurement("oz");
            ing.setQuantity(qty * 16.0);
        }
    }

    @FXML
    public void handleSaveScaledRecipeToShoppingList(){
        Recipe scaledRecipe = copyRecipe(currentRecipe);

        double factor = (double) currentRecipe.getServings() / originalRecipe.getServings();
        String label = String.format(" (%.2gx)", factor); // to stop after 2 decimal places and to round
        scaledRecipe.setName(scaledRecipe.getName() + label);

        if (scaledRecipe != null){
            mainCtrl.showAddScaledRecipeToShoppingList(scaledRecipe);
        }
    }

}
