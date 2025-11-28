package client.scenes;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import recipes.Recipe;
import static org.junit.jupiter.api.Assertions.*;

//@Disabled("UI TestFX tests do not run in CI, run locally only")
public class CreateRecipeCtrlTest extends ApplicationTest {

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

        // inject the server implementation
        // This overrides the "RealServerService" so we don't hit the network
        controller.setServerService(new ServerService() {
            @Override
            public void sendRecipe(Recipe recipe) {
                wasRecipeSent = true;
                capturedRecipe = recipe; // Capture the recipe to verify it later
            }
        });

        stage.setScene(new Scene(root));
        stage.show();
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
        // fill in the Name
        clickOn("#nameTextField").write("Test Chocolate Cake");

        // add an Ingredient Row
        clickOn("#addIngredientButton");
        VBox ingredientsBox = lookup("#ingredientsVBox").query();
        Parent row = (Parent) ingredientsBox.getChildren().get(0);

        // fill in Ingredient Details
        // HBox children order: [Name, Quantity, Unit, Button]
        clickOn(row.getChildrenUnmodifiable().get(0)).write("Flour");
        clickOn(row.getChildrenUnmodifiable().get(1)).write("500");
        clickOn(row.getChildrenUnmodifiable().get(2)).write("grams");

        // click Apply
        clickOn("#applyButton");

        // verify that the server received the data
        assertTrue(wasRecipeSent, "The sendRecipe method should have been called");
        assertNotNull(capturedRecipe, "A valid Recipe object should have been sent");

        assertEquals("Test Chocolate Cake", capturedRecipe.getName());
        assertEquals(1, capturedRecipe.getIngredients().size());
        assertEquals("Flour", capturedRecipe.getIngredients().get(0).getFoodType().getName());
        assertEquals(500, capturedRecipe.getIngredients().get(0).getQuantity());
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
