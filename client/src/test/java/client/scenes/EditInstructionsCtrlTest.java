package client.scenes;

import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;
import org.testfx.framework.junit5.ApplicationTest;
import recipes.Recipe;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import client.utils.ServerUtils;

public class EditInstructionsCtrlTest extends ApplicationTest{
    private EditInstructionsCtrl ctrl;
    private ServerUtils mockServer;
    private MainCtrl mockMainCtrl;

    @BeforeAll
    static void skipInCiWithoutDisplay() {
        String display = System.getenv("DISPLAY");
        Assumptions.assumeTrue(display != null && !display.isEmpty(),
                "Skipping JavaFX UI tests because DISPLAY is not available (CI)");
    }

    /**
     * This method starts the JavaFX application for each test
     */

    @Override
    public void start(Stage stage) throws Exception{
        mockServer = mock(ServerUtils.class);
        mockMainCtrl = mock(MainCtrl.class);

        FXMLLoader loader =  new FXMLLoader(getClass().getResource("/client/scenes/EditInstructions.fxml"));

        loader.setControllerFactory(_ -> new EditInstructionsCtrl(mockServer, mockMainCtrl));

        Parent root =  loader.load();
        ctrl = loader.getController();

        Recipe testRecipe = new Recipe("Pizza");
        testRecipe.setInstructions(new ArrayList<>(List.of("Cut", "Bake")));
        ctrl.initialize(testRecipe);

        stage.setScene(new Scene(root));
        stage.show();
    }

    @Test
    public void testInitializeData(){
        assertEquals(2, ctrl.getInstructionList().getItems().size());
        assertEquals("Cut", ctrl.getInstructionList().getItems().get(0));
        assertEquals("Bake", ctrl.getInstructionList().getItems().get(1));
    }

    @Test
    public void testAddInstruction(){
        clickOn("Add");
        write("New Step");
        clickOn("OK");

        assertEquals(3, ctrl.getInstructionList().getItems().size());
        assertEquals("New Step", ctrl.getInstructionList().getItems().get(2));
    }
}