package recipes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IngredientTest {

    Recipe r;
    FoodItem f;
    Ingredient ingredient;

    @BeforeEach
    public void setup () {
        List<String> instr = new ArrayList<>();
        r = new Recipe ("bryndzové halušky", instr);
        f = new FoodItem("slanina");
        ingredient = new Ingredient(20, "g");
        ingredient.setRecipe(r);
        ingredient.setFoodType(f);
    }


    @Test
    void getRecipeIDTest() {
        assertEquals(r.getId(), ingredient.getRecipeID());
    }

    @Test
    void getFoodTypeTest() {
        assertEquals(f, ingredient.getFoodType());
    }

    @Test
    void getQuantity() {
        assertEquals(20, ingredient.getQuantity());
    }

    @Test
    void getMeasurement() {
        assertEquals("g", ingredient.getMeasurement());
    }

    @Test
    void getRecipe() {
        assertEquals(r, ingredient.getRecipe());
    }

    @Test
    void testEqualsHash() {
        Ingredient ingredient2 = new Ingredient(20, "g");
        ingredient2.setRecipe(r);
        ingredient2.setFoodType(f);
        assertEquals(ingredient, ingredient2);
        assertEquals(ingredient.hashCode(), ingredient2.hashCode());
    }

    @Test
    void testNotEqualsHash() {
        Ingredient ingredient2 = new Ingredient(30, "g");
        ingredient2.setRecipe(r);
        ingredient2.setFoodType(f);
        assertNotEquals(ingredient, ingredient2);
        assertNotEquals(ingredient.hashCode(), ingredient2.hashCode());
    }

    @Test
    void testRecipeUpdate() {
        List<String> instr = new ArrayList<>();
        Ingredient ingredient2 = new Ingredient(30, "g");
        Recipe r2 = new Recipe("Knedliky", instr);
        r.setId(2L);
        r2.setId(42L);
        ingredient2.setRecipe(r2);
        ingredient2.setFoodType(f);
        assertEquals(ingredient2.getRecipeID(), r2.getId());
    }

    @Test
    void testToString() {
        String iStr = ingredient.toString();
        assertTrue(iStr.contains("quantity") && iStr.contains("measurement"));
        assertTrue(iStr.contains("Ingredient"));
        assertTrue(iStr.contains("\n"));
    }
}