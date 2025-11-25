package client.scenes;

import org.junit.jupiter.api.Test;
import recipes.Recipe;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
/*
TO DO: test using javaFX UI components
 */
public class CreateRecipeCtrlUnitTest {

    private final CreateRecipeCtrl ctrl = new CreateRecipeCtrl();

    @Test
    void buildsRecipeWithMultipleIngredientsAndInstructions() {
        String name = "Pie";
        List<String> ingredientNames = List.of("Apple", "Sugar");
        List<Integer> ingredientQuantities = List.of(200, 50);
        List<String> ingredientUnits = List.of("g", "g");
        List<String> instructions = List.of("Cut apples", "Add sugar", "Bake");

        Recipe recipe = ctrl.buildRecipe(name, ingredientNames, ingredientQuantities, ingredientUnits, instructions);

        assertEquals("Pie", recipe.getName());
        assertEquals(2, recipe.getIngredients().size());
        assertEquals("Apple", recipe.getIngredients().get(0).getFoodType().getName());
        assertEquals(50, recipe.getIngredients().get(1).getQuantity());
        assertEquals("Bake", recipe.getInstructions().get(2));
        assertSame(recipe, recipe.getIngredients().get(1).getRecipe());
    }

}
