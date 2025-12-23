package client.scenes;

import client.utils.ServerUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.testfx.framework.junit5.ApplicationTest;
import org.mockito.Mockito;

import recipes.FoodItem;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FoodItemMainCtrlTest extends ApplicationTest {

    private FoodItemMainCtrl ctrl;
    private ServerUtils serverUtils;
    private MainCtrl mainCtrl;

    @BeforeAll
    static void skipInCiWithoutDisplay() {
        String display = System.getenv("DISPLAY");
        Assumptions.assumeTrue(display != null && !display.isEmpty(),
                "Skipping JavaFX UI tests because DISPLAY is not available (CI)");
    }

    @Override
    public void start(Stage stage) throws Exception {
        // Mock dependencies
        serverUtils = mock(ServerUtils.class);
        mainCtrl = mock(MainCtrl.class);

        // Load FXML and set controller
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/scenes/FoodItemMain.fxml"));
        ctrl = new FoodItemMainCtrl(mainCtrl, serverUtils);
        loader.setController(ctrl);
        Parent root = loader.load();

        stage.setScene(new Scene(root));
        stage.show();
    }

    @Test
    public void testInitialSelectionShowsNoDetails() {
        ctrl.updateFoodDetails(null);

        assertEquals("Select food item to see details", ctrl.foodNameLabel.getText());
        assertEquals("", ctrl.recipeUsages.getText());
        assertEquals("", ctrl.nutritionLabel.getText());
    }

    @Test
    public void testUpdateFoodDetailsShowsCorrectInfo() {
        FoodItem item = new FoodItem();
        item.setId(1L);
        item.setName("Apple");
        item.setKcal(52);
        item.setCarbs(14);
        item.setProteins(0);
        item.setFats(0);

        when(serverUtils.getFoodItemRecipeUsage(item)).thenReturn(2);

        ctrl.updateFoodDetails(item);

        assertEquals("Food Name: Apple", ctrl.foodNameLabel.getText());
        assertEquals("Used in 2 recipe(s)", ctrl.recipeUsages.getText());
        assertEquals("Kcal: 52", ctrl.kcalLabel.getText());
        assertEquals("Carbohydrates: 14", ctrl.carbsLabel.getText());
        assertEquals("Proteins: 0", ctrl.proteinsLabel.getText());
        assertEquals("Fats: 0", ctrl.fatsLabel.getText());
    }

    @Test
    public void testRefreshFoodItemsUpdatesList() {
        FoodItem item1 = new FoodItem();
        item1.setName("Banana");

        FoodItem item2 = new FoodItem();
        item2.setName("Orange");

        List<FoodItem> items = Arrays.asList(item1, item2);
        when(serverUtils.getAllFoodItems()).thenReturn(items);

        ctrl.refreshFoodItems();

        assertEquals(2, ctrl.foodItemList.getItems().size());
        assertEquals("Banana", ctrl.foodItemList.getItems().get(0).getName());
    }
}
