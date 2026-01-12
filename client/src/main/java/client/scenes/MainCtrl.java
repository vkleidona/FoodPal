/*
 * Copyright 2021 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package client.scenes;

import client.Main;
import client.network.WebSocketService;
import client.utils.ServerUtils;
import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import recipes.FoodItem;
import recipes.Recipe;

public class MainCtrl implements MainCtrlMethods {

    private Stage primaryStage;

    private ObservableList<Recipe> recipes;
    private ObservableList <FoodItem> foodItems;

    private QuoteOverviewCtrl overviewCtrl;
    private Scene overview;

    private AddQuoteCtrl addCtrl;
    private Scene add;

    // ADD THIS FIELD
    private final RecipeActions recipeActions;

    // ADD THIS CONSTRUCTOR

    //screen 1
    private MainUICtrl mainUICtrl;
    private Scene mainMenu;

    //screen 2
    private EditInstructionsCtrl editInstructionsCtrl;
    private Scene editInstructions;

    //screen 3
    private CreateRecipeCtrl createRecipeCtrl;
    private Scene createRecipe;

    //screen 4
    private AddFoodItemCtrl addFoodItemCtrl;
    private Scene addFoodItem;

    //screen 5
    private FoodItemMainCtrl foodItemMainCtrl;
    private Scene foodItemMain;

    private EditIngredientsCtrl editIngredientsCtrl;
    private Scene editIngredients;

    //screen 6
    private InformationCtrl informationCtrl;
    private Scene informationScene;

    private ShoppingListMainCtrl shoppingListMainCtrl;
    private Scene shoppingListMainScene;

    private AddRecipeToShoppingListCtrl addRecipeToShoppingListCtrl;
    private Scene addRecipeToShoppingList;

    private WebSocketService webSocketService;
    @Inject
    public MainCtrl(WebSocketService webSocketService, RecipeActions recipeActions) {
        this.webSocketService = webSocketService;
        this.recipeActions = recipeActions;
    }

    /**
     * Sets up the MainCtrl class with all UIs and WebSockets
     * @param primaryStage - the Stage which hosts the current Scene
     * @param overview - redundant
     * @param add - redundant
     * @param mainMenu - Main Scene of the FoodPal app
     * @param editInstructions - the Scene for editing instructions
     * @param createRecipe - the Scene for creating a recipe
     * @param addFoodItem - the Scene for adding a FoodItem
     * @param foodItemMain - the Scene for seeing all FoodItems
     * @param editIngredients - the Scene to edit all Ingredients
     * @param shoppingListMain - the Scene to see the ShoppingList
     * @param addRecipeToShoppingList - the Scene to add a recipe to the ShoppingList
     * @param information - the Information Scene for each Recipe
     */
    public void initialize(Stage primaryStage, Pair<QuoteOverviewCtrl, Parent> overview,
                           Pair<AddQuoteCtrl, Parent> add, Pair<MainUICtrl, Parent> mainMenu,
                           Pair<EditInstructionsCtrl, Parent>
                                   editInstructions, Pair<CreateRecipeCtrl, Parent> createRecipe,
                           Pair<AddFoodItemCtrl, Parent>
                                   addFoodItem, Pair<FoodItemMainCtrl, Parent> foodItemMain,
                           Pair<EditIngredientsCtrl, Parent> editIngredients,
                                    Pair <ShoppingListMainCtrl, Parent> shoppingListMain,
                                    Pair <AddRecipeToShoppingListCtrl, Parent> addRecipeToShoppingList,
                                    Pair <InformationCtrl, Parent> information) {

        this.foodItems = webSocketService.getMasterFoodItemsList();
        this.recipes = webSocketService.getMasterRecipeList();

        this.primaryStage = primaryStage;
        this.overviewCtrl = overview.getKey();
        this.overview = new Scene(overview.getValue());

        this.addCtrl = add.getKey();
        this.add = new Scene(add.getValue());

        this.mainUICtrl = mainMenu.getKey();
        this.mainMenu = new Scene(mainMenu.getValue());

        this.editInstructionsCtrl = editInstructions.getKey();
        this.editInstructions = new Scene(editInstructions.getValue());

        this.createRecipeCtrl = createRecipe.getKey();
        this.createRecipe = new Scene(createRecipe.getValue());
        this.createRecipeCtrl.setRecipeActions(this.recipeActions);
        this.createRecipeCtrl.setMainCtrl(this);

        this.addFoodItemCtrl = addFoodItem.getKey();
        this.addFoodItem = new Scene(addFoodItem.getValue());

        this.foodItemMainCtrl = foodItemMain.getKey();
        this.foodItemMain = new Scene(foodItemMain.getValue());

        this.editIngredientsCtrl = editIngredients.getKey();
        this.editIngredients = new Scene(editIngredients.getValue());

        this.shoppingListMainCtrl = shoppingListMain.getKey();
        this.shoppingListMainScene = new Scene(shoppingListMain.getValue());

        this.addRecipeToShoppingListCtrl = addRecipeToShoppingList.getKey();
        this.addRecipeToShoppingList = new Scene (addRecipeToShoppingList.getValue());

        this.informationCtrl = information.getKey();
        this.informationScene = new Scene(information.getValue());

        showMainMenu();
        primaryStage.show();
    }

    private Recipe draftRecipe; // field to store the draft

    public void setDraftRecipe(Recipe draftRecipe) {
        this.draftRecipe = draftRecipe;
    }

    public Recipe getDraftRecipe() {
        return draftRecipe;
    }

    public void showOverview() {
        primaryStage.setTitle("Quotes: Overview");
        primaryStage.setScene(overview);
        overviewCtrl.refresh();
    }

    public void showAdd() {
        primaryStage.setTitle("Quotes: Adding Quote");
        primaryStage.setScene(add);
        add.setOnKeyPressed(e -> addCtrl.keyPressed(e));
    }

    public void showInformation(Recipe recipe) {
        if (informationCtrl != null) {
            informationCtrl.setRecipe(recipe); // <--- PASS THE DATA
        }
        primaryStage.setTitle("Nutritional Information: " + recipe.getName());
        primaryStage.setScene(informationScene);
    }

    public void showMainMenu() {
        primaryStage.setTitle("FoodPal: Main Menu");
        primaryStage.setScene(mainMenu);
    }

    public void showEditInstructions(Recipe recipe) {
        editInstructionsCtrl.initializeData(recipe);
        primaryStage.setTitle("Edit Instructions");
        primaryStage.setScene(editInstructions);
    }

    public void showEditIngredients(Recipe recipe) {
        editIngredientsCtrl.initialize(recipe);
        primaryStage.setTitle("Edit Ingredients");
        primaryStage.setScene(editIngredients);
    }

    public void showCreateRecipe() {
        primaryStage.setTitle("Create Recipe");
        primaryStage.setScene(createRecipe);
    }

    public void showAddFoodItem() {
        primaryStage.setTitle("Add Food Item");
        primaryStage.setScene(addFoodItem);
    }

    /**
     * Switches to the Add FoodItem entry from the ShoppingList
     * @param foodItemEntry - the name of the entered ingredient in the ShoppingList
     */
    public void showAddFoodItemFromShoppingList(String foodItemEntry) {
        primaryStage.setTitle("Add Food Item");
        addFoodItemCtrl.accessFromTheShoppingList(foodItemEntry);
        primaryStage.setScene(addFoodItem);
    }

    /**
     * Switches to the Add FoodItem entry from the ShoppingList
     * @param foodItemEntry - the name of the entered ingredient in the ShoppingList
     */
    public void showAddFoodItemFromAddRecipeToShoppingList(String foodItemEntry) {
        primaryStage.setTitle("Add Food Item");
        addFoodItemCtrl.accessFromAddRecipeToTheShoppingList(foodItemEntry);
        primaryStage.setScene(addFoodItem);
    }

    public void showFoodItemMain() {
        primaryStage.setTitle("Food Item Main");
        primaryStage.setScene(foodItemMain);
    }

    /**
     * Switches to the Shopping List scene
     */
    public void showShoppingList() {
        primaryStage.setTitle("Shopping List");
        shoppingListMainCtrl.refreshInterface();
        primaryStage.setScene(shoppingListMainScene);
    }

    /**
     * Returns to the dialog box for the ShoppingList
     * @param ingredientName - the fresh name which is applied to the ingredient
     */
    public void showShoppingListDialogBox(String ingredientName) {
        primaryStage.setTitle("Shopping List");
        shoppingListMainCtrl.restoreIngredient(ingredientName);
    }

    /**
     * Returns to the dialog box for AddRecipeToShoppingList
     * @param ingredientName - the fresh name which is applied to the ingredient
     */
    public void showAddRecipeToShoppingListDialogBox(String ingredientName) {
        primaryStage.setTitle("Add Recipe to Shopping List");
        addRecipeToShoppingListCtrl.restoreIngredient(ingredientName);
    }

    /**
     * Switches to the Interface asking for confirmation about adding
     * the recipe to the Shopping List
     */
    public void showAddRecipeToShoppingList () {
        primaryStage.setTitle("Add Recipe to Shopping List");
        Recipe currentRecipe = mainUICtrl.getSelectedRecipe();
        addRecipeToShoppingListCtrl.setRecipe(currentRecipe);
        primaryStage.setScene(addRecipeToShoppingList);
    }

    public AddFoodItemCtrl getAddFoodItemCtrl() {
        return addFoodItemCtrl;
    }

    public FoodItemMainCtrl getFoodItemMainCtrl() {
        return foodItemMainCtrl;
    }

    public ObservableList<Recipe> getRecipes() {
        return recipes;
    }

    public ObservableList<FoodItem> getFoodItems() {
        return foodItems;
    }

    /**
     * A method which handles the active recipe being changed
     * @param updated - the updated recipe
     */
    public void handleRecipeBeingUpdated (Recipe updated) {
        Platform.runLater(() -> {
            if (editIngredientsCtrl != null && primaryStage.getScene().equals(editIngredients)) {
                editIngredientsCtrl.refreshContent(updated);
            }
            if (editInstructionsCtrl != null && primaryStage.getScene().equals(editInstructions)) {
                editInstructionsCtrl.refreshContent(updated);
            }
        });
    }

    /**
     * A method which handles the active recipe being changed
     * @param updated - the updated recipe
     */
    public void handleFoodItemBeingUpdated (FoodItem updated) {
        Platform.runLater(() -> {
            if (addFoodItemCtrl != null && primaryStage.getScene().equals(addFoodItem)) {
                addFoodItemCtrl.refreshContent(updated);
            }
        });
    }

    /**
     * A method which handles the active recipe being deleted
     * @param id - the id of the recipe being deleted
     */
    public void handleRecipeBeingDeleted (Long id) {
        Platform.runLater(() -> {
            webSocketService.removeRecipeFromList(id);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Recipe removed!");
            alert.setHeaderText("Selected recipe removed");
            alert.setContentText("The recipe you are currently editing has been removed");
            alert.showAndWait();

            showMainMenu();
        });
    }

    /**
     * A method which handles the active food item being deleted
     * @param id - the food item id being deleted
     */
    public void handleFoodItemBeingDeleted (Long id) {
        Platform.runLater(() -> {

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Recipe removed!");
            alert.setHeaderText("Selected recipe removed");
            alert.setContentText("The recipe you are currently editing has been removed");
            alert.showAndWait();

            showMainMenu();
        });
    }
}