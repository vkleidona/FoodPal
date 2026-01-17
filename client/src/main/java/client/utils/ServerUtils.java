/*
 * Copyright 2021 Delft University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package client.utils;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.MediaType;
import org.checkerframework.checker.units.qual.C;
import org.glassfish.jersey.client.ClientConfig;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import recipes.FoodItem;
import recipes.Ingredient;
import recipes.Recipe;

public class ServerUtils {

	private static final String SERVER = "http://localhost:8080/";

	public boolean isServerAvailable() {
		try {
			ClientBuilder.newClient(new ClientConfig()) //
					.target(SERVER) //
					.request(APPLICATION_JSON) //
					.get();
		} catch (ProcessingException e) {
			if (e.getCause() instanceof ConnectException) {
				return false;
			}
		}
		return true;
	}

    public Recipe updateRecipe(Recipe recipe) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER + "api/recipes/" + recipe.getId())
                .request(APPLICATION_JSON)
                .put(Entity.entity(recipe, APPLICATION_JSON), Recipe.class);
    }

    public Recipe updateIngredients (Recipe recipe, List <Ingredient> ingredients) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER + "api/recipes/")
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
                target(SERVER).path("api/fooditems/search").
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
                target(SERVER).path("api/recipes")
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<Recipe>>() {});
    }


    public List<Recipe> searchRecipesByTerm(String term){
        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER).path("api/recipes/searchByTerm")
                .queryParam("keyword", term)
                .request(APPLICATION_JSON)
                .get(new GenericType<List<Recipe>>() {});
    }

    public FoodItem addFoodItem(FoodItem foodItem) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER).path("api/fooditems")
                .request(APPLICATION_JSON)
                .post(Entity.entity(foodItem, APPLICATION_JSON), FoodItem.class);
    }

    public List<FoodItem> getAllFoodItems() {
        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER).path("api/fooditems")
                .request(APPLICATION_JSON)
                .get(new GenericType<List<FoodItem>>() {});
    }

    public void deleteFoodItem(Long id) {
        ClientBuilder.newClient(new ClientConfig())
                .target(SERVER).path("api/fooditems/" + id)
                .request(APPLICATION_JSON)
                .delete();
    }

    public FoodItem updateFoodItem(Long id, FoodItem foodItem) {
        foodItem.setId(id);
        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER).path("api/fooditems/" + foodItem.getId())
                .request(APPLICATION_JSON)
                .put(Entity.entity(foodItem, APPLICATION_JSON), FoodItem.class);
    }

    public int getFoodItemRecipeUsage(FoodItem foodItem) {
        return ClientBuilder.newClient(new ClientConfig())
                .target(SERVER).path("api/ingredients/usage/" + foodItem.getId())
                .request(APPLICATION_JSON).get(Integer.class);
    }
}