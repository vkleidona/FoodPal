package server.api.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import recipes.FoodItem;
import server.repositories.FoodItemRepository;
import server.repositories.IngredientRepository;

@RestController
@RequestMapping("api/ingredients")
public class IngredientController {

    @Autowired
    private final IngredientRepository ingredientRepo;
    @Autowired
    private final FoodItemRepository foodItemRepo;

    /**
     * A dependency injection-using controller for the Ingredient API
     * @param ingredientRepo - the repository for Ingredients
     * @param foodItemRepo - the repository for FoodItems
     */
    @Autowired
    public IngredientController(IngredientRepository ingredientRepo,
                                FoodItemRepository foodItemRepo) {
        this.ingredientRepo = ingredientRepo;
        this.foodItemRepo = foodItemRepo;
    }

    /**
     * Returns the usage of a FoodItem inside recipes
     * @param id - the FoodItem from the recipe to be used
     * @return int - how many times has this FoodItem been used in recipes
     */
    @GetMapping("/usage/{id}")
    public int getFoodItemRecipeUsage(@PathVariable Long id) {
        FoodItem foodItem = foodItemRepo.findById(id).orElseThrow();
        return ingredientRepo.countFoodItemRecipeUsages(foodItem);
    }
}

