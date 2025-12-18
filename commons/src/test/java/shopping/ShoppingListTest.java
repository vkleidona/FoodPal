package shopping;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import recipes.FoodItem;
import recipes.Ingredient;
import recipes.Recipe;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ShoppingListTest {

    List <Ingredient> testList;
    Ingredient i;
    Ingredient i2;
    ShoppingList s = new ShoppingList();
    Recipe r;

    @BeforeEach
    void setup () {
        i = new Ingredient(20, "g");
        i2 = new Ingredient(30, "g");
        r = new Recipe("bryndzové halušky");
        i.setRecipe(r);
        i.setFoodType(new FoodItem("slanina"));
        i2.setRecipe(r);
        s.addIngredient(i);
        testList = List.of(i);
    }

    @Test
    void getIngredients() {
        assertEquals(testList, s.getIngredients());
    }

    @Test
    void testEqualsHash() {
        ShoppingList s2 = new ShoppingList();
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
}