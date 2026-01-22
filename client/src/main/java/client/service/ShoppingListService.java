package client.service;

import client.configuration.ConfigManager;
import jakarta.inject.Inject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.stereotype.Service;
import recipes.Ingredient;
import shopping.ShoppingList;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ShoppingListService {

    private final ObservableList<Ingredient> ingredientsObservableList;
    private ShoppingList shoppingList;

    private double quantity;
    private String measurement;

    /**
     * A dependency-injection using constructor for the ShoppingList service class
     */
    @Inject
    public ShoppingListService () {
        this.ingredientsObservableList = FXCollections.observableArrayList();
        this.shoppingList = loadShoppingList();
        reloadList();
    }

    /**
     * A getter for the ShoppingList parameter - needed for access
     * from multiple places
     * @return - ShoppingList - returns the ShoppingList instance
     */
    public ShoppingList getShoppingList() {
        return shoppingList;
    }

    public ObservableList<Ingredient> getIngredientsObservableList() {
        return ingredientsObservableList;
    }

    /**
     * Refreshes the interface to match the current state of the Shopping List
     */
    public void reloadList() {
        this.shoppingList = loadShoppingList();
        List<Ingredient> loaded = shoppingList.getIngredients();

        if (loaded == null) {
            loaded = new ArrayList<>();
            shoppingList.setIngredients(loaded);
        }

        ingredientsObservableList.setAll(loaded);
    }

    /**
     * Clears the ShoppingList of Ingredients
     */
    @FXML
    public void reset () {
        shoppingList.getIngredients().clear();
        ingredientsObservableList.clear();
    }

    /**
     * Creates a printable version of the ShoppingList
     * @param stage - the stage the printing function comes from
     */
    @FXML
    public void print (Stage stage) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().
                add(new FileChooser.ExtensionFilter("Shopping_list", "*.txt"));

        File file = fileChooser.showSaveDialog(stage);

        try (FileWriter writer = new FileWriter(file)) {
            for (Ingredient ingredient : ingredientsObservableList) {
                writer.write(ingredient.toString());
                writer.write("\n");
            }
        }
    }

    /**
     * A method for loading all possible measurement types into the choice box
     * @param choiceBox - the choice box where we load the items
     */
    public void loadItemsIntoChoiceBox (ChoiceBox<String> choiceBox) {
        List <String> availableMeasurementTypes =
                List.of("g", "kg", "L", "mL", "oz", "lb", "fl oz", "cup", "tbsp", "tsp",
                        "slice", "clove", "stick", "pinch", "dash", "jar", "-");
        choiceBox.getItems().addAll(availableMeasurementTypes);
    }


    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getMeasurement() {
        return measurement;
    }

    public void setMeasurement(String measurement) {
        this.measurement = measurement;
    }

    /**
     * A middleman method triggered when we wish to save the
     * Shopping list
     */
    public void saveShoppingList() {
        ConfigManager.save();
    }

    /**
     * Loads the Shopping list from the configuration
     * @return Shopping List - loads a Shopping List from the config
     */
    public ShoppingList loadShoppingList() {
        ShoppingList list = ConfigManager.getConfig().getShoppingList();
        if (list == null) {
            list = new ShoppingList();
            ConfigManager.getConfig().setShoppingList(list);
        }
        return list;
    }

}
