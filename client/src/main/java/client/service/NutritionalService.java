package client.service;

import jakarta.inject.Inject;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import recipes.FoodItem;
import recipes.Ingredient;
import recipes.Recipe;

public class NutritionalService {

    private Recipe originalRecipe;
    private Recipe currentRecipe;

    @Inject
    public NutritionalService () {

    }

    public Recipe getCurrentRecipe() {
        return currentRecipe;
    }

    public Recipe getOriginalRecipe() {
        return originalRecipe;
    }

    public void setOriginalRecipe(Recipe originalRecipe) {
        this.originalRecipe = originalRecipe;
    }

    public void setCurrentRecipe(Recipe currentRecipe) {
        this.currentRecipe = currentRecipe;
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
            double grams = ing.getQuantityInGrams();
            totalWeightInGrams += grams;

            if (food != null) {
                double ratio = grams / 100.0;
                totalCal  += ratio * food.getKcal();
                totalProt += ratio * food.getProteins();
                totalCarb += ratio * food.getCarbs();
                totalFat  += ratio * food.getFats();
            }
        }
        // [0]=kcal, [1]=proteins, [2]=carbs, [3]=fats, [4]=weight
        return new double[]{totalCal, totalProt, totalCarb, totalFat, totalWeightInGrams};
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

    /**
     * Helper to scale a single ingredient's visual quantity and internal grams.
     * @param ing - ingredient to be scaled
     * @param scaleFactor - factor with which to scale the ingredient
     */
    public void scaleIngredient(Ingredient ing, double scaleFactor) {
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
     * Helper to redraw the ingredients list.
     * Separated, so we can call it after scaling without resetting the whole recipe object.
     * @param ing - the ingredient to be rendered
     * @return HBox - returns a row of ingredients
     */
    public HBox renderRow(Ingredient ing) {
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
        return row;
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
     * A method which scales the quantity of all ingredients and servings
     * to match the scaled quantity of the selected ingredient
     * @param targetIng - the Ingredient which determines the other quantities
     * @param newQtyStr - the new quantity to be set
     */
    public void handleIngredientChange(Ingredient targetIng, String newQtyStr) {
        try {
            double newQty = Double.parseDouble(newQtyStr);
            if (newQty <= 0) return;

            double oldQty = targetIng.getQuantity();
            double scaleFactor = newQty / oldQty;

            // 1. Update Servings (Rounded to nearest integer)
            int newServings = (int) Math.round(currentRecipe.getServings() * scaleFactor);
            if (newServings < 1) newServings = 1;

            currentRecipe.setServings(newServings);
            for (Ingredient ing : currentRecipe.getIngredients()) {
                scaleIngredient(ing, scaleFactor);
            }

        } catch (NumberFormatException e) {
            System.err.println("Error: Could not scale other ingredients - non-numeric input");
        }
    }

}
