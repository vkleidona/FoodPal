package client.scenes.nutrition;

import client.service.NutritionalService;
import client.utils.MainCtrl;
import client.service.LanguageService;
import jakarta.inject.Inject;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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

    @FXML
    private MenuButton languageButton;

    @FXML
    private ResourceBundle resources;

    @FXML
    private TextField scaleFactor ;


    private final MainCtrl mainCtrl;
    private final LanguageService languageService;
    private final NutritionalService nutritionalService;


    /**
     * Creates a deep copy of the given recipe, including its ingredients,
     * food items, and instructions, so that any scaling done in
     * InformationCtrl does not modify the original Recipe from MainUI.
     *
     * @param src the source Recipe to copy (it may be null)
     * @return a new Recipe instance with copied data, or null if src is null
     */
    private Recipe copyRecipe(Recipe src) {
        if (src == null) return null;

        Recipe copy = new Recipe(src.getName());
        copy.setServings(src.getServings());
        copy.setLocale(src.getLocale());

        if (src.getIngredients() != null) {
            for (Ingredient ing : src.getIngredients()) {
                Ingredient ingCopy = getIngredient(ing, copy);
                copy.getIngredients().add(ingCopy);
            }
        }

        if (src.getInstructions() != null) {
            copy.setInstructions(new java.util.ArrayList<>(src.getInstructions()));
        }

        return copy;
    }

    /**
     * A method which copies the ingredient of a Recipe
     * @param ing - the ingredient we want to copy
     * @param copy - the recipe we will copy into
     * @return Ingredient - the Ingredient to be added into the new recipe
     */
    private static Ingredient getIngredient(Ingredient ing, Recipe copy) {
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
        return ingCopy;
    }


    /**
     * A constructor for the InformationCtrl class which uses Dependency Injection
     * @param mainCtrl - the instance of the MainCtrl class
     * @param languageService - the service supporting languages
     * @param nutritionalService - provides calculations for nutritional values
     */
    @Inject
    public InformationCtrl(MainCtrl mainCtrl, LanguageService languageService,
                           NutritionalService nutritionalService) {
        this.mainCtrl = mainCtrl;
        this.languageService = languageService;
        this.nutritionalService = nutritionalService;
    }

    /**
     * An initializer method - sets up the language menu
     */

    @FXML
    public void initialize() {
        setupLanguageMenu();
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
            int newServings = (int) Math.round(nutritionalService.getCurrentRecipe()
                    .getServings() * factor);
            if (newServings < 1) newServings = 1;
            nutritionalService.getCurrentRecipe().setServings(newServings);
            servingsField.setText(String.valueOf(newServings));

            // Scale all ingredients
            if (nutritionalService.getCurrentRecipe().getIngredients() != null) {
                for (Ingredient ing : nutritionalService.getCurrentRecipe().getIngredients()) {
                    nutritionalService.scaleIngredient(ing, factor);
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
        this.nutritionalService.setOriginalRecipe(copyRecipe(recipe));

        // Create working copy for this screen
        this.nutritionalService.setCurrentRecipe(copyRecipe(recipe));

        // 1. Set Servings Field
        if (nutritionalService.getCurrentRecipe().getServings() != null) {
            servingsField.setText(String.valueOf(nutritionalService.getCurrentRecipe()
                    .getServings()));
        } else {
            servingsField.setText("1");
            nutritionalService.getCurrentRecipe().setServings(1);
        }

        if (nutritionalService.getCurrentRecipe().getIngredients() != null) {
            for (Ingredient ing : nutritionalService.getCurrentRecipe().getIngredients()) {
                nutritionalService.optimizeIngredientUnit(ing);
            }
        }

        if(nutritionalService.getOriginalRecipe().getServings() == null ||
                nutritionalService.getOriginalRecipe().getServings() == 0 ){
            nutritionalService.getOriginalRecipe().setServings(1);
        }

        renderIngredientsList();
        calculateAndShowNutrition();

    }

    /**
     * The back button will restore the state when the recipe
     * was opened
     */
    @FXML
    public void handleResetToOriginal() {
        if (nutritionalService.getOriginalRecipe() == null) return;

        // Recreate working copy from original snapshot
        nutritionalService.setCurrentRecipe(copyRecipe(nutritionalService.getOriginalRecipe()));

        // Reset servings field
        if (nutritionalService.getCurrentRecipe().getServings() != null) {
            servingsField.setText(String.valueOf(nutritionalService.getCurrentRecipe()
                    .getServings()));
        } else {
            nutritionalService.getCurrentRecipe().setServings(1);
            servingsField.setText("1");
        }

        // Redraw UI
        renderIngredientsList();
        calculateAndShowNutrition();

        // Clear scale field and calories field to match fresh state
        scaleFactor.clear();

    }

    /**
     * Helper to redraw the ingredients list.
     * Separated, so we can call it after scaling without resetting the whole recipe object.
     */
    private void renderIngredientsList() {
        ingredientsContainer.getChildren().clear();

        if (nutritionalService.getCurrentRecipe().getIngredients() != null) {
            for (Ingredient ing : nutritionalService.getCurrentRecipe().getIngredients()) {
                try {
                    HBox row = nutritionalService.renderRow(ing);
                    calculateAndShowNutrition();
                    ingredientsContainer.getChildren().add(row);
                    servingsField.setText(String.valueOf(nutritionalService.getCurrentRecipe().
                            getServings()));
                }catch (NumberFormatException e) {
                    System.err.println("Wrong input of a number");
                }
            }
        }
    }

    /**
     * A method which scales the ingredients according to the servings
     */
    @FXML
    public void handleServingsChange() {
        try {
            int newServings = Integer.parseInt(servingsField.getText());
            if (newServings < 1) return; // Ignore invalid values

            int oldServings = nutritionalService.getCurrentRecipe().getServings();
            double scaleFactor = (double) newServings / oldServings;

            // Update Recipe State
            nutritionalService.getCurrentRecipe().setServings(newServings);

            // Scale every ingredient
            for (Ingredient ing : nutritionalService.getCurrentRecipe().getIngredients()) {
                nutritionalService.scaleIngredient(ing, scaleFactor);
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
     * Calculates totals and displays nutritional info per 100g.
     */
    public void calculateAndShowNutrition() {

        double[] data = new double[]{0, 0, 0, 0, 0};
        if (nutritionalService.getCurrentRecipe().getIngredients() != null) {
            data = nutritionalService.calculateNutrition();
        }

        double totalCal = data[0];
        double totalProt = data[1];
        double totalCarb = data[2];
        double totalFat = data[3];
        double totalWeightInGrams = data[4];

        if (totalWeightInGrams > 0) {
            double factor = 100.0 / totalWeightInGrams;

            double calPer100  = totalCal  * factor;
            double protPer100 = totalProt * factor;
            double carbPer100 = totalCarb * factor;
            double fatPer100  = totalFat  * factor;

            caloriesLabel.setText(MessageFormat.format(
                    resources.getString("info.caloriesPer100.value"),
                    calPer100, totalCal));

            proteinsLabel.setText(MessageFormat.format(
                    resources.getString("info.proteinsPer100.value"),
                    protPer100, totalProt));

            carbsLabel.setText(MessageFormat.format(
                    resources.getString("info.carbsPer100.value"),
                    carbPer100, totalCarb));

            fatsLabel.setText(MessageFormat.format(
                    resources.getString("info.fatsPer100.value"),
                    fatPer100, totalFat));
        } else {
            caloriesLabel.setText(MessageFormat.format(
                    resources.getString("info.caloriesPer100.value"), 0.0, 0.0));
            proteinsLabel.setText(MessageFormat.format(
                    resources.getString("info.proteinsPer100.value"), 0.0, 0.0));
            carbsLabel.setText(MessageFormat.format(
                    resources.getString("info.carbsPer100.value"), 0.0, 0.0));
            fatsLabel.setText(MessageFormat.format(
                    resources.getString("info.fatsPer100.value"), 0.0, 0.0));
        }
    }

    /**
     * Creates a copy of the scaled recipe,
     * appends the scale factor to the recipe name and opens the
     * add to shopping list screen, passing this copy.
     */
    @FXML
    public void handleSaveScaledRecipeToShoppingList(){
        Recipe scaledRecipe = copyRecipe(nutritionalService.getCurrentRecipe());

        double factor = (double) nutritionalService.getCurrentRecipe().getServings() /
                nutritionalService.getOriginalRecipe().getServings();
        String label = String.format(" (%.2gx)", factor);
        // to stop after 2 decimal places and to round
        scaledRecipe.setName(scaledRecipe.getName() + label);

        mainCtrl.showAddScaledRecipeToShoppingList(scaledRecipe);
    }

}
