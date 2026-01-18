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

    /**
     * A method for creating a new recipe on the server
     * @param recipe - the recipe to be sent to the server
     */
    @Override
    public void sendRecipe(Recipe recipe) {
        String recipeEndpointUrl = "http://localhost:8080/api/recipes";
        client.target(recipeEndpointUrl)
                .request(MediaType.APPLICATION_JSON) // Use constant for safety
                .post(Entity.entity(recipe, MediaType.APPLICATION_JSON));
    }

    /**
     * A method which retrieves all food items from the database
     * @return List <FoodItem> - a list of all food items in the database
     */
    @Override
    public List<FoodItem> getAllFoodItems() {
        try {
            String foodItemEndpointUrl = "http://localhost:8080/api/fooditems";
            return client.target(foodItemEndpointUrl)
                    .request(MediaType.APPLICATION_JSON)
                    .get(new GenericType<>() {
                    });
        } catch (Exception e) {
            System.err.println("Failed to fetch food items: " + e.getMessage());
            return new ArrayList<>(); // return empty list on failure
        }
    }

}
