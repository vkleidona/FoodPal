package server.api.controllers;

import jakarta.ws.rs.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
     * A Dependency-Injection using constructor for the Recipe API
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
    public ResponseEntity<Long> issueSaveRecipe(@RequestBody Recipe recipe) {
        try {
            Long id = recipeService.saveRecipe(recipe);
            return ResponseEntity.status(HttpStatus.CREATED).body(id);
        }catch (RuntimeException e) {
            return ResponseEntity.internalServerError().build();
        }

    }

    /**
     * Fetches a List of all recipes
     * @return List <Recipe> - returns a list of all Recipe objects
     */
    @GetMapping
    public ResponseEntity<List<Recipe>> issueGetAllRecipes() {
        List <Recipe> recipes = recipeService.getAllRecipes();
        return ResponseEntity.ok(recipes);
    }

    /**
     * Issues a deletion of a recipe from the server given its ID
     * @param id - the ID of the recipe to be deleted
     * @return ResponseEntity <Void> - sends the status of the request
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> issueDeleteRecipe(@PathVariable Long id) {
        boolean successfulDelete = recipeService.deleteRecipe(id);
        if (successfulDelete) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.internalServerError().build();
    }

    /**
     * Issues an update for all ingredients in a recipe
     * @param ingredients - the new List of Ingredients which we provide to the recipe
     * @param recipeId - the recipe to be changed
     * @return ResponseEntity <Recipe> - the Recipe with the new ingredients,
     * together with the response's status code
     */
    @PutMapping(path = "/ingredients/set/{recipeId}")
    public ResponseEntity<Recipe> issueSetAllIngredients(@RequestBody List<Ingredient> ingredients,
                                                         @PathVariable Long recipeId) {
        try {
            Recipe recipe = recipeService.setAllIngredients(ingredients, recipeId);
            return ResponseEntity.ok(recipe);
        }catch (RuntimeException e) {
            System.err.println("Error - could not save ingredients");
            return ResponseEntity.internalServerError().build();
        }

    }

    /**
     * Issues an update of an entire recipe
     * @param id - the ID of the recipe to be updated
     * @param updatedRecipe - the new version of the recipe
     * @return ResponseEntity <Recipe> - the updated recipe, as well as the response status
     */
    @PutMapping(path = "/{id}")
    public ResponseEntity <Recipe> issueUpdateRecipe(@PathVariable("id") Long id,
                                                     @RequestBody Recipe updatedRecipe) {
        try {
            Recipe updated = recipeService.updateRecipe(id, updatedRecipe);
            return ResponseEntity.ok(updated);
        }catch (NotFoundException e) {
            System.err.println("Error - recipe ID not found");
            return ResponseEntity.notFound().build();
        }catch(RuntimeException e) {
            System.err.println("Error - recipe could not be updated");
            return ResponseEntity.internalServerError().build();
        }

    }

    /**
     * A method which issues the searching for recipes by term
     * @param keyword - the keyword by which the user searches in the methods
     * @return List <Recipe> - all recipes which match the term
     */
    @GetMapping("/searchByTerm")
    public List<Recipe> issueSearchRecipesByTerm(@RequestParam String keyword) {
        return recipeService.searchRecipesByTerm(keyword);
    }
}
