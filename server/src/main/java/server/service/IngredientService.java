package server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import recipes.FoodItem;
import server.repositories.FoodItemRepository;
import server.repositories.IngredientRepository;

@Service
public class IngredientService {

    private final IngredientRepository ingredientRepo;

    private final FoodItemRepository foodItemRepo;

    /**
     * A dependency injection-using service class for the Ingredient API
     * @param ingredientRepo - the repository for Ingredients
     * @param foodItemRepo - the repository for FoodItems
     */
    @Autowired
    public IngredientService (IngredientRepository ingredientRepo,
                                FoodItemRepository foodItemRepo) {
        this.ingredientRepo = ingredientRepo;
        this.foodItemRepo = foodItemRepo;
    }

    /**
     * Returns the number of recipes a FoodItem has been used in
     * @param id - the FoodItem to be checked
     * @return int - how many times has this FoodItem been used in recipes
     */
    public int serveFoodItemRecipeUsage(Long id) {
        FoodItem foodItem = foodItemRepo.findById(id).orElseThrow();
        return ingredientRepo.countFoodItemRecipeUsages(foodItem);
    }
}
