package server.api.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import recipes.*;
import server.api.IngredientRepository;
import server.api.repositories.FoodItemRepository;
import server.api.repositories.RecipeRepository;

import java.util.ArrayList;
import java.util.List;
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
    @Autowired
    private IngredientRepository ingredientRepository;

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
}
