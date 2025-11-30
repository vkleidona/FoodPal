package recipes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FoodItemTest {


    private FoodItem f;
    private List<Ingredient> i;

    @BeforeEach
    public void setup () {
        f = new FoodItem("cheese", 100, 20, 30, 40);
        i = new ArrayList<>();
        f.setIngredientsWithFoodItem(i);
    }

    @Test
    void getName() {
        assertEquals("cheese", f.getName());
    }

    @Test
    void getIngredientsWithFoodItem() {
        assertEquals(i, f.getIngredientsWithFoodItem());
    }

    @Test
    void testGetKcal() {
        assertEquals(100, f.getKcal());
    }

    @Test
    void testGetCarbs() {
        assertEquals(20, f.getCarbs());
    }

    @Test
    void testGetProteins() {
        assertEquals(30, f.getProteins());
    }

    @Test
    void testGetFats() {
        assertEquals(40, f.getFats());
    }

    @Test
    void testEqualsHash() {
        FoodItem f2 = new FoodItem("cheese", 100, 20, 30, 40);
        f2.setIngredientsWithFoodItem(i);
        assertEquals(f, f2);
        assertEquals(f.hashCode(), f2.hashCode());
    }

    @Test
    void testEqualsHashIngredients() {
        FoodItem f2 = new FoodItem("cheese", 100, 20, 30, 40);
        Ingredient i1 = new Ingredient(20, "ml");
        i.add(i1);
        f2.setIngredientsWithFoodItem(i);
        i.remove(i1);

        FoodItem f3 = new FoodItem("cheese", 100, 20, 30, 40);
        Ingredient i2 = new Ingredient(20, "ml");
        i.add(i2);
        f3.setIngredientsWithFoodItem(i);

        assertEquals(f3, f2);
        assertEquals(f3.hashCode(), f2.hashCode());
    }


    @Test
    void testToString() {
        String fStr = f.toString();
        assertTrue(fStr.contains("name") && fStr.contains("carbs"));
        assertTrue(fStr.contains("proteins") && fStr.contains("fats"));
        assertTrue(fStr.contains("kcal"));
        assertTrue(fStr.contains("FoodItem"));
        assertTrue(fStr.contains("\n"));
    }
}