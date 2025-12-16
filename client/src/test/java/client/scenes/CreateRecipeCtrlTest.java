package client.scenes;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
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

            @Override
            public java.util.List<recipes.FoodItem> getFoodItems() {
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
        HBox row = (HBox) ingredientsBox.getChildren().get(0);

        clickOn(row.getChildren().get(0)).write("Flour");
        clickOn(row.getChildren().get(1)).write("NotANumber"); // <--- Logic Error Here

        clickOn("#addPreparationButton");
        VBox prepBox = lookup("#preparationVBox").query();
        HBox stepRow = (HBox) prepBox.getChildren().get(0);
        clickOn(stepRow.getChildren().get(0)).write("Mix everything.");

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
        HBox row = (HBox) ingredientsBox.getChildren().get(0);

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
        HBox row = (HBox) ingredientsBox.getChildren().get(0);

        clickOn(row.getChildren().get(0)).write("Flour");
        clickOn(row.getChildren().get(1)).write("500");


        clickOn(row.getChildren().get(2));
        // click the item "g" in the popup list
        clickOn("g");

        clickOn("#addPreparationButton");
        VBox prepBox = lookup("#preparationVBox").query();
        HBox stepRow = (HBox) prepBox.getChildren().get(0);
        clickOn(stepRow.getChildren().get(0)).write("Mix everything.");

        clickOn("#applyButton");

        // verify Data
        assertTrue(wasRecipeSent, "The sendRecipe method should have been called");
        assertNotNull(capturedRecipe, "A valid Recipe object should have been sent");

        assertEquals("Test Chocolate Cake", capturedRecipe.getName());
        assertEquals(1, capturedRecipe.getIngredients().size());

        // verify Ingredient fields
        var ingredient = capturedRecipe.getIngredients().get(0);
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






/*
    @Test
    public void testCancelClearsForm() {
        // Type something
        clickOn("#nameTextField").write("Mistake Recipe");
        verifyThat("#nameTextField", hasText("Mistake Recipe"));

        // Click Cancel
        clickOn("#cancelButton");

        // Verify it is gone
        verifyThat("#nameTextField", hasText(""));
    }

 */
}
