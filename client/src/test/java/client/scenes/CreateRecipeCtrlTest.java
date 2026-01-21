package client.scenes;

import client.scenes.main.CreateRecipeCtrl;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import recipes.Recipe;
import static org.junit.jupiter.api.Assertions.*;

public class CreateRecipeCtrlTest extends ApplicationTest {

    @BeforeAll
    static void skipInCiWithoutDisplay() {
        String display = System.getenv("DISPLAY");
        Assumptions.assumeTrue(display != null && !display.isEmpty(),
                "Skipping JavaFX UI tests because DISPLAY is not available (CI)");
    }



    private CreateRecipeCtrl controller;

    // these fields capture the data sent to the server
    private boolean wasRecipeSent = false;
    private Recipe capturedRecipe = null;

    @Override
    public void start(Stage stage) throws Exception {
        // load the FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/scenes/creatingRecipe.fxml"));
        Parent root = loader.load();

        // the controller instance
        controller = loader.getController();

        controller.setServerService(new ServerService() {
            @Override
            public void sendRecipe(Recipe recipe) {
                wasRecipeSent = true;
                capturedRecipe = recipe;
            }

            @Override
            public java.util.List<recipes.FoodItem> getAllFoodItems() {
                return new java.util.ArrayList<>();
            }

        });

        stage.setScene(new Scene(root));
        stage.show();
    }


    @Test
    public void testInvalidQuantityAlert() {
        clickOn("#nameTextField").write("Bad Quantity Recipe");

        clickOn("#addIngredientButton");
        VBox ingredientsBox = lookup("#ingredientsVBox").query();
        HBox row = (HBox) ingredientsBox.getChildren().getFirst();

        clickOn(row.getChildren().get(0)).write("Flour");
        clickOn(row.getChildren().get(1)).write("NotANumber"); // <--- Logic Error Here

        clickOn("#addPreparationButton");
        VBox prepBox = lookup("#preparationVBox").query();
        HBox stepRow = (HBox) prepBox.getChildren().getFirst();
        clickOn(stepRow.getChildren().getFirst()).write("Mix everything.");

        clickOn("#applyButton");

        clickOn("OK");

        // verify Recipe was NOT sent
        assertFalse(wasRecipeSent, "Recipe should NOT be sent with invalid quantity string");
    }


    @Test
    public void testUnitSystemSwitching() {
        // adding an ingredient row so we have a ChoiceBox to inspect
        clickOn("#addIngredientButton");

        // get the row and the ChoiceBox
        VBox ingredientsBox = lookup("#ingredientsVBox").query();
        HBox row = (HBox) ingredientsBox.getChildren().getFirst();

        @SuppressWarnings("unchecked")
        ChoiceBox<String> unitChoice = (ChoiceBox<String>) row.getChildren().get(2);

        clickOn(row.getChildren().get(0));

        // click Imperial Radio Button
        clickOn("#imperialRadioButton");

        // wait for UI update
        WaitForAsyncUtils.waitForFxEvents();

        // verify contents are Imperial
        assertTrue(unitChoice.getItems().contains("oz"), "Imperial list should contain 'oz'");
        assertTrue(unitChoice.getItems().contains("cup"), "Imperial list should contain 'cup'");
        assertFalse(unitChoice.getItems().contains("g"), "Imperial list should NOT contain 'g'");
        assertEquals("oz", unitChoice.getValue(), "Default imperial unit should be 'oz'");

        // click Metric Radio Button
        clickOn("#metricRadioButton");

        WaitForAsyncUtils.waitForFxEvents();

        // verify contents are Metric
        assertTrue(unitChoice.getItems().contains("g"), "Metric list should contain 'g'");
        assertTrue(unitChoice.getItems().contains("ml"), "Metric list should contain 'ml'");
        assertFalse(unitChoice.getItems().contains("cup"), "Metric list should NOT contain 'cup'");
        assertEquals("g", unitChoice.getValue(), "Default metric unit should be 'g'");

        // click Kitchen Radio Button
        clickOn("#kitchenRadioButton");

        WaitForAsyncUtils.waitForFxEvents();

        assertTrue(unitChoice.getItems().contains("pinch"), "Kitchen list should contain 'pinch'");
        assertEquals("stick", unitChoice.getValue(), "Default kitchen unit should be 'stick'");
    }

