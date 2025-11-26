package client.scenes;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Callback;
import recipes.Recipe;

import java.util.ArrayList;
import java.util.List;

public class TestUI extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 1. Setup the FXML Loader
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/scenes/EditInstructions.fxml"));

        // 2. TRICK: We manually create the controller so we can pass 'null' dependencies
        // This bypasses the need for the real server or MainCtrl for now
        loader.setControllerFactory(new Callback<Class<?>, Object>() {
            @Override
            public Object call(Class<?> param) {
                // Pass null for ServerUtils and MainCtrl just for this visual test
                return new EditInstructionsCtrl(null, null);
            }
        });

        // 3. Load the UI
        Parent root = loader.load();

        // 4. Get the controller and pass it some FAKE data to test
        EditInstructionsCtrl ctrl = loader.getController();

        // Create a fake recipe to test with
        Recipe testRecipe = new Recipe(); // assuming Recipe is in commons
        testRecipe.setInstructions(new ArrayList<>(List.of("Step 1: Preheat oven", "Step 2: Mix flour")));

        // Load the data
        ctrl.initData(testRecipe);

        // 5. Show the window
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Test Instructions UI");
        primaryStage.show();
    }
}