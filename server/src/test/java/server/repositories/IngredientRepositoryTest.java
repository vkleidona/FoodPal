package server.repositories;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import recipes.Ingredient;
import recipes.Recipe;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
public class IngredientRepositoryTest {
    @Autowired
    private IngredientRepository repo;
    @Autowired
    private RecipeRepository recipeRepo;

    Ingredient i;
    Recipe r;

    @BeforeEach
    void setup () {
        r = new Recipe("orange juice");
        recipeRepo.save(r);
        i = new Ingredient(20, "g");
        i.setRecipe(r);
        repo.save(i);
    }

    @Test
    void testSaveAndFind() {
        Optional<Ingredient> ingredient = repo.findById(i.getIngredientID());
        assertTrue(ingredient.isPresent());
        assertEquals("g", ingredient.get().getMeasurement());
        assertEquals(20, ingredient.get().getQuantity());
    }

    @Test
    void testAdded() {
        Optional<Ingredient> ingredient = repo.findById(i.getIngredientID()+1);
        assertFalse(ingredient.isPresent());
    }
}