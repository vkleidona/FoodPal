package server.api.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import recipes.*;
import server.repositories.FoodItemRepository;
import server.repositories.RecipeRepository;

import java.util.Optional;
import java.util.List;

@RestController
@RequestMapping("/recipes")
public class RecipeController {

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private FoodItemRepository foodItemRepository;

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

    @GetMapping
    public List<Recipe> getAllRecipes() {
        return recipeRepository.findAll();
    }

    // ✅ New: Delete a recipe by ID
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
