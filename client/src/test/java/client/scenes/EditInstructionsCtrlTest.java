package client.scenes;

import javafx.application.Platform;
import javafx.scene.control.ListView;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions; // Import this!
import recipes.Recipe;

import java.awt.GraphicsEnvironment;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EditInstructionsCtrlTest {

    /*
     I used AI for the following method because I was not sure how I can
     test with JavaFx components but I understand it
     */
    @BeforeAll
    static void initJFX() {
        // Check if there is no screen
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }

        //Start JavaFX only if there is a screen
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Toolkit already initialized
        }
    }

    @Test
    void initDataLoadsRecipeInstructionsCorrectly() {
        // Skip this test if no screen
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "Skipping UI test on headless server");

        EditInstructionsCtrl ctrl = new EditInstructionsCtrl(null, null);
        ctrl.instructionList = new ListView<>();

        Recipe recipe = new Recipe("test");
        recipe.setInstructions(List.of("Cut", "Bake"));

        ctrl.initializeData(recipe);

        assertEquals(2, ctrl.instructionList.getItems().size());
        assertEquals("Cut", ctrl.instructionList.getItems().get(0));
        assertEquals("Bake", ctrl.instructionList.getItems().get(1));
    }
}