package client.scenes;

import client.network.WebSocketService;
import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.util.Pair;
import client.MyFXML;
import recipes.FoodItem;
import recipes.Recipe;

public class MainCtrl {

    private Stage primaryStage;

    private final MyFXML fxml;


    private ObservableList<Recipe> recipes;
    private ObservableList <FoodItem> foodItems;

    private final RecipeActions recipeActions;


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

    private final WebSocketService webSocketService;

    /**
     * A constructor for the MainCtrl class using Dependency Injection
     * @param fxml - the FXML module which loads the scenes
     * @param webSocketService - the instance of the WebSocket support service
     * @param recipeActions - the instance of the recipe server utilities
     */
    @Inject
    public MainCtrl(MyFXML fxml, WebSocketService webSocketService, RecipeActions recipeActions) {
        this.fxml = fxml;
        this.webSocketService = webSocketService;
        this.recipeActions = recipeActions;
    }

    /**
     * Sets up the MainCtrl class with all UIs and WebSockets
     * @param primaryStage - the Stage which hosts the current Scene
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
    public void initialize(Stage primaryStage, Pair<MainUICtrl, Parent> mainMenu,
                           Pair<EditInstructionsCtrl, Parent>
                                   editInstructions, Pair<CreateRecipeCtrl, Parent> createRecipe,
                           Pair<AddFoodItemCtrl, Parent>
                                   addFoodItem, Pair<FoodItemMainCtrl, Parent> foodItemMain,
                           Pair<EditIngredientsCtrl, Parent> editIngredients,
                                    Pair <ShoppingListMainCtrl, Parent> shoppingListMain,
                                    Pair <AddRecipeToShoppingListCtrl, Parent>
                                   addRecipeToShoppingList,
                                    Pair <InformationCtrl, Parent> information) {

        this.recipes = webSocketService.getMasterRecipeList();
        this.foodItems = webSocketService.getMasterFoodItemsList();

        this.primaryStage = primaryStage;

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

    /**
     * A method used to switch to the nutritional values scene
     * @param recipe - the recipe whose nutritional values have to be initialized
     */
    public void showInformation(Recipe recipe) {
        if (informationCtrl != null) {
            informationCtrl.setRecipe(recipe); // <--- PASS THE DATA
        }
        primaryStage.setTitle("Nutritional Information: " + recipe.getName());
        primaryStage.setScene(informationScene);
    }

    /**
     * A method used to display the main menu
     */
    public void showMainMenu() {
        primaryStage.setTitle("FoodPal: Main Menu");
        primaryStage.setScene(mainMenu);
    }

    /**
     * A method used to show the screen for editing instructions
     * @param recipe - the recipe whose instructions need to be edited
     */
    public void showEditInstructions(Recipe recipe) {
        editInstructionsCtrl.initialize(recipe);
        primaryStage.setTitle("Edit Instructions");
        primaryStage.setScene(editInstructions);
    }

    /**
     * A method used to show the screen for editing ingredients
     * @param recipe - the recipe whose ingredients need to be edited
     */
    public void showEditIngredients(Recipe recipe) {
        editIngredientsCtrl.initialize(recipe);
        primaryStage.setTitle("Edit Ingredients");
        primaryStage.setScene(editIngredients);
    }

    /**
     * A method for switching to the scene for creating recipes
     */
    public void showCreateRecipe() {
        primaryStage.setTitle("Create Recipe");
        primaryStage.setScene(createRecipe);
    }

    /**
     * A method for switching to the scene for adding a new food item
     */
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

    /**
     * A method which switches to the scene for displaying all food items
     */
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
            webSocketService.removeItemFromList(id);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Food Item removed!");
            alert.setHeaderText("Selected food item removed");
            alert.setContentText("The food item you are currently editing has been removed");
            alert.showAndWait();

