package client.utils;

import client.network.WebSocketService;
import client.scenes.food_item.AddFoodItemCtrl;
import client.scenes.food_item.FoodItemMainCtrl;
import client.scenes.main.CreateRecipeCtrl;
import client.scenes.main.EditIngredientsCtrl;
import client.scenes.main.EditInstructionsCtrl;
import client.scenes.main.MainUICtrl;
import client.scenes.nutrition.InformationCtrl;
import client.scenes.shopping_list.AddRecipeToShoppingListCtrl;
import client.scenes.shopping_list.ShoppingListMainCtrl;
import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import client.i18n.LanguageManager;
import javafx.stage.Stage;
import javafx.util.Pair;
import client.MyFXML;
import recipes.FoodItem;
import recipes.Recipe;

public class MainCtrl {

    private enum Screen {
        MAIN_MENU,
        EDIT_INSTRUCTIONS,
        CREATE_RECIPE,
        ADD_FOOD_ITEM,
        FOOD_ITEM_MAIN,
        EDIT_INGREDIENTS,
        INFORMATION,
        SHOPPING_LIST_MAIN,
        ADD_RECIPE_TO_SHOPPING_LIST
    }

    private Recipe selectedRecipeBeforeReload;

    public void setSelectedRecipeForReload(Recipe recipe) {
        this.selectedRecipeBeforeReload = recipe;
    }


    private Screen currentScreen = Screen.MAIN_MENU;

    private Stage primaryStage;

    private final MyFXML fxml;


    private ObservableList<Recipe> recipes;
    private ObservableList <FoodItem> foodItems;

    //screen 1
    private MainUICtrl mainUICtrl;
    private Scene mainMenu;

    //screen 2
    private EditInstructionsCtrl editInstructionsCtrl;
    private Scene editInstructions;

    //screen 3
    private Scene createRecipe;
    private CreateRecipeCtrl createRecipeCtrl;

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

    private boolean cameFromEditIngredients;
    private boolean cameFromFoodItemMain;
    private boolean cameFromShoppingList;
    private boolean cameFromAddRecipeToShoppingList;

