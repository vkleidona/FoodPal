package server.api.controllerTests;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import recipes.FoodItem;
import recipes.Ingredient;
import recipes.Recipe;
import server.api.repositories.FoodItemRepository;
import server.api.repositories.RecipeRepository;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
// we 'start' the application, so that we load all
// the beans, setting up the environment for testing
@AutoConfigureMockMvc
// we use this  to call /recipes just like a real
// client would, but inside the tests.
public class RecipeControllerTest {
    @Autowired
    private MockMvc mockMvc;
    // used to simulate requests
    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private FoodItemRepository foodItemRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        recipeRepository.deleteAll();
        foodItemRepository.deleteAll();
    }

    @Test
    void testSaveRecipe() throws Exception {
        // arrange: build Recipe with Ingredient and FoodItem
        Recipe recipe = new Recipe("TestRecipe");
        Ingredient ingredient = new Ingredient(100, "g");
        FoodItem foodItem = new FoodItem("Apple", 50, 14, 0, 0);
        ingredient.setFoodType(foodItem);
        ingredient.setRecipe(recipe);
        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(ingredient);
        recipe.setIngredients(ingredients);

        // convert to JSON format
        String json = objectMapper.writeValueAsString(recipe);

        // sending the POST request
        mockMvc.perform(post("/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        // verify in database
        assertEquals(1, recipeRepository.count());
        assertEquals(1, foodItemRepository.count());
        assertEquals("TestRecipe", recipeRepository.findAll().get(0).getName());
    }
}
