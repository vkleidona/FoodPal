package client.scenes;

import client.utils.ServerUtils;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.matcher.control.LabeledMatchers;
import recipes.FoodItem;
import recipes.Ingredient;
import shopping.ShoppingList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.testfx.api.FxAssert.verifyThat;

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
        assertNotNull(editNameButton,
                "The button to change the recipeName should be present - we are in the Main UI");
    }

    @Test
    public void testReset () {
        clickOn("#resetButton");
        ObservableList <Ingredient> ingredientsObservableList = lookup("#ingredientsObservableList").query();
        assertTrue(ingredientsObservableList.isEmpty(), "We should reset the list upon clicking reset");
    }

    @Test
    public void testPrint () throws IOException {
        ShoppingList s = ShoppingList.getShoppingList();
        s.setIngredients(new ArrayList<>());
        s.addIngredient(new Ingredient(new FoodItem("salmon"), 20, "g"));
        clickOn("#printButton");
        File file = new File("shopping_list.txt");
        assertTrue(file.exists(), "The readable file should exist");

        Path filePath = Path.of("shopping_list.txt");
        String content = Files.readString(filePath);
        assertTrue(content.contains("salmon - 20.0 g"));
    }

    @Test
    public void testAddNewIngredient () {
        ShoppingList s = ShoppingList.getShoppingList();
        s.setIngredients(new ArrayList<>());

        clickOn("#addIngredientButton");

        Button okButton = lookup("OK").query();
        assertTrue(okButton.isDisable());

        TextField foodName = lookup("#foodItemField").query();
        TextField quantity = lookup("#quantityField").query();
        ChoiceBox <String> unitChoice = lookup("#unitChoice").query();

        //In my implementation, all types of measurements are "merged"
        assertTrue(unitChoice.getItems().contains("g")
                && unitChoice.getItems().contains("oz")
            && unitChoice.getItems().contains("pinch")
                && unitChoice.getItems().contains("-"));

        foodName.setText("Egg");
        assertTrue(okButton.isDisable(), "The OK Button should be disabled when only the name is picked");

        foodName.setText("");
        quantity.setText("200");
        assertTrue(okButton.isDisable(), "The OK Button should be disabled when only a quantity is picked");

        foodName.setText("Egg");
        clickOn(unitChoice);
        clickOn("#-");
        assertEquals("-", unitChoice.getValue());
        clickOn("g");

        assertFalse(okButton.isDisable(), "The OK Button should be enabled when all fields are entered");

        clickOn ("OK");
        ObservableList <Ingredient> ingredientsObservableList = lookup("#ingredientsObservableList").query();
        assertEquals(1, ingredientsObservableList.size());
        assertTrue(ingredientsObservableList.getFirst().toString().contains("Egg - 200.0 g"));
    }

    @Test
    public void testAddNewIngredientWrongNumber () {
        // We click on the button to add a new ingredient
        clickOn("#addIngredientButton");

        TextField foodName = lookup("#foodItemField").query();
        TextField quantity = lookup("#quantityField").query();
        ChoiceBox <String> unitChoice = lookup("#unitChoice").query();

        // We set values for every field, since that is needed to enable the OK Button
        foodName.setText("Egg");
        // We set a wrong value for the quantity
        quantity.setText("Egg");
        unitChoice.setValue("g");

        // We try to save - and verify that we will have gotten an error
        clickOn("OK");
        verifyThat("Invalid input of a decimal number.", NodeMatchers.isVisible());

        clickOn("OK");
    }

    @Test
    public void testEditIngredient () {
        // We set up the Shopping List
        ShoppingList s = ShoppingList.getShoppingList();
        Ingredient i = new Ingredient(new FoodItem("Sirene"), 20, "g");
        s.setIngredients(List.of(i));
        s.storeFile();
        shoppingListMainCtrl.refreshInterface();

        // We find and click on the Edit button
        Node row = lookup(".list-cell").nth(0).query();
        clickOn(from(row).lookup(".edit-button").queryButton());

        Button okButton = lookup("OK").query();

        TextField foodName = lookup("#foodItemField").query();
        TextField quantity = lookup("#quantityField").query();
        ChoiceBox <String> unitChoice = lookup("#unitChoice").query();

        // We test that the previous ingredient information is loaded
        assertEquals("Sirene", foodName.getText());
        assertEquals("20.0", quantity.getText());
        assertEquals("g", unitChoice.getValue());

        // We also assert the OK button is initially disabled
        assertTrue(okButton.isDisable());

        // We change the quantity and save
        quantity.setText("30.0");
        clickOn("OK");

        ObservableList <Ingredient> ingredientObservableList = lookup("#ingredientsObservableList").query();

        // We assert that a new element has not been added, but edited
        assertEquals(1, ingredientObservableList.size());

        // We verify the change
        assertEquals(30, ingredientObservableList.getFirst().getQuantity());
    }

    @Test
    public void testDeleteIngredient () {
        // We set up the Shopping List
        ShoppingList s = ShoppingList.getShoppingList();
        Ingredient i = new Ingredient(new FoodItem("Sirene"), 20, "g");
        s.setIngredients(List.of(i));
        s.storeFile();
        shoppingListMainCtrl.refreshInterface();

        // We find and click on the Edit button
        Node row = lookup(".list-cell").nth(0).query();
        clickOn(from(row).lookup(".delete-button").queryButton());

        ObservableList <Ingredient> ingredientObservableList = lookup("#ingredientsObservableList").query();

        // We assert that the element has been deleted and the size has been changed
        assertEquals(0, ingredientObservableList.size());
    }

    @Test
    public void testAddFoodItemThroughShoppingList () {
        ShoppingList s = ShoppingList.getShoppingList();
        s.setIngredients(new ArrayList<>());

        clickOn("#addIngredientButton");

        TextField foodName = lookup("#foodItemField").query();
        TextField quantity = lookup("#quantityField").query();
        ChoiceBox <String> unitChoice = lookup("#unitChoice").query();

        // Set the new FoodItem to be added
        foodName.setText("Kebab guy's special garlic sauce");
        quantity.setText("200");
        clickOn(unitChoice);
        clickOn("g");

        // Click on the OK Button
        clickOn ("OK");

        // We verify that the popup has appeared
        Stage topStage = (Stage) targetWindow();
        assertEquals("Food Item not found", topStage.getTitle());
        Node dialogPane = lookup(".dialog-pane").query();
        verifyThat(from(dialogPane).lookup(".content.label"), LabeledMatchers.hasText("The ingredient you are adding is not known to us.\n\nDo you wish to add it?"));

        // Click on the OK Button - we are expecting to be led to the Create Food Item UI
        clickOn ("OK");

        Node kCalValue = lookup("#kcalValue").query();
        assertNotNull(kCalValue,
                "The text field to add the calorific value of a Food Item should be present - a sign we are in the correct UI");

        TextField addFoodItemName = lookup("#foodItemField").query();
        TextField kcalValue = lookup("#kcalValue").query();
        TextField carbsValue = lookup("#carbsValue").query();
        TextField proteinsValue = lookup("#proteinsValue").query();
        TextField fatsValue = lookup("#fatsValue").query();

        assertEquals("Kebab guy's special garlic sauce", addFoodItemName.getText(),
                "The input field should already contain the entered Food Item name from the Shopping List");

        // Set the nutritional values for the garlic sauce food item (factually accurate - kebab guys in Bulgaria make them with a lot of fat)
        kcalValue.setText("120");
        carbsValue.setText("6.7");
        proteinsValue.setText("0.2");
        fatsValue.setText("14");

        // We confirm the action
        clickOn("#confirmButton");

        // We are back to the dialog box
        assertNotNull(lookup("#foodItemField").query());
        clickOn("OK");

        ObservableList <Ingredient> ingredientObservableList = lookup("#ingredientsObservableList").query();
        assertNotNull (ingredientObservableList, "We should be back in the Shopping List menu");
        assertEquals("Kebab guy's special garlic sauce", ingredientObservableList.getFirst().getFoodName(),
                "Change has been applied to Shopping List as well");
    }

}
