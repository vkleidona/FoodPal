package client.scenes;

import recipes.FoodItem;
import recipes.Recipe;

import java.util.List;

public interface ServerService {
    void sendRecipe(Recipe recipe);
    List<FoodItem> getAllFoodItems();

    // Added this alias too, just in case your interface uses this name instead
    List<FoodItem> getFoodItems();
}
