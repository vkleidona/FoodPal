package client.scenes;

import client.utils.ServerUtils;
import jakarta.inject.Inject;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import recipes.Recipe;
import recipes.Ingredient;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.IOException;
import java.util.List;

public class MainUICtrl {

    @FXML private ListView<String> recipeListView;

    @FXML private Button addRecipeButton;
    @FXML private Button deleteRecipeButton;
    @FXML private Button editNameButton;
    @FXML private Button editIngredientsButton;
    @FXML private Button editInstructionsButton;
    @FXML private Button foodItemButton;
    @FXML private Button shoppingListButton;
    @FXML private Button addToShoppingListButton;

    @FXML private Button exitButton;
    @FXML private Button minimizeButton;
    @FXML private Button maximizeButton;
    @FXML private Button refreshButton;

    @FXML private Label recipeNameLabel;
    @FXML private VBox ingredientsVBox;
    @FXML private VBox instructionsVBox;

    @FXML private Label servingsLabel;

    private final Client client = ClientBuilder.newClient();
    private final String recipeEndpointUrl = "http://localhost:8080/recipes";

    private List<Recipe> currentRecipes;
    private Recipe selectedRecipe;

    private final ServerUtils server;
    private final MainCtrl mainCtrl;


    @Inject
    public MainUICtrl (ServerUtils server, MainCtrl mainCtrl) {
        this.server = server;
        this.mainCtrl = mainCtrl;
    }

    @FXML
    public void initialize() {
        loadRecipes();

        // Sidebar selection updates main UI
        recipeListView.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            int index = newVal.intValue();
            if (index >= 0 && index < currentRecipes.size()) {
                selectedRecipe = currentRecipes.get(index);
                displayRecipe(selectedRecipe);
            }
        });

        // Top bar buttons
        exitButton.setOnAction(e -> System.exit(0));
        minimizeButton.setOnAction(e -> ((Stage) exitButton.getScene().getWindow()).setIconified(true));
        maximizeButton.setOnAction(e -> {
            Stage stage = (Stage) exitButton.getScene().getWindow();
            stage.setMaximized(!stage.isMaximized());
        });
        refreshButton.setOnAction(e -> loadRecipes());

        // Bottom sidebar buttons
        addRecipeButton.setOnAction(e -> openCreateRecipeUI());
        deleteRecipeButton.setOnAction(e -> handleDeleteRecipe());

        // Placeholders for edit buttons
        editNameButton.setOnAction(e -> handleEditName());
        editIngredientsButton.setOnAction(e -> handleEditIngredients());
        editInstructionsButton.setOnAction(e -> {
            int selectedIndex = recipeListView.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0 && selectedIndex < currentRecipes.size()) {
                handleEditInstructions(currentRecipes.get(selectedIndex));
            }
            else{
                System.out.println("Please select a recipe to edit");
            }
        });
        foodItemButton.setOnAction(e -> handleGoToFoodItems());
    }

    private void loadRecipes() {
        try {
            currentRecipes = client.target(recipeEndpointUrl)
                    .request(MediaType.APPLICATION_JSON)
                    .get(new GenericType<List<Recipe>>() {});

            recipeListView.getItems().clear();
            for (Recipe r : currentRecipes) {
                recipeListView.getItems().add(r.getName());
            }

            // Select first recipe if exists
            if (!currentRecipes.isEmpty()) {
                selectedRecipe = currentRecipes.get(0);
                recipeListView.getSelectionModel().select(0);
                displayRecipe(currentRecipes.get(0));
            } else {
                clearRecipeDisplay();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displayRecipe(Recipe recipe) {
        recipeNameLabel.setText(recipe.getName());

        // if the serving is null, then it becomes 1 automatically
        int servings = (recipe.getServings() != null) ? recipe.getServings() : 1;
        servingsLabel.setText(String.valueOf(servings));

        ingredientsVBox.getChildren().clear();
        for (Ingredient ing : recipe.getIngredients()) {
            ingredientsVBox.getChildren().add(new Label(ing.getFoodType().getName() +
                    " - " + ing.getQuantity() + " " + ing.getMeasurement()));
        }

        instructionsVBox.getChildren().clear();
        int stepNum = 1;
        for (String step : recipe.getInstructions()) {
            instructionsVBox.getChildren().add(new Label(stepNum++ + ". " + step));
        }
    }

    private void clearRecipeDisplay() {
        recipeNameLabel.setText("");
        ingredientsVBox.getChildren().clear();
        instructionsVBox.getChildren().clear();
        servingsLabel.setText("");
    }

    private void openCreateRecipeUI() {
        /*try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/scenes/creatingRecipe.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Create Recipe");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/scenes/creatingRecipe.fxml"));
            Parent root = loader.load();

            // use a control that exists in MainUICtrl
            Stage stage = (Stage) addRecipeButton.getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("Create Recipe");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
*/
        mainCtrl.showCreateRecipe();
    }

    // delete button
    private void handleDeleteRecipe() {
        int selectedIndex = recipeListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= currentRecipes.size()) return;

        Recipe selectedRecipe = currentRecipes.get(selectedIndex);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Recipe");
        alert.setHeaderText("Are you sure you want to delete '" + selectedRecipe.getName() + "'?");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    long id = selectedRecipe.getId(); // primitive long is fine
                    System.out.println("Deleting recipe with ID: " + id);

                    Response serverResponse = client.target(recipeEndpointUrl + "/" + id)
                            .request()
                            .delete();

                    if (serverResponse.getStatus() == 204) {
                        System.out.println("Recipe deleted successfully");
                        loadRecipes(); // refresh sidebar
                    } else {
                        System.out.println("Failed to delete recipe: " + serverResponse.getStatus());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // Placeholder methods for edit buttons
    private void handleEditName() { System.out.println("Edit name placeholder"); }
    private void handleEditIngredients() { System.out.println("Edit ingredients placeholder"); }

    private void handleEditInstructions(Recipe recipe) {
        if(recipe == null){
            System.out.println("Recipe is null");
            return;
        }
        mainCtrl.showEditInstructions(recipe);
    }

    private void handleGoToFoodItems() { System.out.println("Go to Food Items placeholder"); }

    /**
     * The Shopping List is connected to the corresponding interface
     */
    @FXML
    private void handleGoToShoppingList() {
        mainCtrl.showShoppingList();
    }

    /**
     * The Add to Shopping List button is connected to the corresponding interface
     */
    @FXML
    private void handleAddRecipeToShoppingList () {
        mainCtrl.showAddRecipeToShoppingList();
    }

    /**
     * A getter for the selected recipe
     * @return - Recipe - returns the currently selected recipe -
     * useful for displaying ingredients, instructions, etc.
     */
    public Recipe getSelectedRecipe () {
        return selectedRecipe;
    }

    @FXML
    public void handleInformation(ActionEvent actionEvent) {
        if (recipeListView.getSelectionModel().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText(null);
            alert.setContentText("Please select a recipe from the list first!");
            alert.showAndWait();
            return;
        }

        int selectedIndex = recipeListView.getSelectionModel().getSelectedIndex();

        if (selectedIndex < 0 || selectedIndex >= currentRecipes.size()) {
            return;
        }

        Recipe selectedRecipe = currentRecipes.get(selectedIndex);

        if (selectedRecipe == null ||
                selectedRecipe.getName() == null ||
                selectedRecipe.getName().trim().isEmpty()) {

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Invalid Selection");
            alert.setHeaderText(null);
            alert.setContentText("Cannot open detailed view: The selected recipe has no name/data.");
            alert.showAndWait();
            return;
        }

        mainCtrl.showInformation(selectedRecipe);
    }
}
