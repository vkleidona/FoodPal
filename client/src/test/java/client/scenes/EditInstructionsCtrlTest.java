package client.scenes;

import javafx.scene.control.ListView;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import recipes.Recipe;

public class EditInstructionsCtrlTest {

    //Initialize the JavaFX toolkit so that I can later test JavaFX components
    @BeforeAll
    static void initJFX() {
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException e) {
        }
    }

    @Test
    void initDataLoadsRecipeInstructionsCorrectly() {
        // If running on the server (no screen), stop so that it doesnt crash
        if (java.awt.GraphicsEnvironment.isHeadless()) {
            return;
        }

        EditInstructionsCtrl ctrl = new EditInstructionsCtrl(null, null);

        ctrl.instructionList = new ListView<>();

        Recipe recipe = new Recipe();
        recipe.setInstructions(List.of("Cut", "Bake"));

        ctrl.initData(recipe);

        assertEquals(2, ctrl.instructionList.getItems().size());
        assertEquals("Cut", ctrl.instructionList.getItems().get(0));
        assertEquals("Bake", ctrl.instructionList.getItems().get(1));
    }

}
