package server.api.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import server.service.IngredientService;

@RestController
@RequestMapping("api/ingredients")
public class IngredientController {

    private final IngredientService ingredientService;

    /**
     * A dependency injection-using controller for the Ingredient API
     * @param ingredientService - the service class for the Ingredient controller
     */
    @Autowired
    public IngredientController(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    /**
     * Returns the usage of a FoodItem inside recipes
     * @param id - the FoodItem from the recipe to be used
     * @return int - how many times has this FoodItem been used in recipes
     */
    @GetMapping("/usage/{id}")
    public int issueGetFoodItemRecipeUsage(@PathVariable Long id) {
        return ingredientService.getFoodItemRecipeUsage(id);
    }
}

