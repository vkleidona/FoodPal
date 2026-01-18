package client.scenes;

import recipes.FoodItem;
import recipes.Recipe;

import java.util.List;

public interface ServerService {

    /**
     * A method which handles the sending of a recipe to the server
     * @param recipe - the recipe to be posted to the server
     */
    void sendRecipe(Recipe recipe);
    List<FoodItem> getAllFoodItems();

}
