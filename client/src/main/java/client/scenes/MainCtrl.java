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

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Pair;
import recipes.Recipe;

public class MainCtrl {

    private Stage primaryStage;

    private QuoteOverviewCtrl overviewCtrl;
    private Scene overview;

    private AddQuoteCtrl addCtrl;
    private Scene add;

    // ADD THIS FIELD
    private final RecipeActions recipeActions;

    // ADD THIS CONSTRUCTOR
    @com.google.inject.Inject
    public MainCtrl(RecipeActions recipeActions) {
        this.recipeActions = recipeActions;
    }

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

    //screen 6
    private InformationCtrl informationCtrl;
    private Scene informationScene;

    private ShoppingListMainCtrl shoppingListMainCtrl;
    private Scene shoppingListMainScene;

    public void initialize(Stage primaryStage, Pair<QuoteOverviewCtrl, Parent> overview,
                           Pair<AddQuoteCtrl, Parent> add, Pair<MainUICtrl, Parent> mainMenu, Pair<EditInstructionsCtrl, Parent>
                                   editInstructions, Pair<CreateRecipeCtrl, Parent> createRecipe, Pair<AddFoodItemCtrl, Parent>
                                   addFoodItem, Pair<FoodItemMainCtrl, Parent> foodItemMain,
                                    Pair <ShoppingListMainCtrl, Parent> shoppingListMain, Pair<InformationCtrl, Parent> information) {
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

        this.shoppingListMainCtrl = shoppingListMain.getKey();
        this.shoppingListMainScene = new Scene(shoppingListMain.getValue());

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

    public void showCreateRecipe() {
        primaryStage.setTitle("Create Recipe");
        primaryStage.setScene(createRecipe);
    }

    public void showAddFoodItem() {
        primaryStage.setTitle("Add Food Item");
        primaryStage.setScene(addFoodItem);
    }

    public void showFoodItemMain() {
        primaryStage.setTitle("Food Item Main");
        primaryStage.setScene(foodItemMain);
    }

    public void showShoppingList() {
        primaryStage.setTitle("Shopping List");
        primaryStage.setScene(shoppingListMainScene);
    }

    public AddFoodItemCtrl getAddFoodItemCtrl() {
        return addFoodItemCtrl;
    }

    public FoodItemMainCtrl getFoodItemMainCtrl() {
    return foodItemMainCtrl;
    }
}