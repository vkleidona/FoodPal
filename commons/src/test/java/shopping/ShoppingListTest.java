package shopping;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import recipes.FoodItem;
import recipes.Ingredient;
import recipes.Recipe;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class ShoppingListTest {

    List <Ingredient> testList;
    Ingredient i;
    Ingredient i2;
    Recipe r;
    FoodItem f;
    ShoppingList s;

    @TempDir
    Path tempDir;

    File file;

    @BeforeEach
    void setup () {
        file = tempDir.resolve("test_shopping_list.json").toFile();
        f = new FoodItem("slanina", 10, 20, 30, 40);
        i = new Ingredient(20, "g");
        i2 = new Ingredient(30, "g");
        r = new Recipe("bryndzové halušky", new Locale("en"));
        i.setRecipe(r);
        i.setFoodType(f);
        i2.setRecipe(r);
        i2.setFoodType(f);
        s = new ShoppingList(file);
        s.addIngredient(i);
        testList = List.of(i);
    }

    @AfterEach
    void cleanup() {
        File testFile = this.file;
        if (testFile.exists()) testFile.delete();
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
        ShoppingList s2 = new ShoppingList(file);
        // I also test the removeIngredient method, that is why this looks redundant
        s2.addIngredient(i2);
        s.removeIngredient(i);
        s.addIngredient(i2);
        assertEquals(s, s2);
        assertEquals(s.hashCode(), s2.hashCode());
    }

    @Test
    void testNotEqualsHash() {
        ShoppingList s2 = new ShoppingList(file);
        s2.addIngredient(i2);
        assertNotEquals(s, s2);
        assertNotEquals(s.hashCode(), s2.hashCode());
    }

    @Test
    void testToString() {
        String toStr = s.toString();
        assertTrue(toStr.contains("ShoppingList") && toStr.contains("ingredients"));
        assertTrue(toStr.contains("slanina - 20.0 g"));

    }

    @Test
    void testGetShoppingListNonNull() {
        ShoppingList s2 = new ShoppingList(file);
        s2.addIngredient(i);
        assertEquals(s2, s);
    }

    @Test
    void testStoreAndLoad () {
        ArrayList <Ingredient> arrayList = new ArrayList<>();
        arrayList.add(i);
        s.setIngredients(arrayList);

        s.storeFile();

        File file = new File(this.file.getAbsolutePath());
        assertTrue(file.exists());

        s.loadFile();

        assertEquals(1, s.getIngredients().size());
        assertTrue(s.getIngredients().stream().anyMatch(ingredient -> ingredient.getFoodName().equals("slanina")));
        file.deleteOnExit();
    }
}