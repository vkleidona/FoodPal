package client.scenes;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import jakarta.inject.Inject;
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

import java.util.List;


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

public class EditIngredientsCtrl{

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
    private ComboBox<String> inputFoodName;
    @FXML
    private TextField inputQuantity;
    @FXML
    private TextField inputMeasurement;

    // the client cannot call server methods directly
    private final Client client = ClientBuilder.newClient(new ClientConfig());
    private final String recipeEndpointUrl = "http://localhost:8080/api/recipes";

    private Recipe recipe;
    private Ingredient lastClickedIngredient = null;

    private MainCtrl mainCtrl;
    private client.utils.ServerUtils server;

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

    public void initialize(Recipe recipe) {
        /* recipeName.setText(recipe.getName());
        listOfIngredients.getItems().addAll(recipe.getIngredients());
        ingredientNameColumn.setCellValueFactory(new PropertyValueFactory<Ingredient, String>("foodName"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<Ingredient, Double>("quantity"));
        measurementColumn.setCellValueFactory(new PropertyValueFactory<Ingredient, String>("measurement"));
        System.out.println(recipe);

         */

        this.recipe = recipe; // 1. Save the recipe

        //Clear old data
        listOfIngredients.getItems().clear();
        recipeName.setText(recipe.getName());
        listOfIngredients.getItems().addAll(recipe.getIngredients());

        ingredientNameColumn.setCellValueFactory(new PropertyValueFactory<>("foodName"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        measurementColumn.setCellValueFactory(new PropertyValueFactory<>("measurement"));

        List<FoodItem> items = server.getAllFoodItems();


        for (FoodItem item : items) {
            inputFoodName.getItems().add(item.getName());
        }

        //Reset the inputs so the "Name" field isn't stuck as disabled
        resetForm();
    }
    private void resetForm() {
        inputFoodName.setValue(null);
        inputQuantity.clear();
        inputMeasurement.clear();
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
        // Clear the errorLabel.
        errorLabel.setText("");
        lastClickedIngredient = listOfIngredients.getSelectionModel().getSelectedItem();
        // Show values of the clicked ingredient in the input fields.
        if (lastClickedIngredient != null && lastClickedIngredient.getFoodType() != null) {
            inputFoodName.setValue(lastClickedIngredient.getFoodType().getName());
        }
        inputQuantity.setText(String.valueOf(lastClickedIngredient.getQuantity()));
        inputMeasurement.setText(String.valueOf(lastClickedIngredient.getMeasurement()));

        inputFoodName.setDisable(true);
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
        resetForm();
    }

    @FXML
    void addIngredient() {
        errorLabel.setText("");

        // If the ingredient is already in the list don't add it again.
        for(Ingredient ingredient : listOfIngredients.getItems()){
            if (inputFoodName.getEditor().getText().equals(ingredient.getFoodType().getName())) {
                errorLabel.setText("Food is already in the list.");
                return;
            }
        }

        // Try making the new ingredient.
        try {
            double quantity = Double.parseDouble(inputQuantity.getText());

            String foodName = inputFoodName.getEditor().getText();
            FoodItem foodItem = server.getFoodItemByName(foodName);
            if (foodItem==null) {
                foodItem = new FoodItem(foodName);
            }
            String measurement = inputMeasurement.getText();

            Ingredient ingredient = new Ingredient(foodItem, quantity, measurement);
            recipe.addIngredient(ingredient);
            ingredient.setRecipe(recipe);
            // to be removed
            System.out.println(recipe);
            listOfIngredients.getItems().add(ingredient);

            listOfIngredients.refresh();
        } catch (NumberFormatException e) {
            // If the given quantity cannot be parsed to a double give error.
            errorLabel.setText("Quantity must be a double.");
        }

        resetForm();
    }

    /**
     * Removes the selected ingredient from the list.
     */
    @FXML
    void removeIngredient() {
        errorLabel.setText("");
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
            Recipe done = client.target(recipeEndpointUrl)
                    .path("/ingredients/set/" + recipe.getId())
                    .request(APPLICATION_JSON)
                    .put(Entity.entity(listOfIngredients.getItems(), APPLICATION_JSON), Recipe.class);
            // Return to the mainUI
            closeUI();
        } catch (Exception e) {
            // Exceptions
            e.printStackTrace();
        }
    }


    private void closeUI() {
        mainCtrl.showMainMenu();
    }
}