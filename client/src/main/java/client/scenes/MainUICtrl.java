package client.scenes;

import client.network.WebSocketService;
import client.utils.ServerUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;

import javafx.collections.FXCollections;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
//import javafx.stage.FileChooser;
import javafx.stage.Stage;
import recipes.FoodItem;
import recipes.Recipe;
import recipes.Ingredient;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

//import java.io.File;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Optional;

public class MainUICtrl {

    @FXML private ListView<Recipe> recipeListView;

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

    @FXML private TextField RecipeSearchBar;

    private final Client client = ClientBuilder.newClient();
    private final String recipeEndpointUrl = "http://localhost:8080/api/recipes";

    private ObservableList<Recipe> currentRecipes;
    private Recipe selectedRecipe;

    private final ServerUtils server;
    private final MainCtrl mainCtrl;
    private ServerService serverService;

    private Set<Long> favoriteRecipeIds = new HashSet<>();
    private List<Recipe> allLoadedRecipes = new ArrayList<>();

    // File path for saving favorites
    private static final File FAVORITES_FILE = new File("favorites.json");

    /**
     * reads a list of favorite recipe IDs from a local file (favorites.json)
     * and loads them into the application's memory.
     */
    private void loadFavorites() {
        if (!FAVORITES_FILE.exists()) return;

        // Creates a new Jackson ObjectMapper instance, the tool used to
        // convert between Java objects and JSON text
        ObjectMapper mapper = new ObjectMapper();
        try {
            // Reads the file content and converts the JSON data into a Set of Long integers.
            // The TypeReference is required because Set<Long> is a generic type,
            // and Java needs this extra help to understand exactly what type of collection to create.
            favoriteRecipeIds = mapper.readValue(FAVORITES_FILE, new TypeReference<Set<Long>>(){});
        } catch (IOException e) {
            e.printStackTrace(); // Handle error properly in production
        }
    }

    /**
     *  takes the current set of favorite recipe IDs from memory and
     *  writes them into the favorites.json file.
     */
    private void saveFavorites() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(FAVORITES_FILE, favoriteRecipeIds);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML private ToggleButton favoriteButton;
    @FXML private ToggleButton allRecipesTab;
    @FXML private ToggleButton favoritesTab;

    @FXML
    public void toggleFavorite() {
        Recipe current = recipeListView.getSelectionModel().getSelectedItem();
        if (current == null) return;

        if (favoriteButton.isSelected()) {
            favoriteRecipeIds.add(current.getId());
            favoriteButton.setText("★");
        } else {
            boolean removed = favoriteRecipeIds.remove(current.getId());
            favoriteButton.setText("☆");
        }

        saveFavorites();

        if (favoritesTab.isSelected()) {
            // Re-calculate the list of favorites
            List<Recipe> favorites = currentRecipes.stream()
                    .filter(r -> favoriteRecipeIds.contains(r.getId()))
                    .collect(Collectors.toList());

            // Set the new list (this physically removes the item from the UI)
            recipeListView.setItems(FXCollections.observableArrayList(favorites));

            // Optional: Select the first item or clear selection so the details panel doesn't look weird
            if (!favorites.isEmpty()) {
                recipeListView.getSelectionModel().select(0);
            } else {
                // clear details panel if list is empty
            }
        } else {
            recipeListView.refresh(); // Just redraw the stars if in "All Recipes"
        }
    }


    @FXML
    public void showAllRecipes() {
        recipeListView.setItems(FXCollections.observableArrayList(currentRecipes));
    }

    @FXML
    public void showFavoriteRecipes() {

        List<Recipe> favorites = new ArrayList<>();
        for (Recipe r : currentRecipes) {
            if (favoriteRecipeIds.contains(r.getId())) {
                favorites.add(r);
            } else {

            }
        }
        recipeListView.setItems(FXCollections.observableArrayList(favorites));
    }

