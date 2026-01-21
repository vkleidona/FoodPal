package server.api.controllerTests;
import exceptions.ResourceNotFoundException;
import exceptions.ServerErrorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import recipes.FoodItem;
import recipes.Ingredient;
import recipes.Recipe;
import server.api.controllers.RecipeController;
import server.service.RecipeService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

// This is a Unit Test for the new Controller class.
// Another Integration test was created earlier, which will be kept,
// as it provides useful testing functionality.
public class RecipeControllerTest {

    private RecipeController ctrl;
    private RecipeService service;

    @BeforeEach
    void setup() {
        service = mock(RecipeService.class);
        ctrl = new RecipeController(service);
    }

    @Test
    void testSaveRecipe(){
        // arrange: build Recipe with Ingredient and FoodItem
        Recipe recipe = new Recipe("TestRecipe", Locale.of("bg"));
        Ingredient ingredient = new Ingredient(100, "g");
        FoodItem foodItem = new FoodItem("Banitsa");
        ingredient.setFoodType(foodItem);
        ingredient.setRecipe(recipe);
        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(ingredient);
        recipe.setIngredients(ingredients);
        recipe.setId(1L);

        when(service.saveRecipe(recipe)).thenReturn(1L);

        ResponseEntity <Long> response = ctrl.issueSaveRecipe(recipe);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(1L, response.getBody());
    }

    @Test
    void testSaveRecipeUnsuccessfully(){
        // arrange: build Recipe with Ingredient and FoodItem
        Recipe recipe = new Recipe("TestRecipe", Locale.of("bg"));
        Ingredient ingredient = new Ingredient(100, "g");
        FoodItem foodItem = new FoodItem("Баница");
        ingredient.setFoodType(foodItem);
        ingredient.setRecipe(recipe);
        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(ingredient);
        recipe.setIngredients(ingredients);
        recipe.setId(1L);

        when(service.saveRecipe(recipe)).thenThrow(new ServerErrorException("Could not save recipe"));

        assertThrows(ServerErrorException.class, () -> ctrl.issueSaveRecipe(recipe));
    }

    @Test
    public void getAllRecipesTest() {
        Recipe recipe1 = new Recipe("Баница", Locale.of("bg"));
        Recipe recipe2 = new Recipe("Шопска салата", Locale.of("bg"));

        List<Recipe> mockList = List.of(recipe1, recipe2);
        when(service.getAllRecipes()).thenReturn(mockList);

        ResponseEntity <List<Recipe>> result = ctrl.issueGetAllRecipes();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(2, result.getBody().size());
        assertEquals("Баница", result.getBody().getFirst().getName());
    }

    @Test
    public void issueSetAllIngredientsTest(){
        Recipe r = new Recipe("Яйца по панагюрски", Locale.of("bg"));

        Ingredient i =  new Ingredient();
        i.setFoodType(new FoodItem("яйце"));
        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(i);
        r.setId(1L);

        Recipe expected = new Recipe("Яйца по панагюрски", Locale.of("bg"));
        expected.setIngredients(ingredients);
        expected.setId(1L);

        when(service.setAllIngredients(ingredients, 1L)).thenReturn(expected);

        ResponseEntity<Recipe> response = ctrl.issueSetAllIngredients(ingredients, 1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(ingredients, response.getBody().getIngredients());
    }

    @Test
    public void issueSetAllIngredientsTestFail(){
        Recipe r = new Recipe("Яйца по панагюрски", Locale.of("bg"));

        Ingredient i =  new Ingredient();
        i.setFoodType(new FoodItem("яйце"));
        List<Ingredient> ingredients = new ArrayList<>();
        ingredients.add(i);
        r.setId(1L);

        when(service.setAllIngredients(ingredients, 1L)).thenThrow(new ServerErrorException());

        assertThrows(ServerErrorException.class, () -> ctrl.issueSetAllIngredients(ingredients, 1L));
    }

    @Test
    public void issueDeleteRecipeTest() {
        Recipe recipe = new Recipe("Deleted", Locale.of("bg"));
        recipe.setId(1L);

        when(service.deleteRecipe(1L)).thenReturn(true);

        ResponseEntity<Void> response = ctrl.issueDeleteRecipe(1L);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void issueDeleteRecipeUnsuccessfullyTest() {
        Recipe recipe = new Recipe("Deleted", Locale.of("bg"));
        recipe.setId(1L);

        when(service.deleteRecipe(1L)).thenReturn(false);

        ResponseEntity<Void> response = ctrl.issueDeleteRecipe(1L);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
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

        when(service.updateRecipe(1L ,r2)).thenReturn(r2);

        ResponseEntity<Recipe> response = ctrl.issueUpdateRecipe(1L, r2);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(r2, response.getBody());
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

        when(service.updateRecipe(1L ,r2)).thenThrow(new ResourceNotFoundException());

        assertThrows(ResourceNotFoundException.class, () -> ctrl.issueUpdateRecipe(1L, r2));
    }

    @Test
    public void searchByTermTest() {
        Recipe r1 = new Recipe("Яйца по панагюрски", Locale.of("bg"));
        Recipe r2 = new Recipe("Кюфтета по чирпански", Locale.of("bg"));

        List <Recipe> testList = List.of(r1, r2);

        when(service.searchRecipesByTerm("Яйца")).thenReturn(List.of(r1));

        List <Recipe> returnList = ctrl.issueSearchRecipesByTerm("Яйца");
        assertNotEquals(testList, returnList);
        assertEquals(testList.getFirst(), returnList.getFirst());
    }

}