    /**
     * A constructor for the MainCtrl class using Dependency Injection
     * @param fxml - the FXML module which loads the scenes
     * @param webSocketService - the instance of the WebSocket support service
     */
    @Inject
    public MainCtrl(MyFXML fxml, WebSocketService webSocketService) {
        this.fxml = fxml;
        this.webSocketService = webSocketService;
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

        this.createRecipe = new Scene(createRecipe.getValue());
        this.createRecipeCtrl = createRecipe.getKey();

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
            informationCtrl.setRecipe(recipe);
        }
        selectedRecipeBeforeReload = recipe;
        currentScreen = Screen.INFORMATION;
        primaryStage.setTitle("Nutritional Information: " + recipe.getName());
        primaryStage.setScene(informationScene);
    }

    /**
     * A method used to display the main menu
     */
    public void showMainMenu() {
        currentScreen = Screen.MAIN_MENU;
        primaryStage.setTitle("FoodPal: Main Menu");
        primaryStage.setScene(mainMenu);
    }

    /**
     * A method used to show the screen for editing instructions
     * @param recipe - the recipe whose instructions need to be edited
     */
    public void showEditInstructions(Recipe recipe) {
        editInstructionsCtrl.initialize(recipe);
        selectedRecipeBeforeReload = recipe;
        currentScreen = Screen.EDIT_INSTRUCTIONS;
        primaryStage.setTitle("Edit Instructions");
        primaryStage.setScene(editInstructions);
    }

    /**
     * A method used to show the screen for editing ingredients
     * @param recipe - the recipe whose ingredients need to be edited
     */
    public void showEditIngredients(Recipe recipe) {
        editIngredientsCtrl.initialize(recipe);
        selectedRecipeBeforeReload = recipe;
        currentScreen = Screen.EDIT_INGREDIENTS;
        primaryStage.setTitle("Edit Ingredients");
        primaryStage.setScene(editIngredients);
    }

    /**
     * A method for switching to the scene for creating recipes
     */
    public void showCreateRecipe() {
        currentScreen = Screen.CREATE_RECIPE;
        primaryStage.setTitle("Create Recipe");
        primaryStage.setScene(createRecipe);
    }

    /**
     * A method for switching to the scene for creating recipes
     */
    public void showCreateRecipeFromFoodItem() {
        currentScreen = Screen.CREATE_RECIPE;
        createRecipeCtrl.restoreDraft(draftRecipe);
    }

    /**
     * A method for switching to the scene for adding a new food item
     */
    public void showAddFoodItem() {
        currentScreen = Screen.ADD_FOOD_ITEM;
        primaryStage.setTitle("Add Food Item");
        primaryStage.setScene(addFoodItem);
    }

    /**
     * Switches to the Add FoodItem entry from the ShoppingList
     * @param foodItemEntry - the name of the entered ingredient in the ShoppingList
     */
    public void showAddFoodItemFromShoppingList(String foodItemEntry) {
        primaryStage.setTitle("Add Food Item");
        currentScreen = Screen.SHOPPING_LIST_MAIN;
        addFoodItemCtrl.accessFromTheShoppingList(foodItemEntry);
        primaryStage.setScene(addFoodItem);
    }

    /**
     * Switches to the Add FoodItem entry from the ShoppingList
     * @param foodItemEntry - the name of the entered ingredient in the ShoppingList
     */
    public void showAddFoodItemFromAddRecipeToShoppingList(String foodItemEntry) {
        primaryStage.setTitle("Add Food Item");
        currentScreen = Screen.ADD_RECIPE_TO_SHOPPING_LIST;
        addFoodItemCtrl.accessFromAddRecipeToTheShoppingList(foodItemEntry);
        primaryStage.setScene(addFoodItem);
    }

    /**
     * A method which switches to the scene for displaying all food items
     */
    public void showFoodItemMain() {
        currentScreen = Screen.FOOD_ITEM_MAIN;
        primaryStage.setTitle("Food Item Main");
        primaryStage.setScene(foodItemMain);
    }


    /**
     * Switches to the Shopping List scene
     */
    public void showShoppingList() {
        currentScreen = Screen.SHOPPING_LIST_MAIN;
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
        currentScreen = Screen.ADD_RECIPE_TO_SHOPPING_LIST;
        primaryStage.setTitle("Add Recipe to Shopping List");
        Recipe currentRecipe = mainUICtrl.getSelectedRecipe();
        addRecipeToShoppingListCtrl.setRecipe(currentRecipe);
        primaryStage.setScene(addRecipeToShoppingList);
    }


    /**
     * Opens the add to shopping list screen populated with the provided
     * scaled recipe.
     * @param recipeToAdd The scaled recipe object to be passed to the view.
     */
    public void showAddScaledRecipeToShoppingList(Recipe recipeToAdd){
        primaryStage.setTitle("Add Recipe to Shopping List");
        addRecipeToShoppingListCtrl.setRecipe(recipeToAdd);
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
    public void handleRecipeBeingDeleted(Long id) {
        Platform.runLater(() -> {
            webSocketService.removeRecipeFromList(id);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(LanguageManager.getString("main.recipeRemoved.title"));
            alert.setHeaderText(LanguageManager.getString("main.recipeRemoved.header"));
            alert.setContentText(LanguageManager.getString("main.recipeRemoved.content"));
            alert.showAndWait();

            showMainMenu();
        });
    }



    /**
     * A method which handles the active food item being deleted
     * @param id - the food item id being deleted
     */
    public void handleFoodItemBeingDeleted(Long id) {
        Platform.runLater(() -> {
            webSocketService.removeItemFromList(id);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(LanguageManager.getString("main.foodItemRemoved.title"));
            alert.setHeaderText(LanguageManager.getString("main.foodItemRemoved.header"));
            alert.setContentText(LanguageManager.getString("main.foodItemRemoved.content"));
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

        switch (currentScreen) {
            case MAIN_MENU -> showMainMenu();
            case EDIT_INSTRUCTIONS -> {
                if (selectedRecipeBeforeReload != null) {
                    showEditInstructions(selectedRecipeBeforeReload);
                } else {
                    showMainMenu();
                }
            }
            case CREATE_RECIPE -> showCreateRecipe();
            case ADD_FOOD_ITEM -> showAddFoodItem();
            case FOOD_ITEM_MAIN -> showFoodItemMain();
            case EDIT_INGREDIENTS -> {
                if (selectedRecipeBeforeReload != null) {
                    showEditIngredients(selectedRecipeBeforeReload);
                } else {
                    showMainMenu();
                }
            }
            case INFORMATION -> {
                if (selectedRecipeBeforeReload != null) {
                    showInformation(selectedRecipeBeforeReload);
                } else {
                    showMainMenu();
                }
            }
            case SHOPPING_LIST_MAIN -> showShoppingList();
            case ADD_RECIPE_TO_SHOPPING_LIST -> showAddRecipeToShoppingList();
        }
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
        this.createRecipe = new Scene(createRecipePair.getValue());

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
     * A helper method to remember if the user accessed a screen
     * through the edit ingredients screen.
     * @param cameFromEditIngredients - indicates whether the user came
     *                               from the Edit Ingredients Menu
     */
    public void setCameFromEditIngredients(boolean cameFromEditIngredients){
        this.cameFromEditIngredients = cameFromEditIngredients;
    }

    /**
     * Checks whether the previous screen was edit ingredients.
     * @return - returns true if a screen was accessed through the edit ingredients screen.
     * Otherwise, it returns false.
     */
    public boolean isCameFromEditIngredients(){
        return cameFromEditIngredients;
    }

    public void setCameFromFoodItemMain(boolean cameFromFoodItemMain) {
        this.cameFromFoodItemMain = cameFromFoodItemMain;
    }

    public boolean isCameFromFoodItemMain() {
        return cameFromFoodItemMain;
    }

    public boolean isCameFromShoppingList() {
        return cameFromShoppingList;
    }

    public void setCameFromShoppingList(boolean cameFromShoppingList) {
        this.cameFromShoppingList = cameFromShoppingList;
    }

    public void setCameFromAddRecipeToShoppingList(boolean cameFromAddRecipeToShoppingList) {
        this.cameFromAddRecipeToShoppingList = cameFromAddRecipeToShoppingList;
    }

    public boolean isCameFromAddRecipeToShoppingList() {
        return cameFromAddRecipeToShoppingList;
    }

    public void resetOriginFlags(){
        cameFromAddRecipeToShoppingList = false;
        cameFromEditIngredients = false;
        cameFromFoodItemMain = false;
        cameFromShoppingList = false;
        setDraftRecipe(null);
    }

    /**
     * A testing constructor for the MainCtrl
     */
    public MainCtrl () {
        this(null, new WebSocketService());
    }
}