    /**
     * A constructor for the MainUI Controller class, using dependency injection
     * @param server - the injected instance of the ServerUtils
     * @param mainCtrl - the injected instance of the Main Controller
     */
    @Inject
    public MainUICtrl (ServerUtils server, MainCtrl mainCtrl) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.serverService = new RealServerService();
    }

    public void setServerService(ServerService serverService) {
        this.serverService = serverService;
    }

    /**
     * Initializes the main UI by loading all recipes
     */
    @FXML
    public void initialize() {

        // The recipe list matches the initial server one
        this.currentRecipes = WebSocketService.getInstance().getMasterRecipeList();
        // Immediately set currentRecipes to match the changes from the server
        // The recipe list view gets updated every time a change from the server is sensed
        currentRecipes.addListener((ListChangeListener<Recipe>) c -> {
            Platform.runLater(this::updateRecipeListView);
        });

        initialRecipeFetch();

        loadFavorites();

        allRecipesTab.setSelected(true);

        // Sidebar selection updates main UI
        recipeListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedRecipe = newVal;
                displayRecipe(newVal);
                boolean isFav = favoriteRecipeIds.contains(newVal.getId());
                favoriteButton.setSelected(isFav);
                favoriteButton.setText(isFav ? "★" : "☆");
            }
        });

        // Top bar buttons
        exitButton.setOnAction(e -> System.exit(0));
        minimizeButton.setOnAction(e -> ((Stage) exitButton.getScene().getWindow()).setIconified(true));
        maximizeButton.setOnAction(e -> {
            Stage stage = (Stage) exitButton.getScene().getWindow();
            stage.setMaximized(!stage.isMaximized());
        });
        refreshButton.setOnAction(e -> initialRecipeFetch());

        // Bottom sidebar buttons
        addRecipeButton.setOnAction(e -> openCreateRecipeUI());
        deleteRecipeButton.setOnAction(e -> handleDeleteRecipe());

        // Placeholders for edit buttons
        editNameButton.setOnAction(e -> handleEditName());
        editIngredientsButton.setOnAction(e -> {
            if (selectedRecipe != null) {
                handleEditIngredients(selectedRecipe);
            } else {
                System.out.println("Please select a recipe to edit");
            }
        });

        editInstructionsButton.setOnAction(e -> {
            if (selectedRecipe != null) {
                handleEditInstructions(selectedRecipe);
            } else {
                System.out.println("Please select a recipe to edit");
            }
        });
        foodItemButton.setOnAction(e -> handleGoToFoodItems());


        RecipeSearchBar.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ESCAPE) {
                RecipeSearchBar.clear();
                initialRecipeFetch();
            }
        });

        // Tell the list: "When you have a Recipe, just show its name (and maybe a star!)"
        recipeListView.setCellFactory(param -> new ListCell<Recipe>() {
            @Override
            protected void updateItem(Recipe item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null || item.getName() == null) {
                    setText(null);
                } else {
                    // Check if it's a favorite to add the star prefix
                    boolean isFavorite = favoriteRecipeIds.contains(item.getId());
                    String prefix = isFavorite ? "★ " : "";

                    setText(prefix + item.getName());
                }
            }
        });
    }

    /**
     * Loads the recipes initially from the master list using a standard GET request
     */
    public void initialRecipeFetch () {
        // Gets all initial recipes from the server
        List <Recipe> fetched = server.getAllRecipes();

        // The master recipe list is set to match the server ones
        // The initial display of the recipes is also triggered
        Platform.runLater(() -> WebSocketService.getInstance().
                getMasterRecipeList().setAll(fetched));
        if (!currentRecipes.isEmpty()) {
            selectedRecipe = currentRecipes.getFirst();
            recipeListView.getSelectionModel().select(0);
            displayRecipe(currentRecipes.getFirst());
        } else {
            clearRecipeDisplay();
        }
    }

    /**
     * Refreshes the List View
     */
    public void updateRecipeListView () {

        if (favoritesTab.isSelected()) {
            // If we are in Favorites tab, run the filter immediately!
            showFavoriteRecipes();
        } else {
            // Only show all recipes if we are in "All Recipes" tab
            // Updates the sidebar when it senses changes in the database
            recipeListView.getItems().setAll(currentRecipes);

            // Updates the recipe with all of its parameters
            if (selectedRecipe != null) {
                for (Recipe currentRecipe : currentRecipes) {
                    if (currentRecipe.getId().equals(selectedRecipe.getId())) {
                        selectedRecipe = currentRecipe;
                        displayRecipe(selectedRecipe);
                        break;
                    }
                }
            }
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
                    } else {
                        System.out.println("Failed to delete recipe: " + serverResponse.getStatus());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Handles the functionality of the Edit Name button
     */
    private void handleEditName() {

        refreshSelectedRecipe();

        Dialog <String> dialogBox = new Dialog<>();
        dialogBox.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Button okButton = (Button) dialogBox.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(false);

        TextField nameField = new TextField(selectedRecipe.getName());
        Label nameLabel = new Label ("Name:");

        HBox hBox = new HBox();
        hBox.setSpacing(15);
        hBox.setPadding(new Insets(20));
        hBox.getChildren().addAll(nameLabel, nameField);
        dialogBox.getDialogPane().setContent(hBox);

        ChangeListener<String> emptyNameValidator = (obs, old, newVal) -> {
            okButton.setDisable(nameField.getText().isEmpty());
        };

        dialogBox.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                return nameField.getText();
            }
            return null;
        });

        int indexClicked = currentRecipes.indexOf(selectedRecipe);

        dialogBox.showAndWait().ifPresent(result -> {
            if (indexClicked >= 0) {
                selectedRecipe.setName(result);
                server.updateRecipe(selectedRecipe);
            }
        });
    }

    private void handleEditIngredients(Recipe recipe) {
        if(recipe == null){
            System.out.println("Recipe is null");
            return;
        }
        mainCtrl.showEditIngredients(recipe);
    }

    private void handleEditInstructions(Recipe recipe) {
        if(recipe == null){
            System.out.println("Recipe is null");
            return;
        }
        mainCtrl.showEditInstructions(recipe);
    }

    private void handleGoToFoodItems() {
        mainCtrl.showFoodItemMain();
    }

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
    public void downloadRecipe() {
        int index = recipeListView.getSelectionModel().getSelectedIndex();
        if (index < 0 || index >= currentRecipes.size()) return;
        Recipe selectedRecipe = currentRecipes.get(index);

        String content = "Recipe: " + selectedRecipe.getName() + "\n\n";
        content += "Ingredients:\n";

        if (selectedRecipe.getIngredients() != null) {
            for (int i = 0; i < selectedRecipe.getIngredients().size(); i++) {
                content += String.valueOf(i + 1) + ". ";
                content += selectedRecipe.getIngredients().get(i).toString();
                content += "\n";
            }
        }

        content += "Instructions:\n";

        if (selectedRecipe.getInstructions() != null) {
            for (int i = 0; i < selectedRecipe.getInstructions().size(); i++) {
                content += "Step " + String.valueOf(i + 1) + ": ";
                content += selectedRecipe.getInstructions().get(i);
                content += "\n";
            }
        }

        /* THIS COULD POSSIBLY BE USED SO THAT THE USER IS ASKED WHERE TO SAVE THE FILE INSTEAD OF BLINDLY SAVING IT INTO THE
        PROJECT FOLDER

        FileChooser fileChooser = new  FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Recipe", "*.txt"));  //ensures the file is saved as txt

        Stage stage = (Stage) addRecipeButton.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
         */

        try (FileWriter fw = new FileWriter(selectedRecipe.getName() + ".txt")) {
            fw.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void cloneRecipe(){
        int index = recipeListView.getSelectionModel().getSelectedIndex();
        if (index < 0 || index >= currentRecipes.size()) return;
        Recipe selectedRecipe = currentRecipes.get(index);

        TextInputDialog dialog = new TextInputDialog("Copy of " + selectedRecipe.getName());
        dialog.setTitle("Clone Recipe");
        dialog.setHeaderText("Create a new copy");
        dialog.setContentText("Enter a name for the new recipe");

        Optional<String> result = dialog.showAndWait();

        if(result.isPresent()){
            String newName = result.get();
            Recipe cloneRecipe =  new Recipe(selectedRecipe);
            cloneRecipe.setName(newName);
            serverService.sendRecipe(cloneRecipe);
        }
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

    /**
     * A method for refreshing the selected recipe, so the parameter can be safely used
     */
    public void refreshSelectedRecipe() {
        int selectedIndex = recipeListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < currentRecipes.size()) {
            selectedRecipe = currentRecipes.get(selectedIndex);
        }
    }

    public void searchForRecipe() {
        String searchQuery = RecipeSearchBar.getText().trim();

        if (searchQuery.isEmpty()) {
            recipeListView.getSelectionModel().clearSelection();
            recipeListView.getItems().clear();
            currentRecipes.clear();
            return;
        }

        String[] recipeTerms = searchQuery.split(",");
        List<String> trimmedRecipeTerms = new ArrayList<>();
        for(String term : recipeTerms){
            String editedTerm = term.trim();
            if (!editedTerm.isEmpty()){
                trimmedRecipeTerms.add(editedTerm);
            }
        }

        List<Recipe> recipes = null;
        for (String term : trimmedRecipeTerms) {
            List<Recipe> termResults = server.searchRecipesByTerm(term);

            if (recipes == null) {
                recipes = termResults;
            } else {
                recipes.retainAll(termResults);
            }
        }

        if (favoritesTab.isSelected()) {
            recipes = recipes.stream()
                    .filter(r -> favoriteRecipeIds.contains(r.getId()))
                    .collect(Collectors.toList());
        }

        currentRecipes = (ObservableList<Recipe>) recipes;

        recipeListView.getSelectionModel().clearSelection();
        recipeListView.getItems().setAll(currentRecipes);
    }

}
