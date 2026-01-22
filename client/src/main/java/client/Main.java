package client;

import static com.google.inject.Guice.createInjector;

import java.io.IOException;
import java.net.URISyntaxException;

import client.configuration.ConfigManager;
import client.service.WebSocketService;
import client.scenes.food_item.AddFoodItemCtrl;
import client.scenes.food_item.FoodItemMainCtrl;
import client.scenes.main.CreateRecipeCtrl;
import client.scenes.main.EditIngredientsCtrl;
import client.scenes.main.EditInstructionsCtrl;
import client.scenes.main.MainUICtrl;
import client.scenes.nutrition.InformationCtrl;
import client.scenes.shopping_list.AddRecipeToShoppingListCtrl;
import client.scenes.shopping_list.ShoppingListMainCtrl;
import client.utils.MainCtrl;
import com.google.inject.Injector;

import client.utils.ServerUtils;
import javafx.application.Application;


import javafx.stage.Stage;
import shopping.ShoppingList;

public class Main extends Application {

    private static final Injector INJECTOR = createInjector(new MyModule());
    private static final MyFXML FXML = new MyFXML(INJECTOR);

    /**
     * The Main method of the Application - it starts the client
     * @param args - the arguments with which the application starts
     * @throws URISyntaxException - a possible Exception with the provided URI syntax
     * in the arguments of Main
     * @throws IOException - a possible Exception with the file
     */
    public static void main(String[] args) throws URISyntaxException, IOException {
        String customConfig = null;

        for (int i = 0; i < args.length; i++) {
            if ("-cfg".equals(args[i]) && i + 1 < args.length) {
                customConfig = args[i + 1];
                break;
            }
        }

        ConfigManager.init(customConfig);

        launch();
    }

    /**
     * A method which starts up the application and loads all instances
     * @param primaryStage - the primary stage to be shown to the user
     */
    @Override
	public void start(Stage primaryStage) {

        var mainCtrl = INJECTOR.getInstance(MainCtrl.class);

        var serverUtils = INJECTOR.getInstance(ServerUtils.class);
        if (!serverUtils.isServerAvailable()) {
            var msg = "Server needs to be started before the client, " +
                    "but it does not seem to be available. Shutting down.";
            System.err.println(msg);
			return;
        }

        var information = FXML.load(InformationCtrl.class,
                "client", "scenes", "InformationUI.fxml");

        var mainUI = FXML.load(MainUICtrl.class, "client", "scenes", "MainUI.fxml");
        var editInstructions = FXML.load
                (EditInstructionsCtrl.class, "client", "scenes", "EditInstructions.fxml");
        var createRecipe = FXML.load
                (CreateRecipeCtrl.class, "client", "scenes", "creatingRecipe.fxml");

        var addFoodItem = FXML.load(AddFoodItemCtrl.class, "client", "scenes", "AddFoodItem.fxml");
        var foodItemMain = FXML.load
                (FoodItemMainCtrl.class, "client", "scenes", "FoodItemMain.fxml");

        var editIngredients = FXML.load(EditIngredientsCtrl.class,
                "client", "scenes", "EditIngredients.fxml");

        var shoppingListMain = FXML.load(ShoppingListMainCtrl.class,
                "client", "scenes", "ShoppingListMainUI.fxml");

        var addRecipeToShoppingList = FXML.load(AddRecipeToShoppingListCtrl.class,
                "client", "scenes", "AddRecipeToShoppingList.fxml");

        WebSocketService wss = INJECTOR.getInstance(WebSocketService.class);
        wss.connect(() -> {
            System.out.println("Session established! Starting UI...");
            mainCtrl.initialize(primaryStage, mainUI, editInstructions,
                    createRecipe, addFoodItem, foodItemMain, editIngredients,
                    shoppingListMain, addRecipeToShoppingList, information);

            mainUI.getKey().initialRecipeFetch();
        });
    }

    /**
     * A method which defines the behavior of the application when stopping - namely
     * to store the ShoppingList inside a file
     * @throws Exception - any Exception which might be thrown when storing the Shopping list file
     */
    @Override
    public void stop() throws Exception {
        INJECTOR.getInstance(ShoppingList.class).storeFile();
        super.stop();
    }
}