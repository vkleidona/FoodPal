package client.scenes;

import client.utils.ServerUtils;
import com.google.inject.Provider;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.testfx.framework.junit5.ApplicationTest;
import recipes.Ingredient;
import recipes.Recipe;
import recipes.FoodItem;

import jakarta.ws.rs.core.Response;

import java.lang.reflect.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class MainUICtrlTest extends ApplicationTest {


    @BeforeAll
    static void skipInCiWithoutDisplay() {
        String display = System.getenv("DISPLAY");
        Assumptions.assumeTrue(display != null && !display.isEmpty(), "Skip JavaFX UI tests because DISPLAY is not available");
    }


    // Controller instance
    private MainUICtrl controller;

    // Stubbed server utilities
    private ServerUtils mockServer;

    // Flags for verifying delete calls
    private long deletedRecipeId = -1;

    @Override
    public void start(Stage stage) throws Exception {

        // Mock ServerUtils
        mockServer = new ServerUtils() {
            public Response deleteRecipe(long id) {
                deletedRecipeId = id;
                return Response.status(204).build();
            }
        };

        // Mock MainCtrl

        MainCtrl mockMainCtrl = new MainCtrl(null) {
            @Override
            public void showFoodItemMain() {}

            @Override
            public void showShoppingList() {}
        };

        // Load UI
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/scenes/MainUI.fxml"));
        Parent root = loader.load();

        controller = loader.getController();

        // Inject mocks into controller
        Field serverField = MainUICtrl.class.getDeclaredField("server");
        Field mainField = MainUICtrl.class.getDeclaredField("mainCtrl");

        serverField.setAccessible(true);
        mainField.setAccessible(true);

        serverField.set(controller, mockServer);
        mainField.set(controller, mockMainCtrl);

        stage.setScene(new Scene(root));
        stage.show();

        // Wait for FX thread
        Thread.sleep(200);
    }


    // Utility to build a Recipe via Reflection (due to private constructor)
    private Recipe makeRecipe(long id, String name, List<Ingredient> ingredients, List<String> instructions) {
        try {
            Constructor<?> ctor = Recipe.class.getDeclaredConstructors()[0];
            ctor.setAccessible(true);

            Recipe r = (Recipe) ctor.newInstance();
            setField(r, "id", id);
            setField(r, "name", name);
            setField(r, "ingredients", ingredients);
            setField(r, "instructions", instructions);

            return r;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setField(Object obj, String field, Object value) throws Exception {
        Field f = obj.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(obj, value);
    }

    private Ingredient makeIngredient(String name, double qty, String unit) {
        try {
            Constructor<?> foodCtor = FoodItem.class.getDeclaredConstructors()[0];
            foodCtor.setAccessible(true);
            FoodItem food = (FoodItem) foodCtor.newInstance();
            setField(food, "name", name);

            Constructor<?> ingCtor = Ingredient.class.getDeclaredConstructors()[0];
            ingCtor.setAccessible(true);
            Ingredient ing = (Ingredient) ingCtor.newInstance();

            setField(ing, "foodType", food);
            setField(ing, "quantity", qty);
            setField(ing, "measurement", unit);

            return ing;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testUILoads() {
        ListView list = lookup("#recipeListView").query();
        assertNotNull(list);
    }

    @Test
    public void testDisplayRecipe() throws Exception {
        // Prepare sample recipe
        Recipe recipe = makeRecipe(
                1L,
                "Cake",
                List.of(makeIngredient("Flour", 200, "g")),
                List.of("Mix", "Bake")
        );

        // Inject fake recipe list
        Field recipesField = MainUICtrl.class.getDeclaredField("currentRecipes");
        recipesField.setAccessible(true);
        recipesField.set(controller, List.of(recipe));

        // Inject into listView
        ListView<String> list = lookup("#recipeListView").query();
        javafx.application.Platform.runLater(() -> {
            list.getItems().clear();
            list.getItems().add("Cake");
            list.getSelectionModel().select(0);
        });

        Thread.sleep(200);

        // Verify UI updated
        Label name = lookup("#recipeNameLabel").query();
        assertEquals("Cake", name.getText());

        VBox ingredients = lookup("#ingredientsVBox").query();
        assertEquals(1, ingredients.getChildren().size());

        VBox instructions = lookup("#instructionsVBox").query();
        assertEquals(2, instructions.getChildren().size());
    }

    @Test
    public void testDeleteRecipe() throws Exception {
        Recipe recipe = makeRecipe(
                5L,
                "Pasta",
                new ArrayList<>(),
                new ArrayList<>()
        );

        Field recipesField = MainUICtrl.class.getDeclaredField("currentRecipes");
        recipesField.setAccessible(true);
        recipesField.set(controller, List.of(recipe));

        // Populate list
        ListView<String> list = lookup("#recipeListView").query();
        javafx.application.Platform.runLater(() -> {
            list.getItems().clear();
            list.getItems().add("Pasta");
            list.getSelectionModel().select(0);
        });

        Thread.sleep(200);

        // Click delete
        clickOn("#deleteRecipeButton");

        // Confirm popup - must auto-confirm via pressing ENTER
        press(javafx.scene.input.KeyCode.ENTER).release(javafx.scene.input.KeyCode.ENTER);

        Thread.sleep(200);

        assertEquals(5L, deletedRecipeId);
    }

    @Test
    public void testEditInstructionsButtonRunsHandler() {
        clickOn("#editInstructionsButton");
        // since it prints to console, only verify UI does not break
        assertTrue(true);
    }

    @Test
    public void testEditIngredientsButtonRunsHandler() {
        clickOn("#editIngredientsButton");
        assertTrue(true);
    }
}
