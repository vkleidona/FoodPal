package client.scenes;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import recipes.FoodItem;
import recipes.Ingredient;
import recipes.Recipe;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ApplicationExtension.class)
public class InformationCtrlTest extends ApplicationTest {

    private InformationCtrl controller;
    private boolean mainMenuShown = false;

    @BeforeAll
    static void skipInCiWithoutDisplay() {
        String display = System.getenv("DISPLAY");
        Assumptions.assumeTrue(display != null && !display.isEmpty(),
                "Skipping JavaFX UI tests because DISPLAY is not available (CI)");
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/scenes/InformationUI.fxml"));

        loader.setControllerFactory(param -> {
            MainCtrl mockMainCtrl = new MainCtrl() {
                @Override
                public void showMainMenu() {
                    mainMenuShown = true;
                }
            };
            return new InformationCtrl(mockMainCtrl);
        });

        // load the FXML
        Parent root = loader.load();

        controller = loader.getController();


        stage.setScene(new Scene(root));
        stage.show();
    }

    private Recipe createTestRecipe() {
        Recipe r = new Recipe("Test Recipe");
        r.setServings(2);

        // FoodItem: 100kcal per 100g
        FoodItem apple = new FoodItem("Apple", 100, 0.5, 10, 0.5);

        // Ingredient: 200g of Apple (so 200kcal total for the whole recipe)
        Ingredient ing1 = new Ingredient(apple, 200L, "g");
        ing1.setQuantityInGrams(200);

        List<Ingredient> list = new ArrayList<>();
        list.add(ing1);
        r.setIngredients(list);
        return r;
    }

    @Test
    public void testSetRecipePopulatesFields() {
        Recipe r = createTestRecipe();

        Platform.runLater(() -> controller.setRecipe(r));
        WaitForAsyncUtils.waitForFxEvents();

        // Verify Servings Field
        TextField servingsField = lookup("#servingsField").query();
        assertEquals("2", servingsField.getText());

        // Verify Total Calories (200g * 1kcal/g = 200 kcal)
        TextField caloriesRecipe = lookup("#caloriesRecipe").query();
        assertEquals("200", caloriesRecipe.getText());

        // Verify Ingredient Row created
        VBox container = lookup("#ingredientsContainer").query();
        assertEquals(1, container.getChildren().size());

        // Verify specific label text in the row
        HBox row = (HBox) container.getChildren().get(0);
        Label nameLbl = (Label) row.getChildren().get(0);
        assertEquals("Apple", nameLbl.getText());
    }

    @Test
    public void testUnitOptimization() {
        Recipe r = new Recipe("Heavy Recipe");
        r.setServings(1);
        FoodItem rock = new FoodItem("Rock", 0,0,0,0);

        // 1500g should optimize to 1.5 kg
        Ingredient ing = new Ingredient(rock, 1500L, "g");
        ing.setQuantityInGrams(1500);
        r.setIngredients(List.of(ing));

        Platform.runLater(() -> controller.setRecipe(r));
        WaitForAsyncUtils.waitForFxEvents();

        VBox container = lookup("#ingredientsContainer").query();
        HBox row = (HBox) container.getChildren().get(0);
        TextField qtyField = (TextField) row.getChildren().get(1);
        Label unitLbl = (Label) row.getChildren().get(2);

        assertEquals("1.50", qtyField.getText());
        assertEquals("kg", unitLbl.getText());
    }

    @Test
    public void testChangeServingsUpdatesUI() {
        // Initial: 2 servings, 200g Apple
        Recipe r = createTestRecipe();
        Platform.runLater(() -> controller.setRecipe(r));
        WaitForAsyncUtils.waitForFxEvents();

        // 1. Double-click to select existing text ("2") so it gets overwritten
        doubleClickOn("#servingsField");

        // 2. Write "4" (this replaces "2" because it was selected)
        write("4");

        // 3. Press Enter to trigger onAction
        type(javafx.scene.input.KeyCode.ENTER);

        // Verify Servings changed in logic
        assertEquals(4, r.getServings());

        // Verify Ingredient Quantity scaled (200g * 2 = 400g)
        VBox container = lookup("#ingredientsContainer").query();
        HBox row = (HBox) container.getChildren().get(0);
        TextField qtyField = (TextField) row.getChildren().get(1);
        assertEquals("400", qtyField.getText());

        // Verify Total Calories scaled (200 -> 400)
        TextField caloriesRecipe = lookup("#caloriesRecipe").query();
        assertEquals("400", caloriesRecipe.getText());
    }

    @Test
    public void testChangeTotalCaloriesUpdatesServings() {
        // Initial: 2 servings, 200 kcal
        Recipe r = createTestRecipe();
        Platform.runLater(() -> controller.setRecipe(r));
        WaitForAsyncUtils.waitForFxEvents();

        // 1. Click on total calories field
        clickOn("#caloriesRecipe");
        // 2. Write "600" (Scaling factor x3)
        doubleClickOn("#caloriesRecipe").write("600");
        type(javafx.scene.input.KeyCode.ENTER);

        // Verify Servings (2 * 3 = 6)
        TextField servingsField = lookup("#servingsField").query();
        assertEquals("6", servingsField.getText());

        // Verify Ingredients (200g * 3 = 600g)
        VBox container = lookup("#ingredientsContainer").query();
        HBox row = (HBox) container.getChildren().get(0);
        TextField qtyField = (TextField) row.getChildren().get(1);
        assertEquals("600", qtyField.getText());
    }

    @Test
    public void testIngredientChangeUpdatesTotals() {
        // Initial: 2 servings, 200g Apple (200kcal)
        Recipe r = createTestRecipe();
        Platform.runLater(() -> controller.setRecipe(r));
        WaitForAsyncUtils.waitForFxEvents();

        // Find the quantity text field for the first ingredient
        VBox container = lookup("#ingredientsContainer").query();
        HBox row = (HBox) container.getChildren().get(0);
        TextField qtyField = (TextField) row.getChildren().get(1);

        // Click specifically on that node
        clickOn(qtyField).write("400"); // Append "400" (resulting in 200400)? No, let's clear first.

        // Safer approach for internal components without IDs: directly set text and fire event
        Platform.runLater(() -> {
            qtyField.setText("400");
            qtyField.fireEvent(new javafx.event.ActionEvent());
        });
        WaitForAsyncUtils.waitForFxEvents();

        // Doubled quantity -> Doubled servings -> Doubled total calories
        TextField servingsField = lookup("#servingsField").query();
        assertEquals("4", servingsField.getText());

        TextField caloriesRecipe = lookup("#caloriesRecipe").query();
        assertEquals("400", caloriesRecipe.getText());
    }

    @Test
    public void testCancelButton() {
        clickOn("Cancel");
        assertTrue(mainMenuShown, "Cancel button should trigger return to main menu");
    }

    @Test
    public void testNutritionPer100gLogic() {
        // 100g ingredient with 50kcal/100g
        Recipe r = new Recipe("Diet");
        FoodItem item = new FoodItem("Diet Food", 50, 5, 5, 5);
        Ingredient ing = new Ingredient(item, 100L, "g");
        ing.setQuantityInGrams(100);
        r.setIngredients(List.of(ing));

        Platform.runLater(() -> controller.setRecipe(r));
        WaitForAsyncUtils.waitForFxEvents();

        Label calLabel = lookup("#caloriesLabel").query();
        // Should contain "50"
        assertTrue(calLabel.getText().contains("50"));
    }
}
