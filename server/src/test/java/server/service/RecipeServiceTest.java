package server.service;

import exceptions.ResourceNotFoundException;
import exceptions.ServerErrorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import recipes.FoodItem;
import recipes.Ingredient;
import recipes.Recipe;
import server.repositories.FoodItemRepository;
import server.repositories.RecipeRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RecipeServiceTest {

    private RecipeRepository recipeRepository;

    private FoodItemRepository foodItemRepository;

    private RecipeService service;

    @BeforeEach
    void setup() {
        recipeRepository = mock(RecipeRepository.class);
        foodItemRepository = mock(FoodItemRepository.class);
        SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
        service = new RecipeService(recipeRepository, foodItemRepository, messagingTemplate);
    }

    @Test
    void testSaveRecipe() {
        // arrange: build Recipe with Ingredient and FoodItem
        Recipe recipe = new Recipe("TestRecipe", Locale.of("bg"));
        Ingredient ingredient = new Ingredient(100, "g");
        FoodItem foodItem = new FoodItem("Apple");
        ingredient.setFoodType(foodItem);
        ingredient.setRecipe(recipe);
        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(ingredient);
        recipe.setIngredients(ingredients);
        recipe.setId(1L);

        when(recipeRepository.save(recipe)).thenReturn(recipe);

        Long id = service.saveRecipe(recipe);
        assertEquals(1L, id);
    }

    @Test
    void testSaveRecipeUnsuccessfully() {
        // arrange: build Recipe with Ingredient and FoodItem
        Recipe recipe = new Recipe("TestRecipe", Locale.of("bg"));
        Ingredient ingredient = new Ingredient(100, "g");
        FoodItem foodItem = new FoodItem("Apple");
        ingredient.setFoodType(foodItem);
        ingredient.setRecipe(recipe);
        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(ingredient);
        recipe.setIngredients(ingredients);
        recipe.setId(1L);

        when(recipeRepository.save(recipe)).thenThrow(new ServerErrorException());

        assertThrows(ServerErrorException.class, () -> service.saveRecipe(recipe));
    }

    @Test
    public void getAllRecipesTest() {
        Recipe recipe = new Recipe("TestRecipe", Locale.of("bg"));
        Ingredient ingredient = new Ingredient(100, "g");
        FoodItem foodItem = foodItemRepository.findByName("Apple").
                orElseGet(() -> foodItemRepository.save(new FoodItem("Apple", 50, 14, 0, 0)));
        ingredient.setFoodType(foodItem);
        ingredient.setRecipe(recipe);
        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(ingredient);
        recipe.setIngredients(ingredients);
        recipe.setId(1L);

        when(recipeRepository.findAll()).thenReturn(List.of(recipe));
        assertEquals("TestRecipe", service.getAllRecipes().getFirst().getName());
    }

    @Test
    public void setAllIngredientsTest(){
        Recipe r = new Recipe("Яйца по панагюрски", Locale.of("bg"));

        Ingredient i =  new Ingredient();
        i.setFoodType(new FoodItem("яйце"));
        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(i);
        r.setId(1L);

        Recipe expected = new Recipe("Яйца по панагюрски", Locale.of("bg"));
        expected.setIngredients(ingredients);
        Ingredient i2 = new Ingredient(new FoodItem("egg"), 20.0, "g");
        expected.addIngredient(i2);
        expected.setId(2L);

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(r));
        when(recipeRepository.save(any(Recipe.class))).thenAnswer(j -> j.getArgument(0));

        Recipe incoming = service.setAllIngredients(expected.getIngredients(), 1L);

        assertNotEquals(incoming, expected);
        assertEquals(List.of(i, i2),
                incoming.getIngredients());
    }

    @Test
    public void issueSetAllIngredientsTestFail(){
        Recipe r = new Recipe("Яйца по панагюрски", Locale.of("bg"));

        Ingredient i =  new Ingredient();
        i.setFoodType(new FoodItem("яйце"));
        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(i);
        r.setId(1L);

        when(recipeRepository.findById(1L)).thenThrow(new ServerErrorException());

        assertThrows(ServerErrorException.class, () -> service.setAllIngredients(ingredients, 1L));
    }

    @Test
    public void issueDeleteRecipeTest() {
        Recipe recipe = new Recipe("Deleted", Locale.of("bg"));
        recipe.setId(1L);

        when(recipeRepository.existsById(1L)).thenReturn(true);

        boolean deleted = service.deleteRecipe(1L);
        assertTrue(deleted);
    }

    @Test
    public void issueDeleteRecipeUnsuccessfullyTest() {
        Recipe recipe = new Recipe("Deleted", Locale.of("bg"));
        recipe.setId(1L);

        when(recipeRepository.existsById(1L)).thenReturn(false);

        boolean deleted = service.deleteRecipe(1L);
        assertFalse(deleted);
    }

    @Test
    public void issueUpdateRecipeTest() {
        Recipe r = new Recipe("Яйца по панагюрски", Locale.of("bg"));

        Ingredient i =  new Ingredient();
        i.setFoodType(new FoodItem("яйце"));
        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(i);
        r.setId(1L);
        r.setIngredients(ingredients);

        Recipe r2 = new Recipe("Яйце по панагюрски", Locale.of("bg"));
        r2.setIngredients(ingredients);
        r2.setInstructions(List.of("Boil"));
        r2.setId(1L);

        when(recipeRepository.findById(1L)).thenReturn(Optional.of(r));
        when(recipeRepository.save(any(Recipe.class))).thenAnswer(j -> j.getArgument(0));

        assertEquals(r2, service.updateRecipe(1L, r2));
    }

    @Test
    public void issueUpdateRecipeUnsuccessfulTest() {
        Recipe r = new Recipe("Яйца по панагюрски", Locale.of("bg"));

        Ingredient i =  new Ingredient();
        i.setFoodType(new FoodItem("яйце"));
        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(i);
        r.setId(1L);
        r.setIngredients(ingredients);

        Recipe r2 = new Recipe("Яйце по панагюрски", Locale.of("bg"));
        r2.setIngredients(ingredients);
        r2.setInstructions(List.of("Boil"));

        when(recipeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.updateRecipe(1L, r2));
    }
}
