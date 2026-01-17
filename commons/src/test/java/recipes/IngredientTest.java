package recipes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class IngredientTest {

    Recipe r;
    FoodItem f;
    Ingredient ingredient;

    @BeforeEach
    public void setup () {
        r = new Recipe ("bryndzové halušky");
        f = new FoodItem("slanina", 100, 20, 30, 40);
        ingredient = new Ingredient(f, 20, "g");
        ingredient.setRecipe(r);
    }

    @Test
    void getSetIngredientIDTest() {
        ingredient.setIngredientID(3L);
        assertNotEquals(null, ingredient.getIngredientID());
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
        Ingredient ingredient2 = new Ingredient(f, 20, "g");
        ingredient2.setRecipe(r);
        assertEquals(ingredient, ingredient2);
        assertEquals(ingredient.hashCode(), ingredient2.hashCode());
    }

    @Test
    void testNotEqualsHash() {
        Ingredient ingredient2 = new Ingredient(f, 30, "g");
        ingredient2.setRecipe(r);
        assertNotEquals(ingredient, ingredient2);
        assertNotEquals(ingredient.hashCode(), ingredient2.hashCode());
    }

    @Test
    void testRecipeUpdate() {
        Ingredient ingredient2 = new Ingredient(f, 30, "g");
        Recipe r2 = new Recipe("Knedliky");
        r.setId(2L);
        r2.setId(42L);
        ingredient2.setRecipe(r2);
       assertEquals(ingredient2.getRecipe().getId(), r2.getId());
    }

    @Test
    void testToString() {
        String iStr = ingredient.toString();
        assertEquals("slanina - 20.0 g (for preparing bryndzové halušky)",iStr);
    }
}