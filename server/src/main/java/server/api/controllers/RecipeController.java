package server.api.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import recipes.*;
import server.api.repositories.FoodItemRepository;
import server.api.repositories.RecipeRepository;

import java.util.Optional;

@RestController
// /recipes: endpoint, prepares it
// to handle all requests sent to "/recipes"
@RequestMapping("/recipes")
public class RecipeController {

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private FoodItemRepository foodItemRepository;

    @PostMapping(consumes = "application/json")
    // @RequestBody: the JSON format is turned into a Java object
    public ResponseEntity<Long> saveRecipe(@RequestBody Recipe recipe) {
        for (Ingredient ingredient : recipe.getIngredients()) {

            FoodItem incoming = ingredient.getFoodType();
            Optional<FoodItem> match = foodItemRepository.findByName(incoming.getName());
            FoodItem foodItem;

            if (match.isPresent()) {
                foodItem = match.get();
            } else {
                foodItem = incoming;
                foodItemRepository.save(foodItem); // save new FoodItem
            }

            ingredient.setFoodType(foodItem);
            ingredient.setRecipe(recipe);
        }

        Recipe saved = recipeRepository.save(recipe);
        return ResponseEntity.ok(saved.getId());
    }
}
