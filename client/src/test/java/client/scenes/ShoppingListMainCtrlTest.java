package client.scenes;

import client.utils.ServerUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class ShoppingListMainCtrlTest extends ApplicationTest {

    private ShoppingListMainCtrl shoppingListMainCtrl;
    private ServerUtils serverUtils;
    private MainCtrl mainCtrl;

    @BeforeAll
    public static void skipWithoutDisplay () {
        String display = System.getenv("DISPLAY");
        Assumptions.assumeTrue(display != null && !display.isEmpty(),
                "Skipping JavaFX UI tests because DISPLAY is not available (CI)");
    }

    @Override
    public void start (Stage stage) throws IOException {

        serverUtils = mock(ServerUtils.class);
        mainCtrl = mock(MainCtrl.class);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/scenes/ShoppingListMainUI.fxml"));

        shoppingListMainCtrl = new ShoppingListMainCtrl(serverUtils, mainCtrl);
        loader.setController(shoppingListMainCtrl);

        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.show();
    }

    @Test
    public void testBack () {
        clickOn("#returnToMainButton");
        Node editNameButton = lookup("#editNameButton").query();
        assertNotNull(editNameButton, "The Recipe Name should be present - we are in the Main UI");
    }

}
