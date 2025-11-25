package client.scenes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import recipes.Recipe;
import recipes.Ingredient;
import recipes.FoodItem;
import java.io.IOException;
import java.util.*;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;

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

    // the client cannot call server methods directly
    private final Client client = ClientBuilder.newClient();
    private final String recipeEndpointUrl = "http://localhost:8080/recipes";

    /**
     * we save all the given information in the database
     */
    /*
    TO DO:
    modify when doing the nutritional value part
     */
    @FXML
    public void handleApply() throws JsonProcessingException {

        // first, we get the recipe name
        String recipeName = nameTextField.getText().trim();
        Recipe recipe = new Recipe(recipeName);

        // then, all the ingredients
        List<Ingredient> ingredients = new ArrayList<>();
        for (Node node : ingredientsVBox.getChildren()) {
            if (node instanceof HBox row) {
                TextField nameField = (TextField) row.getChildren().get(0);
                TextField quantityField = (TextField) row.getChildren().get(1);
                TextField unitField = (TextField) row.getChildren().get(2);

                /*
                TO DO:
                handle the different names units have, such as grams, g, gr
                will mean the same thing
                 */
                String ingName = nameField.getText().trim();
                int quantity = Integer.parseInt(quantityField.getText().trim());
                String unit = unitField.getText().trim();

                FoodItem foodItem = new FoodItem(ingName, 0, 0, 0, 0); // placeholder nutrition
                Ingredient ingredient = new Ingredient(quantity, unit);
                ingredient.setFoodType(foodItem);
                ingredient.setRecipe(recipe);
                ingredients.add(ingredient);
            }
        }
        recipe.setIngredients(ingredients);

        // gather preparation steps
        List<String> steps = new ArrayList<>();
        for (Node node : preparationVBox.getChildren()) {
            if (node instanceof HBox row) {
                TextField stepField = (TextField) row.getChildren().get(0);
                String step = stepField.getText().trim();
                if (!step.isEmpty()) {
                    steps.add(step);
                }
            }
        }
        recipe.setInstructions(steps);

        // POST request, sending the Recipe object to the server over the network
        try {
            // Recipe is automatically turned into JSON text, this text is sent
            // to the server's /recipes URL via POST
            // the server reconstructs the Recipe Object and saves it in the database
            client.target(recipeEndpointUrl)
                    .request("application/json")
                    .post(Entity.entity(recipe, "application/json"));
            goToMainUI();

        } catch (Exception e) {
            e.printStackTrace();
        }
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource(""));

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

/*
TO DO: implement these buttons, delete from the database
 */
    public void handleDeleteIngredient(){

    }

    public void handleDeletePreparation(){

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

        TextField unitField = new TextField();
        unitField.setPromptText("Unit");

        Button deleteButton = new Button("x");
        // when this button is clicked -> the row is removed

        HBox row = new HBox(10, nameField, quantityField, unitField, deleteButton);
        // creating a horizontal container, for a single ingredient input row
        // 10 = spacing, in pixels, between rows/children
        // this rows contains in the exact order, the fields
        deleteButton.setOnAction(event ->
                ingredientsVBox.getChildren().remove(row));
        // defines what happens when the button is clicked, the row is deleted
        ingredientsVBox.getChildren().add(row);
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
            FoodItem foodItem = new FoodItem(ingredientNames.get(i), 0, 0, 0, 0); // placeholder nutrition
            Ingredient ingredient = new Ingredient(ingredientQuantities.get(i), ingredientUnits.get(i));
            ingredient.setFoodType(foodItem);
            ingredient.setRecipe(recipe);
            ingredients.add(ingredient);
        }
        recipe.setIngredients(ingredients);
        recipe.setInstructions(instructions);

        return recipe;
    }

}
