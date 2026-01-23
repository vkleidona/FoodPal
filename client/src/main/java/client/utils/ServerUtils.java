package client.utils;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import java.net.ConnectException;

import java.util.ArrayList;
import java.util.List;

import client.configuration.ConfigManager;
import jakarta.ws.rs.core.MediaType;
import org.glassfish.jersey.client.ClientConfig;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import recipes.FoodItem;
import recipes.Ingredient;
import recipes.Recipe;

public class ServerUtils {

    public static String getServerUrl() {
        return ConfigManager.getConfig().getServerUrl();
    }

    /**
     * A tester method which determines whether the server is available
     * @return true if the server has started and false otherwise
     */
    public boolean isServerAvailable() {
        try {
            ClientBuilder.newClient(new ClientConfig()) //
					.target(getServerUrl()) //
					.request(APPLICATION_JSON) //
					.get();
        } catch (ProcessingException e) {
            if (e.getCause() instanceof ConnectException) {
				return false;
            }
        }
        return true;
    }

    /**
     * A method which issues a server request for deleting a recipe
     * @param id - the recipe to be deleted
     */
    public void deleteRecipe (Long id) {
        ClientBuilder.newClient(new ClientConfig())
                .target(getServerUrl() + "/api/recipes/" + id)
                .request(APPLICATION_JSON)
                .delete();
    }

    /**
     * A method used for updating a recipe on the server
     * @param recipe - the new Recipe object to update
     */
    public void updateRecipe(Recipe recipe) {
        ClientBuilder.newClient(new ClientConfig())
                .target(getServerUrl() + "/api/recipes/" + recipe.getId())
                .request(APPLICATION_JSON)
                .put(Entity.entity(recipe, APPLICATION_JSON), Recipe.class);
    }

    /**
     * A method which updates the ingredients of a recipe
     * @param recipe      - the Recipe whose ingredients should be updated
     * @param ingredients - the new list of ingredients to update the recipe with
     */
    public void updateIngredients (Recipe recipe, List <Ingredient> ingredients) {
        ClientBuilder.newClient(new ClientConfig())
                .target(getServerUrl() + "/api/recipes/")
                .path("/ingredients/set/" + recipe.getId())
                .request(APPLICATION_JSON)
                .put(Entity.entity(ingredients, APPLICATION_JSON), Recipe.class);
    }


    /**
     * Searches the FoodItem repository for type
     * @param foodType - String entered by the user which we will search for FoodType
     * @return FoodItem - the corresponding FoodItem
     */
    public FoodItem getFoodItemByName (String foodType) {
        return ClientBuilder.newClient(new ClientConfig()).
                target(getServerUrl()).path("/api/fooditems/search").
                queryParam("name", foodType).
                request(APPLICATION_JSON).
                get(FoodItem.class);
    }

    /**
     * Gets all recipes in the database
     * @return List <Recipe> - returns a List of all recipes
     */
    public List<Recipe> getAllRecipes() {
        return ClientBuilder.newClient(new ClientConfig()).
                target(getServerUrl()).path("/api/recipes")
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<>() {
                });
    }

    /**
     * Returns a List of Recipes which correspond to one search term
     * @param term - the term could be the recipe's name, ingredients or instructions
     * @return List <Recipe> - the list of recipes which correspond to the right term
     */
    public List<Recipe> searchRecipesByTerm(String term){
        return ClientBuilder.newClient(new ClientConfig())
                .target(getServerUrl()).path("/api/recipes/searchByTerm")
                .queryParam("keyword", term)
                .request(APPLICATION_JSON)
                .get(new GenericType<>() {
                });
    }

    /**
     * A method for adding a FoodItem object to the database
     * @param foodItem - the FoodItem object to be added
     * @return FoodItem - the added FoodItem is returned
     */
    public FoodItem addFoodItem(FoodItem foodItem) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(getServerUrl()).path("/api/fooditems")
                .request(APPLICATION_JSON)
                .post(Entity.entity(foodItem, APPLICATION_JSON), FoodItem.class);
    }

    /**
     * A method which retrieves all FoodItems from the database
     * @return List <FoodItem> - a list of all FoodItems in the database
     */
    public List<FoodItem> getAllFoodItems() {
        try {
            return ClientBuilder.newClient(new ClientConfig())
                    .target(getServerUrl()).path("/api/fooditems")
                    .request(APPLICATION_JSON)
                    .get(new GenericType<>() {
                    });
        }catch (RuntimeException e) {
            System.err.println("Failed to fetch food items: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * A method which deletes a FoodItem from the database by ID
     * @param id - the ID of the FoodItem to be deleted
     */
    public void deleteFoodItem(Long id) {
        ClientBuilder.newClient(new ClientConfig())
                .target(getServerUrl()).path("/api/fooditems/" + id)
                .request(APPLICATION_JSON)
                .delete();
    }

    /**
     * A method which is used for updating every FoodItem in the database
     * @param id - the ID of the FoodItem to be updated
     * @param foodItem - the new FoodItem object to be put in place of the old one
     */
    public void updateFoodItem(Long id, FoodItem foodItem) {
        foodItem.setId(id);
        ClientBuilder.newClient(new ClientConfig())
                .target(getServerUrl()).path("/api/fooditems/" + foodItem.getId())
                .request(APPLICATION_JSON)
                .put(Entity.entity(foodItem, APPLICATION_JSON), FoodItem.class);
    }

    /**
     * A method which retrieves the usage of one FoodItems in recipes
     * @param foodItem - the FoodItem whose usage is to be searched
     * @return int - the number of recipes the FoodItem is used in
     */
    public int getFoodItemRecipeUsage(FoodItem foodItem) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(getServerUrl()).path("/api/ingredients/usage/" + foodItem.getId())
                .request(APPLICATION_JSON).get(Integer.class);
    }

    /**
     * A method for creating a new recipe on the server
     * @param recipe - the recipe to be sent to the server
     */
    public void sendRecipe(Recipe recipe) {
        String recipeEndpointUrl = "http://localhost:8080/api/recipes";
        ClientBuilder.newClient(new ClientConfig()).target(recipeEndpointUrl)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(recipe, MediaType.APPLICATION_JSON));
    }
}