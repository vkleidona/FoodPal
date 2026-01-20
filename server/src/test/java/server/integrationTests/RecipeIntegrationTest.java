package server.integrationTests;
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
import server.repositories.FoodItemRepository;
import server.repositories.RecipeRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
// we 'start' the application, so that we load all
// the beans, setting up the environment for testing
@AutoConfigureMockMvc
// we use this  to call /recipes just like a real
// client would, but inside the tests.
public class RecipeIntegrationTest {
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
        Recipe recipe = new Recipe("TestRecipe", Locale.of("bg"));
        Ingredient ingredient = new Ingredient(100, "g");
        FoodItem foodItem = foodItemRepository.findByName("Apple").
                orElseGet(() -> foodItemRepository.save(new FoodItem("Apple", 50, 14, 0, 0)));
        ingredient.setFoodType(foodItem);
        ingredient.setRecipe(recipe);
        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(ingredient);
        recipe.setIngredients(ingredients);
        recipe.setId(null);

        // convert to JSON format
        String json = objectMapper.writeValueAsString(recipe);

        // sending the POST request
        mockMvc.perform(post("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());

        // verify in database
        assertEquals(1, recipeRepository.count());
        assertEquals(1, foodItemRepository.count());
        assertEquals("TestRecipe", recipeRepository.findAll().getFirst().getName());
    }

    @Test
    public void getAllRecipesTest() throws Exception{
        recipeRepository.save(new Recipe("Pasta", Locale.of("bg")));
        recipeRepository.save(new Recipe("Pizza", Locale.of("bg")));

        String jsonResponse = mockMvc.perform(get("/api/recipes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Recipe[] recipes = objectMapper.readValue(jsonResponse, Recipe[].class);

        assertEquals(2, recipes.length);
        assertEquals("Pasta", recipes[0].getName());
        assertEquals("Pizza", recipes[1].getName());
    }

    @Test
    public void issueSetAllIngredientsTest() throws Exception{
        Recipe r = new Recipe("Omelette", Locale.of("bg"));
        r = recipeRepository.save(r);

        Ingredient i =  new Ingredient();
        i.setFoodType(new FoodItem("egg"));
        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(i);

        String jsonInput = objectMapper.writeValueAsString(ingredients);

        String responseJson = mockMvc.perform(put("/api/recipes/ingredients/set/" + r.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInput))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Recipe result = objectMapper.readValue(responseJson, Recipe.class);
        assertEquals(1, result.getIngredients().size());
    }

    @Test
    public void issueDeleteRecipeTest() throws Exception{
        Recipe recipe = new Recipe("To be deleted", Locale.of("bg"));
        recipe = recipeRepository.save(recipe);

        mockMvc.perform(delete("/api/recipes/" + recipe.getId()))
                .andExpect(status().isNoContent());

        assertEquals(0, recipeRepository.count());
        assertFalse(recipeRepository.existsById(recipe.getId()));
    }


    @Test
    public void issueUpdateRecipeNameTest() throws Exception{
        Recipe original = new Recipe("Old name", Locale.of("bg"));
        original = recipeRepository.save(original);

        Recipe updated = new Recipe("New name");
        String inputJson = objectMapper.writeValueAsString(updated);

        mockMvc.perform(put("/api/recipes/" + original.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(inputJson))
                .andExpect(status().isOk());

        Recipe dbRecipe = recipeRepository.findById(original.getId()).isPresent() ?
                recipeRepository.findById(original.getId()).get() : new Recipe("");
        assertEquals("New name", dbRecipe.getName());
    }

}

