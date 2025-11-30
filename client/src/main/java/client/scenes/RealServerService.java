package client.scenes;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import recipes.Recipe;
// will perform the network call
public class RealServerService implements ServerService {
    private final Client client = ClientBuilder.newClient();
    private final String recipeEndpointUrl = "http://localhost:8080/recipes";

    @Override
    public void sendRecipe(Recipe recipe) {
        client.target(recipeEndpointUrl)
                .request("application/json")
                .post(Entity.entity(recipe, "application/json"));
    }
}
