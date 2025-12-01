package client.scenes;

import client.utils.ServerUtils;
import jakarta.inject.Inject;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import javafx.util.Pair;
import recipes.Ingredient;
import shopping.ShoppingList;

import java.util.*;


public class ShoppingListMainCtrl {

    @FXML
    private Button resetButton;

    @FXML
    private Button returnToMainButton;

    @FXML
    private Button printButton;

    @FXML
    private Button addIngredientButton;

    @FXML
    private ListView <Ingredient> ingredientsListView;
    private ObservableList<Ingredient> ingredientsObservableList;

    private ShoppingList s;

    private final ServerUtils server;
    private final MainCtrl mainCtrl;


    @Inject
    public ShoppingListMainCtrl (ServerUtils server, MainCtrl mainCtrl) {
        this.server = server;
        this.mainCtrl = mainCtrl;
    }

    public void initialize() {
        //TODO: Change that to the actual ShoppingList
        s = ShoppingList.getShoppingList();
        List <Ingredient> ingredientList = s.getIngredients();
        if (ingredientList==null) {
           ingredientList = new ArrayList<>();
        }
        ingredientsObservableList = FXCollections.observableArrayList(ingredientList);
        ingredientsListView.setItems(ingredientsObservableList);
    }

    @FXML
    public void reset () {
        ingredientsListView.getItems().clear();
    }

    @FXML
    public void print () {

    }

    @FXML
    public void addIngredient () {

    }

    @FXML
    public void returnToMain () {
        mainCtrl.showMainMenu();
    }
}
