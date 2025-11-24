package recipes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NutritionTest {

    private Nutrition n;
    private FoodItem f;

    @BeforeEach
    void addData() {
        n = new Nutrition (100, 20, 30, 40);
        f = new FoodItem("apple");
        f.setNutritionalValues(n);
    }

    @Test
    void testGetKcal() {
        assertEquals(100, n.getKcal());
    }

    @Test
    void testGetCarbs() {
        assertEquals(20, n.getCarbs());
    }

    @Test
    void testGetProteins() {
        assertEquals(30, n.getProteins());
    }

    @Test
    void testGetFats() {
        assertEquals(40, n.getFats());
    }

    @Test
    void testEqualsHashCode() {
        Nutrition n2 = new Nutrition (100, 20, 30, 40);
        f.setNutritionalValues(n2);
        assertEquals(n, n2);
    }

    @Test
    void testNotEqualsHashCodeSameFoodItem() {
        Nutrition n2 = new Nutrition (200, 20, 40, 50);
        assertNotEquals(n, n2);
    }

    @Test
    void testNotEqualsHashCodeDiffFoodItem() {
        Nutrition n2 = new Nutrition (200, 20, 40, 50);
        assertNotEquals(n, n2);
    }

    @Test
    void testToString() {
        String nStr = n.toString();
        assertTrue(nStr.contains("carbs") && nStr.contains("fats")
                && nStr.contains("kcal") && nStr.contains("proteins"));
        assertTrue(nStr.contains("Nutrition"));
        assertTrue(nStr.contains("\n"));
    }
}