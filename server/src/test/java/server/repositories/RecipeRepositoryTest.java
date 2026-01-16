package server.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import recipes.Recipe;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class RecipeRepositoryTest {

    @Autowired
    private RecipeRepository recipeRepository;

    @Test
    void testSaveAndFindById() {
        Recipe recipe = new Recipe("Pasta");
        recipe.setLocale(new Locale("en"));
        Recipe saved = recipeRepository.save(recipe);

        assertNotNull(saved.getId(), "Id should be generated after saving");

        Recipe found = recipeRepository.findById(saved.getId()).orElse(null);
        assertNotNull(found);
        assertEquals("Pasta", found.getName());
    }

    @Test
    void testFindAll() {
        Recipe r = new Recipe("Salad");
        Recipe r2 = new Recipe("Soup");
        r.setLocale(new Locale("en"));
        r2.setLocale(new Locale("en"));
        recipeRepository.save(r);
        recipeRepository.save(r2);

        var all = recipeRepository.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void testDelete() {
        Recipe recipe = new Recipe("Cake");
        recipe.setLocale(new Locale("bg"));
        Recipe saved = recipeRepository.save(recipe);
        long id = saved.getId();

        recipeRepository.deleteById(id);
        assertFalse(recipeRepository.findById(id).isPresent());
    }

}
