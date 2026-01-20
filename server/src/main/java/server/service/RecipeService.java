package server.service;

import commons.MiniUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import recipes.FoodItem;
import recipes.Ingredient;
import recipes.Recipe;
import server.repositories.FoodItemRepository;
import server.repositories.IngredientRepository;
import server.repositories.RecipeRepository;

import java.util.List;
import java.util.Optional;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;

    private final FoodItemRepository foodItemRepository;

    private final IngredientRepository ingredientRepository;

    private final SimpMessagingTemplate messageTemplate;

    /**
     * A Dependency-Injection using constructor for all fields of the service class
     * @param recipeRepository - the repository for recipes
     * @param foodItemRepository - the repository for FoodItems
     * @param ingredientRepository - the repository for ingredients
     * @param messageTemplate - the message template used to communicate with the client
     */
    @Autowired
    public RecipeService(RecipeRepository recipeRepository,
                         FoodItemRepository foodItemRepository,
                         IngredientRepository ingredientRepository,
                         SimpMessagingTemplate messageTemplate) {
        this.recipeRepository = recipeRepository;
        this.foodItemRepository = foodItemRepository;
        this.ingredientRepository = ingredientRepository;
        this.messageTemplate = messageTemplate;
    }

    /**
     * Creates a new recipe
     * @param recipe - the new recipe to be created
     * @return ResponseEntity <Long> - the ID of the newly added recipe,
     * as well as a status code for the response
     */
    @PostMapping(consumes = "application/json")
    public ResponseEntity<Long> saveRecipe(@RequestBody Recipe recipe) {
        for (Ingredient ingredient : recipe.getIngredients()) {

            FoodItem incoming = ingredient.getFoodType();
            Optional<FoodItem> match = foodItemRepository.findByName(incoming.getName());
            FoodItem foodItem;

            if (match.isPresent()) {
                foodItem = match.get();
            } else {
                foodItem = incoming;
                foodItemRepository.save(foodItem);
                messageTemplate.convertAndSend("/topic/food_items", foodItem);
            }
            ingredient.setFoodType(foodItem);
            ingredient.setRecipe(recipe);
        }

        Recipe saved = recipeRepository.save(recipe);
        MiniUpdate miniUpdate = new MiniUpdate(saved.getId(), saved.getName());
        messageTemplate.convertAndSend("/topic/recipes/" + saved.getId(), saved);
        messageTemplate.convertAndSend("/topic/recipes/new", saved);
        messageTemplate.convertAndSend("/topic/recipes", miniUpdate);
        return ResponseEntity.ok(saved.getId());
    }

    /**
     * Returns a List of all recipes
     *
     * @return List <Recipe> - returns a list of all Recipe objects
     */
    @GetMapping
    public List<Recipe> getAllRecipes() {
        return recipeRepository.findAll();
    }

    /**
     * Deletes a recipe from the server by ID
     *
     * @param id - the ID of the recipe to be deleted
     * @return ResponseEntity <Void> - sends the status of the request
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable Long id) {
        if (recipeRepository.existsById(id)) {
            recipeRepository.deleteById(id);
            messageTemplate.convertAndSend("/topic/recipes/delete", id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Updates the ingredients of a recipe
     *
     * @param ingredients - the new List of Ingredients which we provide the recipe
     * @param recipeId    - the recipe to be changed
     * @return ResponseEntity <Recipe> - the Recipe with the new ingredients,
     * together with the response's status code
     */
    @PutMapping(path = "/ingredients/set/{recipeId}")
    public ResponseEntity<Recipe> setAllIngredients(@RequestBody List<Ingredient> ingredients,
                                                    @PathVariable Long recipeId) {
        if (recipeRepository.findById(recipeId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return recipeRepository.findById(recipeId).map(currentRecipe -> {
            currentRecipe.getIngredients().clear();

            if (ingredients != null) {
                ingredients.forEach(ingredient -> ingredient.setRecipe(currentRecipe));
                currentRecipe.getIngredients().addAll(ingredients);
            }
            Recipe saved = recipeRepository.save(currentRecipe);
            MiniUpdate miniUpdate = new MiniUpdate(saved.getId(), saved.getName());
            messageTemplate.convertAndSend("/topic/recipes", miniUpdate);
            messageTemplate.convertAndSend("/topic/recipes/" + saved.getId(), saved);
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Updates an entire recipe (used for changing the name)
     *
     * @param id            - the ID of the recipe to be updated
     * @param updatedRecipe - the new version of the recipe
     * @return ResponseEntity <Recipe> - the updated recipe, as well as the response status
     */
    @PutMapping(path = "/{id}")
    public ResponseEntity<Recipe> updateRecipe(@PathVariable("id") Long id,
                                               @RequestBody Recipe updatedRecipe) {
        if (recipeRepository.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        recipeRepository.findById(id).map(oldRecipe -> {
            oldRecipe.setName(updatedRecipe.getName());
            oldRecipe.setServings(updatedRecipe.getServings());
            oldRecipe.setInstructions(updatedRecipe.getInstructions());

            setAllIngredients(updatedRecipe.getIngredients(), oldRecipe.getId());

            Recipe newRecipe = recipeRepository.save(oldRecipe);

            MiniUpdate miniUpdate = new MiniUpdate(newRecipe.getId(), newRecipe.getName());
            messageTemplate.convertAndSend("/topic/recipes", miniUpdate);
            messageTemplate.convertAndSend("/topic/recipes/" + newRecipe.getId(), newRecipe);
            return ResponseEntity.ok(newRecipe);
        });
        return ResponseEntity.ok().build();
    }

    /**
     * A method which searches for recipes by term
     *
     * @param keyword - the keyword by which the user searches in the methods
     * @return List <Recipe> - all recipes which match the term
     */
    @GetMapping("/searchByTerm")
    public List<Recipe> searchRecipesByTerm(@RequestParam String keyword) {
        return recipeRepository.findAllByTerm(keyword);
    }
}
