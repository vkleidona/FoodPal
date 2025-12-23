package shopping;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import recipes.FoodItem;
import recipes.Ingredient;
import recipes.Recipe;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ShoppingListTest {

    List <Ingredient> testList;
    Ingredient i;
    Ingredient i2;
    ShoppingList s = ShoppingList.getShoppingList();
    Recipe r;
    FoodItem f;

    @BeforeEach
    void setup () {
        f = new FoodItem("slanina", 10, 20, 30, 40);
        i = new Ingredient(20, "g");
        i2 = new Ingredient(30, "g");
        r = new Recipe("bryndzové halušky");
        i.setRecipe(r);
        i.setFoodType(f);
        i2.setRecipe(r);
        i2.setFoodType(f);
        s.addIngredient(i);
        testList = List.of(i);
    }

    @Test
    void getIngredients() {
        s.setIngredients(testList);
        assertEquals(testList, s.getIngredients());
    }

    @Test
    void setIngredientsTest() {
        List <Ingredient> iList = new ArrayList<>();
        iList.add(i2);
        s.setIngredients(iList);
        assertEquals(List.of(i2), s.getIngredients());
        s.removeIngredient(i2);
    }

    @Test
    void setIngredientIndexTest() {
        ArrayList <Ingredient> arrayList = new ArrayList<>();
        arrayList.add(i);
        s.setIngredients(arrayList);
        s.setIngredient(0, i2);
        assertEquals(List.of(i2), s.getIngredients());
    }

    @Test
    void testEqualsHash() {
        ShoppingList s2 = ShoppingList.getShoppingList();
        // I also test the removeIngredient method, that is why this looks redundant
        s2.addIngredient(i2);
        s.removeIngredient(i);
        s.addIngredient(i2);
        assertEquals(s, s2);
        assertEquals(s.hashCode(), s2.hashCode());
    }

    @Test
    void testNotEqualsHash() {
        ShoppingList s2 = new ShoppingList();
        s2.addIngredient(i2);
        assertNotEquals(s, s2);
        assertNotEquals(s.hashCode(), s2.hashCode());
    }

    @Test
    void testToString() {
        String toStr = s.toString();
        assertTrue(toStr.contains("ShoppingList") && toStr.contains("ingredients"));
        assertTrue(toStr.contains("slanina - 20.0 g (for preparing bryndzové halušky)"));
        System.out.println(toStr);
    }

    @Test
    void testGetShoppingListNonNull() {
        ShoppingList s2 = ShoppingList.getShoppingList();
        s2.addIngredient(i);
        assertEquals(s2, ShoppingList.getShoppingList());
    }

    @Test
    void testStoreAndLoad () {
        ArrayList <Ingredient> arrayList = new ArrayList<>();
        arrayList.add(i);
        s.setIngredients(arrayList);

        s.storeFile();

        File file = new File("shopping_list_ingredients.bin");
        assertTrue(file.exists());

        s.setIngredients(null);
        s.loadFile();

        assertEquals(1, s.getIngredients().size());
        assertTrue(s.getIngredients().stream().anyMatch(ingredient -> ingredient.getFoodName().equals("slanina")));
        file.deleteOnExit();
    }
}