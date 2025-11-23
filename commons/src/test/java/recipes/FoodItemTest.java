package recipes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FoodItemTest {


    private FoodItem f;
    private Nutrition n;
    private List<Ingredient> i;

    @BeforeEach
    public void setup () {
        f = new FoodItem("cheese");
        n = new Nutrition(100, 20, 30, 40);
        f.setNutritionalValues(n);
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
    void getNutritionalValues() {
        assertEquals(n, f.getNutritionalValues());
    }

    @Test
    void testEqualsHash() {
        FoodItem f2 = new FoodItem("cheese");
        f2.setNutritionalValues(n);
        f2.setIngredientsWithFoodItem(i);
        assertEquals(f, f2);
        assertEquals(f.hashCode(), f2.hashCode());
    }

    @Test
    void testEqualsHashIngredients() {
        FoodItem f2 = new FoodItem("cheese");
        f2.setNutritionalValues(n);
        Ingredient i1 = new Ingredient(20, "ml");
        i.add(i1);
        f2.setIngredientsWithFoodItem(i);
        i.remove(i1);

        FoodItem f3 = new FoodItem("cheese");
        f3.setNutritionalValues(n);
        Ingredient i2 = new Ingredient(20, "ml");
        i.add(i2);
        f3.setIngredientsWithFoodItem(i);

        assertEquals(f3, f2);
        assertEquals(f3.hashCode(), f2.hashCode());
    }

    @Test
    void testUpdateNutritionalValues() {
        FoodItem f2 = new FoodItem("cheese");
        f2.setNutritionalValues(n);
        f2.setNutritionalValues(n);
        assertEquals(f2, n.getCorrespondingFoodItem());
    }

    @Test
    void testToString() {
        String fStr = f.toString();
        assertTrue(fStr.contains("name"));
        assertTrue(fStr.contains("FoodItem"));
        assertTrue(fStr.contains("\n"));
    }
}