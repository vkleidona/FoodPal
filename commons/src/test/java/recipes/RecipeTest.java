package recipes;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecipeTest {

    Recipe r;
    List <Ingredient> i;

    @BeforeEach
    public void setup() {
        r = new Recipe("bryndzové halušky");
        i = new ArrayList<>();
        r.setIngredients(i);
    }

    @Test
    void getSetId() {
        r.setId(4L);
        assertNotEquals(null, r.getId());
        assertEquals(4L, r.getId());
    }

    @Test
    void getName() {
        assertEquals("bryndzové halušky", r.getName());
    }

    @Test
    void getIngredients() {
        assertEquals(i, r.getIngredients());
    }

    @Test
    void getInstructions() {
        List <String> instr = new ArrayList<>();
        assertEquals(instr, r.getInstructions());
    }

    @Test
    void setInstructionsTest() {
        List <String> instr = new ArrayList<>();
        instr.add("Bake");
        r.setInstructions(instr);
        List <String> empty = new ArrayList<>();
        assertNotEquals(empty, r.getInstructions());
    }

    @Test
    void testEquals() {
        Recipe r2 = new Recipe("bryndzové halušky");
        r2.setIngredients(i);
        assertEquals(r2, r);
        assertEquals(r2.hashCode(), r.hashCode());
    }

    @Test
    void testNotEquals() {
        Recipe r2 = new Recipe("bryndzové halusky");
        r2.setIngredients(i);
        assertNotEquals(r2, r);
        assertNotEquals(r2.hashCode(), r.hashCode());
    }

    @Test
    void testToString() {
        String rStr = r.toString();
        assertTrue(rStr.contains("name") && rStr.contains("instructions"));
        assertTrue(rStr.contains("Recipe"));
        assertTrue(rStr.contains("\n"));
    }
}