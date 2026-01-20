package server.api.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import recipes.*;
import server.service.RecipeService;

import java.util.List;

@RestController
@RequestMapping("api/recipes")
public class RecipeController {

    private final RecipeService recipeService;

    /**
     * A Dependency-Injection using constructor for all fields of the controller class
     * @param recipeService - the service class for handling all business logic of recipes
     */
    @Autowired
    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    /**
     * Issues the creation of a new recipe
     * @param recipe - the new recipe to be created
     * @return ResponseEntity <Long> - the ID of the newly added recipe,
     * as well as a status code for the response
     */
    @PostMapping(consumes = "application/json")
    public ResponseEntity<Long> handleSaveRecipe(@RequestBody Recipe recipe) {
        return recipeService.saveRecipe(recipe);
    }

    /**
     * Fetches a List of all recipes
     * @return List <Recipe> - returns a list of all Recipe objects
     */
    @GetMapping
    public List<Recipe> handleGetAllRecipes() {
        return recipeService.getAllRecipes();
    }

    /**
     * Issues a recipe delete from the server by ID
     * @param id - the ID of the recipe to be deleted
     * @return ResponseEntity <Void> - sends the status of the request
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable Long id) {
        return recipeService.deleteRecipe(id);
    }

    /**
     * Sets an update for all ingredients in a recipe
     * @param ingredients - the new List of Ingredients which we provide to the recipe
     * @param recipeId - the recipe to be changed
     * @return ResponseEntity <Recipe> - the Recipe with the new ingredients,
     * together with the response's status code
     */
    @PutMapping(path = "/ingredients/set/{recipeId}")
    public ResponseEntity<Recipe> setAllIngredients(@RequestBody List<Ingredient> ingredients,
                                                    @PathVariable Long recipeId) {
        return recipeService.setAllIngredients(ingredients, recipeId);
    }

    /**
     * Issues an update of an entire recipe
     * @param id - the ID of the recipe to be updated
     * @param updatedRecipe - the new version of the recipe
     * @return ResponseEntity <Recipe> - the updated recipe, as well as the response status
     */
    @PutMapping(path = "/{id}")
    public ResponseEntity <Recipe> updateRecipe (@PathVariable("id") Long id,
                                                 @RequestBody Recipe updatedRecipe) {
        return recipeService.updateRecipe(id, updatedRecipe);
    }

    /**
     * A method which issues the searching for recipes by term
     * @param keyword - the keyword by which the user searches in the methods
     * @return List <Recipe> - all recipes which match the term
     */
    @GetMapping("/searchByTerm")
    public List<Recipe> searchRecipesByTerm(@RequestParam String keyword) {
        return recipeService.searchRecipesByTerm(keyword);
    }
}
