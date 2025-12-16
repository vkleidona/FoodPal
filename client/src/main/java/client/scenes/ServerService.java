package client.scenes;

import recipes.FoodItem;
import recipes.Recipe;

import java.util.List;

public interface ServerService {
    void sendRecipe(Recipe recipe);
    List<FoodItem> getAllFoodItems();
}
