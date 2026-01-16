package client.scenes;

import client.utils.ServerUtils;
import jakarta.inject.Inject;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import recipes.FoodItem;
import recipes.Ingredient;
import recipes.Recipe;
import client.i18n.LanguageManager;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
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

    private final MainCtrl mainCtrl;
    private final ServerUtils serverUtils;
    private Recipe currentRecipe;

    @Inject
    public InformationCtrl(MainCtrl mainCtrl, ServerUtils serverUtils) {
        this.mainCtrl = mainCtrl;
        this.serverUtils = serverUtils;
    }

    @FXML
    public void initialize() {
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
     * go the main UI
     */
    @FXML
    private void returnToMainUI() {
        mainCtrl.showMainMenu();
    }

    /**
     * return to main UI
     * @param actionEvent for the cancel button
     */
    public void handleCancel(ActionEvent actionEvent){
        returnToMainUI();
    }

    /**
     * here we pass the data from the main UI
     * to the InformationUI
     * @param recipe Recipe
     */
    public void setRecipe(Recipe recipe) {
        this.currentRecipe = recipe;

        // 1. Set Servings Field
        if (recipe.getServings() != null) {
            servingsField.setText(String.valueOf(recipe.getServings()));
        } else {
            servingsField.setText("1");
            recipe.setServings(1);
        }

        if (recipe.getIngredients() != null) {
            for (Ingredient ing : recipe.getIngredients()) {
                optimizeIngredientUnit(ing);
            }
        }

        // render Ingredients List
        renderIngredientsList();

        // update Nutritional Totals
        calculateAndShowNutrition();
    }

    /**
     * helper to redraw the ingredients list.
     * separated, so we can call it after scaling without resetting the whole recipe object.
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
                TextField qtyField = new TextField();
                if (ing.getQuantity() == (long) ing.getQuantity()) {
                    qtyField.setText(String.format("%d", (long) ing.getQuantity()));
                } else {
                    qtyField.setText(String.format("%.2f", ing.getQuantity()));
                }

                qtyField.setPrefWidth(80);
                qtyField.setStyle("-fx-font-size: 14px;");

                // when user presses ENTER on an ingredient quantity ---
                qtyField.setOnAction(e -> handleIngredientChange(ing, qtyField.getText()));

                // Unit
                Label unitLbl = new Label(ing.getMeasurement());
                unitLbl.setStyle("-fx-font-size: 14px;");

                row.getChildren().addAll(nameLbl, qtyField, unitLbl);
                ingredientsContainer.getChildren().add(row);
            }
        }
    }

    /**
     * scaling logic: user changes Servings -> Scale Ingredients
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
            // Ignore non-integer input
        }
    }

    /**
     * scaling logic 2: user changes Ingredient Quantity -> Scale Servings & Other Ingredients
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
            // Ignore invalid input
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
                for (Ingredient ing : currentRecipe.getIngredients()) {
                    FoodItem food = ing.getFoodType();
                    if (food != null) {
                        currentTotalCalories += (ing.getQuantityInGrams() / 100.0) * food.getKcal();
                    }
                }
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
    private void calculateAndShowNutrition() {
        double totalCal = 0;
        double totalProt = 0;
        double totalCarb = 0;
        double totalFat = 0;
        double totalWeightInGrams = 0;

        if (currentRecipe.getIngredients() != null) {
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
        }

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
     * Automatically converts units based on quantity thresholds.
     * Examples:
     * - 1500 g -> 1.5 kg
     * - 0.5 kg -> 500 g
     */
    private void optimizeIngredientUnit(Ingredient ing) {
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

        // Imperial Mass: oz <-> lb (1 lb = 16 oz)
        else if (unit.equals("oz") && qty >= 16) {
            ing.setMeasurement("lb");
            ing.setQuantity(qty / 16.0);
        } else if (unit.equals("lb") && qty < 1) {
            ing.setMeasurement("oz");
            ing.setQuantity(qty * 16.0);
        }
    }


}