            showFoodItemMain();
        });
    }

    /**
     * Reloads all main scenes in the currently selected language.
     */
    public void reloadScenesForNewLanguage() {
        reloadMainScenes();
        reloadAdditionalScenes();
        // Finally, show the main menu again
        showMainMenu();
    }


    /**
     * A helper method for reloading all scenes of the basic requirements
     */
    public void reloadMainScenes () {
        // Main UI
        Pair<MainUICtrl, Parent> mainPair =
                fxml.load(MainUICtrl.class, "client", "scenes", "MainUI.fxml");
        this.mainUICtrl = mainPair.getKey();
        this.mainMenu = new Scene(mainPair.getValue());

        // Create Recipe
        Pair<CreateRecipeCtrl, Parent> createRecipePair =
                fxml.load(CreateRecipeCtrl.class, "client", "scenes", "creatingRecipe.fxml");
        this.createRecipeCtrl = createRecipePair.getKey();
        this.createRecipe = new Scene(createRecipePair.getValue());
        this.createRecipeCtrl.setRecipeActions(this.recipeActions);
        this.createRecipeCtrl.setMainCtrl(this);

        // Edit Ingredients
        Pair<EditIngredientsCtrl, Parent> editIngredientsPair =
                fxml.load(EditIngredientsCtrl.class, "client", "scenes", "EditIngredients.fxml");
        this.editIngredientsCtrl = editIngredientsPair.getKey();
        this.editIngredients = new Scene(editIngredientsPair.getValue());

        // Edit Instructions
        Pair<EditInstructionsCtrl, Parent> editInstructionsPair =
                fxml.load(EditInstructionsCtrl.class, "client", "scenes", "EditInstructions.fxml");
        this.editInstructionsCtrl = editInstructionsPair.getKey();
        this.editInstructions = new Scene(editInstructionsPair.getValue());
    }

    /**
     * A helper method for reloading all scenes of the additional requirements
     */
    public void reloadAdditionalScenes () {
        // Add Food Item
        Pair<AddFoodItemCtrl, Parent> addFoodItemPair =
                fxml.load(AddFoodItemCtrl.class, "client", "scenes", "AddFoodItem.fxml");
        this.addFoodItemCtrl = addFoodItemPair.getKey();
        this.addFoodItem = new Scene(addFoodItemPair.getValue());

        // Add Recipe To Shopping List
        Pair<AddRecipeToShoppingListCtrl, Parent> addRecipeToShoppingListPair =
                fxml.load(AddRecipeToShoppingListCtrl.class,
                        "client", "scenes", "AddRecipeToShoppingList.fxml");
        this.addRecipeToShoppingListCtrl = addRecipeToShoppingListPair.getKey();
        this.addRecipeToShoppingList = new Scene(addRecipeToShoppingListPair.getValue());

        // Food Item Main
        Pair<FoodItemMainCtrl, Parent> foodItemMainPair =
                fxml.load(FoodItemMainCtrl.class, "client", "scenes", "FoodItemMain.fxml");
        this.foodItemMainCtrl = foodItemMainPair.getKey();
        this.foodItemMain = new Scene(foodItemMainPair.getValue());

        // Information UI
        Pair<InformationCtrl, Parent> informationPair =
                fxml.load(InformationCtrl.class, "client", "scenes", "InformationUI.fxml");
        this.informationCtrl = informationPair.getKey();
        this.informationScene = new Scene(informationPair.getValue());

        // Shopping List Main UI
        Pair<ShoppingListMainCtrl, Parent> shoppingListMainPair =
                fxml.load(ShoppingListMainCtrl.class,
                        "client", "scenes", "ShoppingListMainUI.fxml");
        this.shoppingListMainCtrl = shoppingListMainPair.getKey();
        this.shoppingListMainScene = new Scene(shoppingListMainPair.getValue());
    }

    /**
     * A testing constructor for the MainCtrl
     */
    public MainCtrl () {
        this(null, new WebSocketService(), null);
    }
}