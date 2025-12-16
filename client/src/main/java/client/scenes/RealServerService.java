package client.scenes;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import recipes.FoodItem;
import recipes.Recipe;

import java.util.ArrayList;
import java.util.List;

// will perform the network call
public class RealServerService implements ServerService {

    private final Client client = ClientBuilder.newClient();
    private final String recipeEndpointUrl = "http://localhost:8080/recipes";
    private final String foodItemEndpointUrl = "http://localhost:8080/api/fooditems";

    @Override
    public void sendRecipe(Recipe recipe) {
        client.target(recipeEndpointUrl)
                .request(MediaType.APPLICATION_JSON) // Use constant for safety
                .post(Entity.entity(recipe, MediaType.APPLICATION_JSON));
    }

    @Override
    public List<FoodItem> getAllFoodItems() {
        try {
            return client.target(foodItemEndpointUrl)
                    .request(MediaType.APPLICATION_JSON)
                    .get(new GenericType<List<FoodItem>>() {});
        } catch (Exception e) {
            System.err.println("Failed to fetch food items: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>(); // return empty list on failure
        }
    }

    @Override
    public List<recipes.FoodItem> getFoodItems() {
        return getAllFoodItems();
    }

}
