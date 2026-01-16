package client.scenes;

import client.utils.ServerUtils;
import javafx.collections.ObservableList;
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

    /**
     * If the display is not available, we skip the tests
     */
    @BeforeAll
    public static void skipWithoutDisplay () {
        String display = System.getenv("DISPLAY");
        Assumptions.assumeTrue(display != null && !display.isEmpty(),
                "Skipping JavaFX UI tests because DISPLAY is not available (CI)");
    }

    /**
     * A start method which initializes the tested controller class
     * @param stage - the current stage of the JavaFX application
     * @throws IOException - an IOException is thrown if the file is not found at the specified location
     */
    @Override
    public void start (Stage stage) throws IOException {

        ServerUtils serverUtils;
        MainCtrl mainCtrl;
        ShoppingList s;

        // We try to find the controller class at the specified path
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/scenes/ShoppingListMainUI.fxml"));

        // We use DI to load the ServerUtils and MainCtrl into the controller class and link it to the UI
        serverUtils = mock(ServerUtils.class);
        mainCtrl = mock(MainCtrl.class);
        s = new ShoppingList();
        shoppingListMainCtrl = new ShoppingListMainCtrl(serverUtils, mainCtrl, s);
        loader.setController(shoppingListMainCtrl);

        // We load the scene
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Tests the option to return to the Main UI
     */
    @Test
    public void testBack () {
        // We look for the correct button
        clickOn("#returnToMainButton");
        Node editNameButton = lookup("#editNameButton").query();
        assertNotNull(editNameButton,
                "The button to change the recipeName should be present - an indicator that we are in the Main UI");
    }

    /**
     * Tests the resetting of the Shopping List
     */
    @Test
    public void testReset () {
        clickOn("#resetButton");
        ObservableList <Ingredient> ingredientsObservableList = lookup("#ingredientsObservableList").query();
        assertTrue(ingredientsObservableList.isEmpty(), "The ShoppingList should have 0 elements after clicking reset");
    }

    /**
     * Tests the printing functionality
     * @throws IOException - an IOException is thrown if the text file is not found
     */
    @Test
    public void testPrint () throws IOException {
        // We set up the Shopping List
        ShoppingList s = new ShoppingList();
        s.setIngredients(new ArrayList<>());
        s.addIngredient(new Ingredient(new FoodItem("salmon"), 20, "g"));

        clickOn("#printButton");
        File file = new File("shopping_list.txt");
        assertTrue(file.exists(), "The readable file with the Shopping List should exist");

        Path filePath = Path.of("shopping_list.txt");
        String content = Files.readString(filePath);
        assertTrue(content.contains("salmon - 20.0 g"), "The file should contain the ingredient in the ShoppingList");
    }

    /**
     * Tests the addition of a new ingredient
     */
    @Test
    public void testAddNewIngredient () {
        // We set up the Shopping List
        ShoppingList s = new ShoppingList();
        s.setIngredients(new ArrayList<>());

        // We find the button to add ingredients and click it
        clickOn("#addIngredientButton");

        Button okButton = lookup("OK").query();
        assertTrue(okButton.isDisable(), "The OK Button has to be initially disabled");

        // We look up the relevant text fields to use them later
        TextField foodName = lookup("#foodItemField").query();
        TextField quantity = lookup("#quantityField").query();
        ChoiceBox <String> unitChoice = lookup("#unitChoice").query();

        // In my implementation, all types of measurements are "merged"
        assertTrue(unitChoice.getItems().contains("g")
                && unitChoice.getItems().contains("oz")
            && unitChoice.getItems().contains("pinch")
                && unitChoice.getItems().contains("-"),
                "The choice box should contain a variety of measurement types");

        foodName.setText("Egg");
        assertTrue(okButton.isDisable(), "The OK Button should be disabled when only the name is picked");

        // We reset the food type to test the OK Button
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

        // We test that the change has propagated into the ShoppingList
        assertEquals(1, ingredientsObservableList.size(), "The Shopping List should now contain 1 ingredient");
        assertTrue(ingredientsObservableList.getFirst().toString().contains("Egg - 200.0 g"),
                "The ShoppingList should have correct information entered");
    }

    /**
     * Tests the alert system when an ingredient with a wrong quantity is entered
     */
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

    /**
     * Tests the editing functionality
     */
    @Test
    public void testEditIngredient () {
        // We set up the Shopping List and add 1 ingredient to it
        ShoppingList s = new ShoppingList();
        Ingredient i = new Ingredient(new FoodItem("Sirene"), 20, "g");
        s.setIngredients(List.of(i));

        // We save the changes and reload them
        s.storeFile();
        shoppingListMainCtrl.refreshInterface();

        // We find and click on the Edit button
        Node row = lookup(".list-cell").nth(0).query();
        clickOn(from(row).lookup(".edit-button").queryButton());

        TextField foodName = lookup("#foodItemField").query();
        TextField quantity = lookup("#quantityField").query();
        ChoiceBox <String> unitChoice = lookup("#unitChoice").query();

        // We test that the previous ingredient information is loaded
        assertEquals("Sirene", foodName.getText());
        assertEquals("20.0", quantity.getText());
        assertEquals("g", unitChoice.getValue());

        // We change the quantity and save
        quantity.setText("30.0");
        clickOn("OK");

        ObservableList <Ingredient> ingredientObservableList = lookup("#ingredientsObservableList").query();

        assertEquals(1, ingredientObservableList.size(),
                "Only an edit should have occurred, not an addition");

        assertEquals(30, ingredientObservableList.getFirst().getQuantity(),
                "The quantity needs to have changed");
    }

    /**
     * Tests the delete functionality
     */
    @Test
    public void testDeleteIngredient () {
        // We set up the Shopping List
        ShoppingList s = new ShoppingList();
        Ingredient i = new Ingredient(new FoodItem("Sirene"), 20, "g");
        s.setIngredients(List.of(i));
        s.storeFile();
        shoppingListMainCtrl.refreshInterface();

        // We find and click on the Delete button
        Node row = lookup(".list-cell").nth(0).query();
        clickOn(from(row).lookup(".delete-button").queryButton());

        ObservableList <Ingredient> ingredientObservableList = lookup("#ingredientsObservableList").query();

        assertEquals(0, ingredientObservableList.size(),
                "The ingredient should be deleted from the Shopping List which now should have 0 ingredients");
    }

    /**
     * Tests that a FoodItem can be added through the ShoppingList as well
     */
    @Test
    public void testAddFoodItemThroughShoppingList () {
        // Sets up the ShoppingList
        ShoppingList s = new ShoppingList();
        s.setIngredients(new ArrayList<>());

        clickOn("#addIngredientButton");

        TextField foodName = lookup("#foodItemField").query();
        TextField quantity = lookup("#quantityField").query();
        ChoiceBox <String> unitChoice = lookup("#unitChoice").query();

        // We set the new FoodItem to be added
        foodName.setText("Kebab guy's special garlic sauce");
        quantity.setText("200");
        clickOn(unitChoice);
        clickOn("g");

        clickOn ("OK");

        // We verify that the popup has appeared
        Stage topStage = (Stage) targetWindow();
        assertEquals("Food Item not found", topStage.getTitle(), "The title of the current window must match the title of the popup dialog box");
        Node dialogPane = lookup(".dialog-pane").query();
        // We also test that the content of the popup matches as well
        verifyThat(from(dialogPane).lookup(".content.label"), LabeledMatchers.hasText("The ingredient you are adding is not known to us.\n\nDo you wish to add it?"));

        // Click on the OK Button - we are expecting to be led to the Create Food Item UI
        clickOn ("OK");

        Node kCalValue = lookup("#kcalValue").query();
        assertNotNull(kCalValue,
                "The text field to add the calorific value of a Food Item should be present - a sign we are in the correct UI");

        // We set up all fields in the AddFoodItemUI for later use
        TextField addFoodItemName = lookup("#foodItemField").query();
        TextField kcalValue = lookup("#kcalValue").query();
        TextField carbsValue = lookup("#carbsValue").query();
        TextField proteinsValue = lookup("#proteinsValue").query();
        TextField fatsValue = lookup("#fatsValue").query();

        assertEquals("Kebab guy's special garlic sauce", addFoodItemName.getText(),
                "The input field should already contain the entered Food Item name from the Shopping List");

        // We set the nutritional values for the garlic sauce food item (factually accurate - kebab guys in Bulgaria make them with a lot of fat)
        kcalValue.setText("120");
        carbsValue.setText("6.7");
        proteinsValue.setText("0.2");
        fatsValue.setText("14");

        // We confirm the action
        clickOn("#confirmButton");

        assertNotNull(lookup("#quantityField").query(),
                "We should be back in the original dialog box for adding / editing an ingredient");
        clickOn("OK");

        ObservableList <Ingredient> ingredientObservableList = lookup("#ingredientsObservableList").query();
        assertNotNull (ingredientObservableList, "We should be back in the Shopping List menu");
        assertEquals("Kebab guy's special garlic sauce", ingredientObservableList.getFirst().getFoodName(),
                "Change has been applied to Shopping List as well");
    }

}
