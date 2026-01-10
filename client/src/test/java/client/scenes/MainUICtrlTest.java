package client.scenes;

import client.utils.ServerUtils;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import recipes.Recipe;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MainUICtrlTest extends ApplicationTest {


    private MainUICtrl controller;
    private MockServerUtils mockServer;
    private MockMainCtrl mockMainCtrl;

    // UI Elements
    private ListView<Recipe> recipeListView;
    private Label recipeNameLabel;
    private Label servingsLabel;
    private ToggleButton favoriteButton;
    private ToggleButton allRecipesTab;
    private ToggleButton favoritesTab;
    private VBox ingredientsVBox;
    private VBox instructionsVBox;

    // Buttons
    private Button addRecipeButton;
    private Button deleteRecipeButton;
    private Button editNameButton;
    private Button editIngredientsButton;
    private Button editInstructionsButton;
    private Button foodItemButton;
    private Button shoppingListButton;
    private Button addToShoppingListButton;
    private Button exitButton;
    private Button minimizeButton;
    private Button maximizeButton;
    private Button refreshButton;

    @BeforeAll
    static void skipInCiWithoutDisplay() {
        String display = System.getenv("DISPLAY");
        Assumptions.assumeTrue(display != null && !display.isEmpty(),
                "Skipping JavaFX UI tests because DISPLAY is not available (CI)");
    }

    @Override
    public void start(Stage stage) {
        // 1. Initialize Mocks
        mockServer = new MockServerUtils();
        mockMainCtrl = new MockMainCtrl();

        // 2. Initialize Controller
        controller = new MainUICtrl(mockServer, mockMainCtrl, null);

        // 3. Initialize UI Components
        recipeListView = new ListView<>();
        recipeNameLabel = new Label();
        servingsLabel = new Label();
        favoriteButton = new ToggleButton("☆");
        allRecipesTab = new ToggleButton("All Recipes");
        favoritesTab = new ToggleButton("Favorites");
        ingredientsVBox = new VBox();
        instructionsVBox = new VBox();

        addRecipeButton = new Button("Add");
        deleteRecipeButton = new Button("Delete");
        editNameButton = new Button("Edit Name");
        editIngredientsButton = new Button("Edit Ingred.");
        editInstructionsButton = new Button("Edit Instr.");
        foodItemButton = new Button("Food Items");
        shoppingListButton = new Button("Shopping List");
        addToShoppingListButton = new Button("Add to List");
        exitButton = new Button("X");
        minimizeButton = new Button("-");
        maximizeButton = new Button("+");
        refreshButton = new Button("Refresh");

        // 4. Inject components via Reflection
        injectField("recipeListView", recipeListView);
        injectField("recipeNameLabel", recipeNameLabel);
        injectField("servingsLabel", servingsLabel);
        injectField("favoriteButton", favoriteButton);
        injectField("allRecipesTab", allRecipesTab);
        injectField("favoritesTab", favoritesTab);
        injectField("ingredientsVBox", ingredientsVBox);
        injectField("instructionsVBox", instructionsVBox);

        injectField("addRecipeButton", addRecipeButton);
        injectField("deleteRecipeButton", deleteRecipeButton);
        injectField("editNameButton", editNameButton);
        injectField("editIngredientsButton", editIngredientsButton);
        injectField("editInstructionsButton", editInstructionsButton);
        injectField("foodItemButton", foodItemButton);
        injectField("shoppingListButton", shoppingListButton);
        injectField("addToShoppingListButton", addToShoppingListButton);
        injectField("exitButton", exitButton);
        injectField("minimizeButton", minimizeButton);
        injectField("maximizeButton", maximizeButton);
        injectField("refreshButton", refreshButton);

        // 5. Manually Wire Actions (Simulating FXML behavior)
        // Public methods:
        favoriteButton.setOnAction(e -> controller.toggleFavorite());
        allRecipesTab.setOnAction(e -> controller.showAllRecipes());
        favoritesTab.setOnAction(e -> controller.showFavoriteRecipes());

        // Private methods (Need reflection helper to wire):
        shoppingListButton.setOnAction(e -> invokePrivateMethod("handleGoToShoppingList"));
        addToShoppingListButton.setOnAction(e -> invokePrivateMethod("handleAddRecipeToShoppingList"));

        // 6. Initialize Controller logic
        controller.initialize();

        // 7. Build Scene
        VBox root = new VBox(
                new HBox(allRecipesTab, favoritesTab),
                recipeListView,
                recipeNameLabel, favoriteButton,
                addRecipeButton, foodItemButton, shoppingListButton,
                editIngredientsButton, editInstructionsButton
        );
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.show();
    }

    @AfterEach
    public void cleanup() {
        // Delete test files
        File f = new File("favorites.json");
        if (f.exists()) f.delete();

        File r = new File("Pancakes.txt");
        if (r.exists()) r.delete();
    }

    @Test
    public void testLoadRecipes() {
        WaitForAsyncUtils.waitForFxEvents();
        assertFalse(recipeListView.getItems().isEmpty());
        assertEquals(2, recipeListView.getItems().size());
        assertEquals("Pancakes", recipeListView.getItems().get(0).getName());
    }

    @Test
    public void testSelectRecipeUpdatesDetails() {
        WaitForAsyncUtils.waitForFxEvents();
        Platform.runLater(() -> recipeListView.getSelectionModel().select(0));
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals("Pancakes", recipeNameLabel.getText());
        assertEquals("4", servingsLabel.getText());
    }

    @Test
    public void testFavoriteToggle() {
        WaitForAsyncUtils.waitForFxEvents();
        Platform.runLater(() -> recipeListView.getSelectionModel().select(0));
        WaitForAsyncUtils.waitForFxEvents();

        assertFalse(favoriteButton.isSelected());
        assertEquals("☆", favoriteButton.getText());

        clickOn(favoriteButton);
        WaitForAsyncUtils.waitForFxEvents();

        assertTrue(favoriteButton.isSelected());
        assertEquals("★", favoriteButton.getText());

        // Verify it persists in Favorites Tab
        Platform.runLater(() -> {
            favoritesTab.setSelected(true);
            controller.showFavoriteRecipes();
        });
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(1, recipeListView.getItems().size());
        assertEquals("Pancakes", recipeListView.getItems().get(0).getName());
    }

    @Test
    public void testTabSwitching() {
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(2, recipeListView.getItems().size()); // All

        Platform.runLater(() -> {
            favoritesTab.setSelected(true);
            controller.showFavoriteRecipes();
        });
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(0, recipeListView.getItems().size()); // Favorites (empty)

        Platform.runLater(() -> {
            allRecipesTab.setSelected(true);
            controller.showAllRecipes();
        });
        WaitForAsyncUtils.waitForFxEvents();
        assertEquals(2, recipeListView.getItems().size()); // All again
    }

    @Test
    public void testNavigationButtons() {
        WaitForAsyncUtils.waitForFxEvents();

        clickOn(addRecipeButton);
        assertTrue(mockMainCtrl.createRecipeShown, "Add Button should show Create UI");

        clickOn(foodItemButton);
        assertTrue(mockMainCtrl.foodItemMainShown, "Food Item Button should show Food UI");

        clickOn(shoppingListButton);
        assertTrue(mockMainCtrl.shoppingListShown, "Shopping List Button should show Shopping List");
    }

    @Test
    public void testEditButtonsWithSelection() {
        WaitForAsyncUtils.waitForFxEvents();

        // Select Pancakes
        Platform.runLater(() -> recipeListView.getSelectionModel().select(0));
        WaitForAsyncUtils.waitForFxEvents();

        clickOn(editIngredientsButton);
        assertTrue(mockMainCtrl.editIngredientsShown, "Should open Edit Ingredients");

        clickOn(editInstructionsButton);
        assertTrue(mockMainCtrl.editInstructionsShown, "Should open Edit Instructions");
    }

    @Test
    public void testEditButtonsNoSelection() {
        WaitForAsyncUtils.waitForFxEvents();
        Platform.runLater(() -> recipeListView.getSelectionModel().clearSelection());
        WaitForAsyncUtils.waitForFxEvents();

        clickOn(editIngredientsButton);
        assertFalse(mockMainCtrl.editIngredientsShown, "Should NOT open without selection");
    }

    @Test
    public void testDownloadRecipe() {
        WaitForAsyncUtils.waitForFxEvents();
        Platform.runLater(() -> recipeListView.getSelectionModel().select(0));
        WaitForAsyncUtils.waitForFxEvents();

        // Call download directly as it might not be a button on the main view
        Platform.runLater(() -> controller.downloadRecipe());
        WaitForAsyncUtils.waitForFxEvents();

        File file = new File("Pancakes.txt");
        assertTrue(file.exists(), "Recipe file should be created");
    }


    private void injectField(String fieldName, Object value) {
        try {
            Field field = controller.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(controller, value);
        } catch (Exception e) {
            throw new RuntimeException("Injection failed: " + fieldName, e);
        }
    }

    private void invokePrivateMethod(String methodName) {
        try {
            Method method = controller.getClass().getDeclaredMethod(methodName);
            method.setAccessible(true);
            method.invoke(controller);
        } catch (Exception e) {
            throw new RuntimeException("Method invocation failed: " + methodName, e);
        }
    }

    static class MockServerUtils extends ServerUtils {
        public MockServerUtils() { super(); } // Use super(null) if needed

        @Override
        public List<Recipe> getAllRecipes() {
            List<Recipe> recipes = new ArrayList<>();

            Recipe r1 = new Recipe("Pancakes");
            r1.setId(101L);
            r1.setServings(4);
            r1.setIngredients(new ArrayList<>());
            r1.setInstructions(new ArrayList<>());

            Recipe r2 = new Recipe("Salad");
            r2.setId(102L);
            r2.setServings(2);
            r2.setIngredients(new ArrayList<>());
            r2.setInstructions(new ArrayList<>());

            recipes.add(r1);
            recipes.add(r2);
            return recipes;
        }

        @Override
        public Recipe updateRecipe(Recipe r) { return r; }
    }

    static class MockMainCtrl extends MainCtrl {
        public boolean createRecipeShown = false;
        public boolean editIngredientsShown = false;
        public boolean editInstructionsShown = false;
        public boolean shoppingListShown = false;
        public boolean addRecipeToShoppingListShown = false;
        public boolean infoShown = false;
        public boolean foodItemMainShown = false;

        public MockMainCtrl() { super(null, null); }

        @Override public void showCreateRecipe() { createRecipeShown = true; }
        @Override public void showEditIngredients(Recipe r) { editIngredientsShown = true; }
        @Override public void showEditInstructions(Recipe r) { editInstructionsShown = true; }
        @Override public void showFoodItemMain() { foodItemMainShown = true; }
        @Override public void showShoppingList() { shoppingListShown = true; }
        @Override public void showAddRecipeToShoppingList() { addRecipeToShoppingListShown = true; }
        @Override public void showInformation(Recipe r) { infoShown = true; }
    }
}
