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
package client;

import static com.google.inject.Guice.createInjector;

import java.io.IOException;
import java.net.URISyntaxException;

import client.scenes.*;
import com.google.inject.Injector;

import client.utils.ServerUtils;
import javafx.application.Application;


import javafx.stage.Stage;
import shopping.ShoppingList;

public class Main extends Application {

	private static final Injector INJECTOR = createInjector(new MyModule());
	private static final MyFXML FXML = new MyFXML(INJECTOR);

    public static void main(String[] args) throws URISyntaxException, IOException {
		launch();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {

		var serverUtils = INJECTOR.getInstance(ServerUtils.class);
		if (!serverUtils.isServerAvailable()) {
			var msg = "Server needs to be started before the client, but it does not seem to be available. Shutting down.";
			System.err.println(msg);
			return;
		}

		var information = FXML.load(InformationCtrl.class, "client", "scenes", "InformationUI.fxml");

		var overview = FXML.load(QuoteOverviewCtrl.class, "client", "scenes", "QuoteOverview.fxml");
		var add = FXML.load(AddQuoteCtrl.class, "client", "scenes", "AddQuote.fxml");

        var mainUI = FXML.load(MainUICtrl.class, "client", "scenes", "MainUI.fxml");
        var editInstructions = FXML.load
                (EditInstructionsCtrl.class, "client", "scenes", "EditInstructions.fxml");
        var createRecipe = FXML.load
                (CreateRecipeCtrl.class, "client", "scenes", "creatingRecipe.fxml");

        var addFoodItem = FXML.load(AddFoodItemCtrl.class, "client", "scenes", "AddFoodItem.fxml");
        var foodItemMain = FXML.load
                (FoodItemMainCtrl.class, "client", "scenes", "FoodItemMain.fxml");

        var shoppingListMain = FXML.load
                (ShoppingListMainCtrl.class, "client", "scenes", "ShoppingListMainUI.fxml");
        var addRecipeToShoppingList = FXML.load
                (AddRecipeToShoppingListCtrl.class, "client", "scenes", "AddRecipeToShoppingList.fxml");

        var mainCtrl = INJECTOR.getInstance(MainCtrl.class);
        mainCtrl.initialize(primaryStage, overview, add, mainUI, editInstructions,
                createRecipe, addFoodItem, foodItemMain, shoppingListMain, addRecipeToShoppingList, information);
	}

    /**
     * Stores the ingredients of the current shopping list in a file
     * when the application is stopped
     * @throws Exception - a FileNotFoundException might be thrown in case the
     * file has not been found
     */
    @Override
    public void stop() throws Exception {
        ShoppingList.getShoppingList().storeFile();
        super.stop();
    }
}