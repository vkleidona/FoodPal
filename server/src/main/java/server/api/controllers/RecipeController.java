package server.api.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import recipes.*;
import server.repositories.FoodItemRepository;
import server.repositories.RecipeRepository;
import server.repositories.IngredientRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/recipes")
public class RecipeController {

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private FoodItemRepository foodItemRepository;
    @Autowired
    private IngredientRepository ingredientRepository;

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
            }

            ingredient.setFoodType(foodItem);
            ingredient.setRecipe(recipe);
        }

        Recipe saved = recipeRepository.save(recipe);
        return ResponseEntity.ok(saved.getId());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<Recipe> getById(@PathVariable("id") long id) {
        if(id < 0 || !recipeRepository.existsById(id)){
            return ResponseEntity.notFound().build();
        }
        Recipe recipe = recipeRepository.findById(id).get();
        System.out.println("Recipe: " + recipe);
        return ResponseEntity.ok(recipe);
    }

    // Currently does not work. I think?
    @PostMapping(path = "/ingredients/set/{recipeId}")
    public ResponseEntity<Recipe> setAllIngredients(@RequestBody List<Ingredient> ingredients,
                                                    @PathVariable Long recipeId) {
        if (recipeId < 0 || !recipeRepository.existsById(recipeId)) {
            return ResponseEntity.notFound().build();
        }

        Recipe currentRecipe = recipeRepository.findById(recipeId).get();

        ingredientRepository.deleteAll(currentRecipe.getIngredients());

        currentRecipe.setIngredients(new ArrayList<>());

        for  (Ingredient ingredient : ingredients) {
            currentRecipe.addIngredient(ingredient);
        }

        Recipe saved = recipeRepository.save(currentRecipe);

        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public List<Recipe> getAllRecipes() {
        return recipeRepository.findAll();
    }

    //Delete a recipe by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable Long id) {
        if (recipeRepository.existsById(id)) {
            recipeRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
