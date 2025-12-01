package client.scenes;

import com.fasterxml.jackson.core.JsonProcessingException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import recipes.Recipe;
import recipes.Ingredient;
import recipes.FoodItem;
import java.io.IOException;
import java.util.*;

public class CreateRecipeCtrl {

    /**
     * container for the ingredients
     */
    @FXML
    private VBox ingredientsVBox;

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

    /**
     * helper method to display an alert dialog to the user.
     * @param type    type of alert (e.g., ERROR, WARNING, INFORMATION).
     * @param title   title text of the alert window.
     * @param content main message text to display in the alert.
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private ServerService serverService;

    public CreateRecipeCtrl() {
        this.serverService = new RealServerService();
    }

    public void setServerService(ServerService serverService) {
        this.serverService = serverService;
    }

    private final String recipeEndpointUrl = "http://localhost:8080/recipes";

    /**
     * updates ONLY the currently selected row to the chosen system
     * clears quantity and resets unit options for that row
     */
    @FXML
    public void handleSystemChange() {
        if (selectedRow == null) return; // no row selected, does nothing

        // gets the controls from the selected row
        TextField quantityField = (TextField) selectedRow.getChildren().get(1);

        @SuppressWarnings("unchecked")
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
            unitChoice.getItems().addAll("slice", "clove", "stick", "pinch", "dash", "jar", "other");
            unitChoice.setValue("stick");
        }
    }


    /*
    TO DO:
    modify when doing the nutritional value part
     */
    /**
     * main handler for the Apply button
     * coordinates validation, object creation, and saving
     */
    @FXML
    public void handleApply() {
        String recipeName = nameTextField.getText().trim();
        if (recipeName.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Missing recipe name",
                    "Please enter a recipe name before saving.");
            return;
        }

        if (!validateRecipeCompleteness()) return;

        try {
            Recipe recipe = new Recipe(recipeName);

            // build Ingredients List (throws Exception if invalid)
            List<Ingredient> ingredients = buildIngredientsList(recipe);
            if (ingredients == null) return; // validation failed inside helper
            recipe.setIngredients(ingredients);

            // build Instructions List
            List<String> steps = buildInstructionsList();
            if (steps == null) return; // validation failed inside helper
            recipe.setInstructions(steps);

            // save
            serverService.sendRecipe(recipe);
            goToMainUI();

        } catch (Exception e) {
            e.printStackTrace();
        }
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
            alert.setTitle("Incomplete Recipe");
            alert.setHeaderText("Missing Ingredients or Instructions");
            alert.setContentText("Save without ingredients or instructions?");

            Optional<ButtonType> result = alert.showAndWait();
            return result.isPresent() && result.get() == ButtonType.OK;
        }
        return true;
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
                TextField nameField = (TextField) row.getChildren().get(0);
                TextField quantityField = (TextField) row.getChildren().get(1);
                ChoiceBox<String> unitChoice = (ChoiceBox<String>) row.getChildren().get(2);
                TextField customUnitField = (TextField) row.getChildren().get(3);
                TextField gramsField = (TextField) row.getChildren().get(4);

                String name = nameField.getText().trim();
                String qtyStr = quantityField.getText().trim();

                if (name.isEmpty() || qtyStr.isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Incomplete Ingredient",
                            "Name and Quantity are required.");
                    return null;
                }

                int quantity;
                try {
                    quantity = Integer.parseInt(qtyStr);
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Quantity",
                            "Quantity must be a valid number.");
                    return null;
                }

                // Calculate Unit & Grams
                String selectedUnit = unitChoice.getValue();
                String finalUnit = selectedUnit;
                double gramsPerUnit = UNIT_TO_GRAMS.getOrDefault(selectedUnit, 0.0);

                if ("other".equals(selectedUnit)) {
                    String customName = customUnitField.getText().trim();
                    String customGrams = gramsField.getText().trim();

                    if (customName.isEmpty() || customGrams.isEmpty()) {
                        showAlert(Alert.AlertType.ERROR, "Missing Custom Info",
                                "Fill out custom unit name and grams.");
                        return null;
                    }
                    try {
                        gramsPerUnit = Double.parseDouble(customGrams);
                    } catch (NumberFormatException e) {
                        showAlert(Alert.AlertType.ERROR, "Invalid Grams",
                                "Custom grams must be a number.");
                        return null;
                    }
                    finalUnit = customName;
                }

                Ingredient ing = new Ingredient(quantity, finalUnit, quantity * gramsPerUnit);
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
                TextField stepField = (TextField) row.getChildren().get(0);
                String text = stepField.getText().trim();

                if (text.isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Empty Instruction",
                            "Please fill out all steps or remove empty rows.");
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
    }

    private void goToMainUI() {
        try {
            // loading the fxml file, the UI where the user will be directed
            // between "" the filepath to the main UI needs to be added
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/scenes/MainUI.fxml"));

            // this builds the UI, meaning any previous user input will be reset
            /*
            this can be adjusted, depending on whether we want to keep the
            previous user input, or not
             */
            Parent mainRoot = loader.load();

            // we will have here our current scene, so then it can be changed
            Stage stage = (Stage) nameTextField.getScene().getWindow();

            // a new scene is created
            Scene mainScene = new Scene(mainRoot);

            // the current scene is replaced with the new one
            stage.setScene(mainScene);
        } catch (IOException e){
            // if loading the FXML file fails, the error will be caught here
        }
    }

    /**
     * when the add button is pressed, '+', near 'Preparation:' a new step
     * of the recipe will be added.
     * the rows will also contain a delete button.
     */
    @FXML
    public void handleAddPreparation(){

        TextField stepField = new TextField();
        stepField.setPromptText("Step description");

        Button deleteButton = new Button("x");
        HBox row = new HBox(10, stepField, deleteButton);
        deleteButton.setOnAction(event ->
                preparationVBox.getChildren().remove(row));

        preparationVBox.getChildren().add(row);
    }


    /**
     * when the add button is pressed, '+', near 'Ingredients:' a new 'ingredient row' will be
     * added, where the user will have to add the name, quantity, unit.
     * the rows will also contain a delete button.
     */
    /*
    TO DO:
    add fields for the nutritional values
     */
    @FXML
    public void handleAddIngredient(){

        TextField nameField = new TextField();
        nameField.setPromptText("Ingredient name");

        TextField quantityField = new TextField();
        quantityField.setPromptText("Quantity");

        /*
        TextField unitField = new TextField();
        unitField.setPromptText("Unit");
        previously, when the user could enter the unit
         */

        ChoiceBox<String> unitChoice = new ChoiceBox<>();

        if (metricRadioButton.isSelected()) {
            unitChoice.getItems().addAll("g", "kg", "ml", "l");
            unitChoice.setValue("g");
        } else if (imperialRadioButton.isSelected()) {
            unitChoice.getItems().addAll("oz", "lb", "fl oz", "cup", "tbsp", "tsp");
            unitChoice.setValue("oz");
        } else {
            unitChoice.getItems().addAll("slice", "clove", "stick", "pinch", "dash", "jar", "other");
            unitChoice.setValue("stick");
        }

        // hidden fields
        TextField customUnitField = new TextField();
        customUnitField.setPromptText("Unit Name (e.g. can)");
        customUnitField.setVisible(false); // hidden by default
        customUnitField.setManaged(false); // does not take up space when hidden

        TextField gramsField = new TextField();
        gramsField.setPromptText("Grams per 1 unit");
        gramsField.setVisible(false);
        gramsField.setManaged(false);

        unitChoice.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean isOther = "other".equals(newVal);

            // Show/Hide fields
            customUnitField.setVisible(isOther);
            customUnitField.setManaged(isOther);

            gramsField.setVisible(isOther);
            gramsField.setManaged(isOther);
        });

        Button deleteButton = new Button("x");
        // when this button is clicked -> the row is removed

        HBox row = new HBox(10, nameField, quantityField, unitChoice, customUnitField, gramsField, deleteButton);
        // creating a horizontal container, for a single ingredient input row
        // 10 = spacing, in pixels, between rows/children
        // this rows contains in the exact order, the fields

        // when user clicks Name or Quantity, we mark this row as selected
        nameField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) selectedRow = row;
        });
        quantityField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) selectedRow = row;
        });
        unitChoice.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) selectedRow = row;
        });
        customUnitField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) selectedRow = row;
        });
        gramsField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) selectedRow = row;
        });


        deleteButton.setOnAction(event ->
                ingredientsVBox.getChildren().remove(row));
        // defines what happens when the button is clicked, the row is deleted
        ingredientsVBox.getChildren().add(row);
        selectedRow = row;
        // .getChildren() -> all elements in the VBox(in this case, rows)

    }

    /**
     * helper method, to use in the test class
     * @param name String
     * @param ingredientNames list
     * @param ingredientQuantities list
     * @param ingredientUnits list
     * @param instructions list
     * @return recipe
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

        return recipe;
    }
}
