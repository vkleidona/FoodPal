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
    void getSetName() {
        f.setName("sirene");
        assertNotEquals("cheese", f.getName());
    }

    @Test
    void getIngredientsWithFoodItem() {
        assertEquals(i, f.getIngredientsWithFoodItem());
    }

    @Test
    void testSetGetKcal() {
        f.setKcal(80);
        assertEquals(80, f.getKcal());
    }

    @Test
    void testSetGetCarbs() {
        f.setCarbs(40.2);
        assertEquals(40.2, f.getCarbs());
    }

    @Test
    void testSetGetProteins() {
        f.setProteins(20.7);
        assertEquals(20.7, f.getProteins());
    }

    @Test
    void testSetGetFats() {
        f.setFats(30.8);
        assertEquals(30.8, f.getFats());
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