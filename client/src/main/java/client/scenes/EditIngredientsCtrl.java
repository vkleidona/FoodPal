package client.scenes;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import jakarta.ws.rs.client.Entity;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.glassfish.jersey.client.ClientConfig;
import recipes.FoodItem;
import recipes.Ingredient;
import recipes.Recipe;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;

import java.net.URL;
import java.util.ResourceBundle;

/*
    Edit Ingredients scene for a recipe can be initialized with the following code:

    // Create a loader.
        FXMLLoader loader = new FXMLLoader();

    // Set the fxml file to be loaded.
        loader.setLocation(getClass().getResource("/client/scenes/EditIngredients.fxml"));

    // Set the controller for the scene to a controller with the selected recipe.
        loader.setController(new EditIngredientsCtrl(recipe));

    // Create the new scene from the loader.
        Scene scene = new Scene(loader.load());

    // You now have a scene for the specified recipe.
 */

public class EditIngredientsCtrl implements Initializable {

    // Variable for testing if the return method is called
    private boolean calledReturn =  false;

    public boolean getCalledReturn() {
        return calledReturn;
    }

    @FXML
    private TableView<Ingredient> listOfIngredients;

    @FXML
    private TableColumn<Ingredient, String> ingredientNameColumn;
    @FXML
    private TableColumn<Ingredient, Double> quantityColumn;
    @FXML
    private TableColumn<Ingredient, String> measurementColumn;

    @FXML
    private Label errorLabel;
    @FXML
    private Label recipeName;

    @FXML
    private TextField inputFoodName;
    @FXML
    private TextField inputQuantity;
    @FXML
    private TextField inputMeasurement;

    // the client cannot call server methods directly
    private final Client client = ClientBuilder.newClient(new ClientConfig());
    private final String recipeEndpointUrl = "http://localhost:8080/recipes";

    private Recipe recipe;
    private Ingredient lastClickedIngredient = null;

    /**
     * Constructor creates an EditIngredientsCtrl for the given recipe.
     * @param recipe - the recipe to change ingredients for.
     */
    public EditIngredientsCtrl(Recipe recipe) {
        this.recipe = recipe;
    }

    /**
     * Can set the displayed recipe of the controller.
     * Currently unused: recipe should be provided in the constructor.
     * @param recipe - new recipe to be displayed.
     */
    public void setRecipe(Recipe recipe){
        this.recipe = recipe;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        recipeName.setText(recipe.getName());
        listOfIngredients.getItems().addAll(recipe.getIngredients());
        ingredientNameColumn.setCellValueFactory(new PropertyValueFactory<Ingredient, String>("foodName"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<Ingredient, Double>("quantity"));
        measurementColumn.setCellValueFactory(new PropertyValueFactory<Ingredient, String>("measurement"));
        System.out.println(recipe);
    }


    /**
     * When an ingredient in the listOfIngredients is clicked.
     * Updates the input fields and last clicked ingredient.
     */
    @FXML
    void rowClicked() {
        // Clear the errorLabel.
        errorLabel.setText("");
        lastClickedIngredient = listOfIngredients.getSelectionModel().getSelectedItem();
        // Show values of the clicked ingredient in the input fields.
        inputFoodName.setText(String.valueOf(lastClickedIngredient.getFoodType().getName()));
        inputQuantity.setText(String.valueOf(lastClickedIngredient.getQuantity()));
        inputMeasurement.setText(String.valueOf(lastClickedIngredient.getMeasurement()));
    }

    /**
     * Submit the changes made to the ingredient values to the table.
     */
    @FXML
    void submitEdit() {
        // Clear errorLabel.
        errorLabel.setText("");
        // Is any ingredient selected?
        if(lastClickedIngredient == null){
            errorLabel.setText("No ingredient selected");
            return;
        }

        // Find the lastClickedIngredient in the table and update the values to match user input.
        ObservableList<Ingredient> currentTable = listOfIngredients.getItems();

        for (Ingredient ingredient : currentTable) {
            if (ingredient.equals(lastClickedIngredient)) {
                ingredient.setFoodType(new FoodItem(inputFoodName.getText()));
                try {
                    // If the food quantity can't be parsed to a double output an error message.
                    ingredient.setQuantity(Double.parseDouble(inputQuantity.getText()));
                } catch (NumberFormatException e) {
                    errorLabel.setText("Quantity must be a double.");
                }
                ingredient.setMeasurement(inputMeasurement.getText());

                // Refresh the table with the updates values.
                listOfIngredients.refresh();
                break;
            }
        }
    }

    @FXML
    void addIngredient() {
        errorLabel.setText("");

        // If the ingredient is already in the list don't add it again.
        for(Ingredient ingredient : listOfIngredients.getItems()){
            if (inputFoodName.getText().equals(ingredient.getFoodType().getName())) {
                errorLabel.setText("Food is already in the list.");
                return;
            }
        }

        // Try making the new ingredient.
        try {
            double quantity = Double.parseDouble(inputQuantity.getText());
            FoodItem foodItem = new FoodItem(inputFoodName.getText());
            String measurement = inputMeasurement.getText();

            Ingredient ingredient = new Ingredient(foodItem, quantity, measurement);
            ingredient.setRecipe(recipe);
            System.out.println(recipe);
            listOfIngredients.getItems().add(ingredient);

            listOfIngredients.refresh();
        } catch (NumberFormatException e) {
            // If the given quantity cannot be parsed to a double give error.
            errorLabel.setText("Quantity must be a double.");
        }
    }

    /**
     * Removes the selected ingredient from the list.
     */
    @FXML
    void removeIngredient() {
        errorLabel.setText("");
        listOfIngredients.getItems().remove(lastClickedIngredient);
        listOfIngredients.refresh();
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
        recipe.setId(1);
        recipe.setIngredients(listOfIngredients.getItems());
        System.out.println(recipe);
        // Send the new listOfIngredients to the server.
        try {
            Recipe done = client.target(recipeEndpointUrl)
                    .path("/ingredients/set/" + recipe.getId())
                    .request(APPLICATION_JSON)
                    .post(Entity.entity(listOfIngredients.getItems(), APPLICATION_JSON), Recipe.class);
            // Return to the mainUI
            System.out.println(done);
            closeUI();
        } catch (Exception e) {
            // Exceptions
            e.printStackTrace();
        }
    }

    /**
     * To be implemented:
     * Should call the main controller to set the scene to the last UI.
     */
    private void closeUI() {
        calledReturn =  true;
    }
}