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

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.glassfish.jersey.client.ClientConfig;

import commons.Quote;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import recipes.Recipe;

public class ServerUtils {

	private static final String SERVER = "http://localhost:8080/";

	public void getQuotesTheHardWay() throws IOException, URISyntaxException {
		var url = new URI("http://localhost:8080/api/quotes").toURL();
		var is = url.openConnection().getInputStream();
		var br = new BufferedReader(new InputStreamReader(is));
		String line;
		while ((line = br.readLine()) != null) {
			System.out.println(line);
		}
	}

	public List<Quote> getQuotes() {
		return ClientBuilder.newClient(new ClientConfig()) //
				.target(SERVER).path("api/quotes") //
				.request(APPLICATION_JSON) //
				.get(new GenericType<List<Quote>>() {});
	}

	public Quote addQuote(Quote quote) {
		return ClientBuilder.newClient(new ClientConfig()) //
				.target(SERVER).path("api/quotes") //
				.request(APPLICATION_JSON) //
				.post(Entity.entity(quote, APPLICATION_JSON), Quote.class);
	}

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

    public void goToMainUI(Scene currentScene) {
        try {
            // loading the fxml file, the UI where the user will be directed
            // between "" the filepath to the main UI needs to be added
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/scenes/MainUI.fxml"));

            // this builds the UI, meaning any previous user input will be reset
            /*
            this can be adjusted, depending on whether we want to keep the
            previous user input, or not
             */
            Parent mainRoot = loader.load();

            // we will have here our current scene, so then it can be changed
            Stage stage = (Stage) currentScene.getWindow();

            // a new scene is created
            Scene mainScene = new Scene(mainRoot);

            // the current scene is replaced with the new one
            stage.setScene(mainScene);
        } catch (IOException e){
            // if loading the FXML file fails, the error will be caught here
        }
    }
}