    @Test
    public void testAddIngredientAddsRow() {
        // verify initial state
        VBox ingredientsBox = lookup("#ingredientsVBox").query();
        assertEquals(0, ingredientsBox.getChildren().size());

        // click Add
        clickOn("#addIngredientButton");

        // wait for JavaFX to process UI events
        javafx.application.Platform.runLater(() -> {}); // schedule a no-op
        try {
            Thread.sleep(200); // small pause to let UI update
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // verify row added
        assertEquals(1, ingredientsBox.getChildren().size());
    }

    @Test
    public void testAddPreparationAddsRow() {
        VBox prepBox = lookup("#preparationVBox").query();
        assertEquals(0, prepBox.getChildren().size());

        clickOn("#addPreparationButton");

        assertEquals(1, prepBox.getChildren().size());
    }

    @Test
    public void testApplySendsCorrectData() {
        clickOn("#nameTextField").write("Test Chocolate Cake");


        clickOn("#addIngredientButton");
        VBox ingredientsBox = lookup("#ingredientsVBox").query();
        HBox row = (HBox) ingredientsBox.getChildren().getFirst();

        clickOn(row.getChildren().get(0)).write("Flour");
        clickOn(row.getChildren().get(1)).write("500");


        clickOn(row.getChildren().get(2));
        // click the item "g" in the popup list
        clickOn("g");

        clickOn("#addPreparationButton");
        VBox prepBox = lookup("#preparationVBox").query();
        HBox stepRow = (HBox) prepBox.getChildren().getFirst();
        clickOn(stepRow.getChildren().getFirst()).write("Mix everything.");

        clickOn("#applyButton");

        // verify Data
        assertTrue(wasRecipeSent, "The sendRecipe method should have been called");
        assertNotNull(capturedRecipe, "A valid Recipe object should have been sent");

        assertEquals("Test Chocolate Cake", capturedRecipe.getName());
        assertEquals(1, capturedRecipe.getIngredients().size());

        // verify Ingredient fields
        var ingredient = capturedRecipe.getIngredients().getFirst();
        assertEquals("Flour", ingredient.getFoodType().getName());
        assertEquals(500, ingredient.getQuantity());
        assertEquals("g", ingredient.getMeasurement()); // Verify unit matches
    }

    @Test
    public void testMissingNameAlert() {
        clickOn("#applyButton");
        clickOn("OK");
        assertFalse(wasRecipeSent, "Recipe should NOT be sent if name is missing");
    }

    @Test
    public void testIncompleteRecipeAlert_Cancel() {
        clickOn("#nameTextField").write("Empty Recipe");

        clickOn("#applyButton");

        clickOn("Cancel");

        assertFalse(wasRecipeSent, "Recipe should NOT be sent if user clicks Cancel");
    }

    @Test
    public void testIncompleteRecipeAlert_Proceed() {
        clickOn("#nameTextField").write("Just Name Recipe");
        clickOn("#applyButton");

        // click OK to confirm saving anyway
        clickOn("OK");

        // verify SENT
        assertTrue(wasRecipeSent, "Recipe SHOULD be sent if user clicks OK");
    }

    /**
     * Tests that the 'x' button correctly removes an ingredient row.
     */
    @Test
    public void testDeleteIngredient() {
        // Add two ingredients
        clickOn("#addIngredientButton");
        clickOn("#addIngredientButton");

        VBox ingredientsBox = lookup("#ingredientsVBox").query();
        assertEquals(2, ingredientsBox.getChildren().size(), "Should have 2 rows initially");

        // Click the 'x' button on the first row
        // The structure is HBox -> [Combo, Text, Choice, Text, Text, Button]
        // The delete button is the last child in the HBox
        HBox firstRow = (HBox) ingredientsBox.getChildren().getFirst();
        clickOn(firstRow.getChildren().getLast());

        // Wait for UI update
        WaitForAsyncUtils.waitForFxEvents();

        assertEquals(1, ingredientsBox.getChildren().size(), "One row should remain after deletion");
    }

    @Test
    public void testBuildRecipeHelper() {
        java.util.List<String> names = java.util.Arrays.asList("Apple", "Banana");
        java.util.List<Integer> quantities = java.util.Arrays.asList(1, 2);
        java.util.List<String> units = java.util.Arrays.asList("piece", "kg");
        java.util.List<String> instructions = java.util.Arrays.asList("Step 1", "Step 2");

        Recipe r = controller.buildRecipe("Test Helper", names, quantities, units, instructions);

        assertNotNull(r);
        assertEquals("Test Helper", r.getName());
        assertEquals(2, r.getIngredients().size());
        assertEquals("Apple", r.getIngredients().getFirst().getFoodType().getName());
        assertEquals(1, r.getIngredients().getFirst().getQuantity());
        assertEquals("piece", r.getIngredients().getFirst().getMeasurement());
        assertEquals(2, r.getInstructions().size());
        assertEquals("Step 1", r.getInstructions().getFirst());
    }

    @Test
    public void testHandleRefreshUpdatesComboBoxes() {
        // 1. Add an ingredient row
        clickOn("#addIngredientButton");

        // 2. Inject a new ServerService that returns a specific item
        controller.setServerService(new ServerService() {
            @Override
            public java.util.List<recipes.FoodItem> getAllFoodItems() {
                java.util.List<recipes.FoodItem> list = new java.util.ArrayList<>();
                list.add(new recipes.FoodItem("New Server Item", 0,0,0,0));
                return list;
            }
            @Override public void sendRecipe(Recipe r) {}
        });

        // 3. Call handleRefresh explicitly (since we might not have a button for it in the test UI)
        javafx.application.Platform.runLater(() -> controller.handleRefresh());
        WaitForAsyncUtils.waitForFxEvents();

        // 4. Verify the ComboBox now contains "New Server Item"
        VBox ingredientsBox = lookup("#ingredientsVBox").query();
        HBox row = (HBox) ingredientsBox.getChildren().getFirst();
        @SuppressWarnings("unchecked")
        ComboBox<String> nameBox = (ComboBox<String>) row.getChildren().getFirst();

        assertTrue(nameBox.getItems().contains("New Server Item"), "ComboBox should contain updated items from server");
        assertTrue(nameBox.getItems().contains("+ Create New Food Item..."), "ComboBox should still have create option");
    }

}
