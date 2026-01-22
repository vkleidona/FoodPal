package client.scenes.food_item;

import client.network.WebSocketService;
import client.utils.MainCtrl;
import client.service.LanguageService;
import client.utils.ServerUtils;
import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.messaging.simp.stomp.StompSession;
import recipes.FoodItem;
import client.i18n.LanguageManager;
import java.text.MessageFormat;
import java.util.*;

import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;

public class FoodItemMainCtrl {

    private FoodItem currentlySelected;

    private ObservableList<FoodItem> allFoodItems;

    private Long lastSubscribedID;

    private StompSession.Subscription foodItemSubscription;

    @FXML
    private ListView<FoodItem> foodItemListView;
    @FXML
    private Label foodNameLabel;
    @FXML
    private Label nutritionLabel;
    @FXML
    private Label kcalLabel;
    @FXML
    private Label carbsLabel;
    @FXML
    private Label proteinsLabel;
    @FXML
    private Label fatsLabel;
    @FXML
    private Button mainMenuButton;
    @FXML
    private Label recipeUsages;

    @FXML
    private MenuButton languageButton;

    @FXML
    private ResourceBundle resources;

    private final MainCtrl mainCtrl;
    private final ServerUtils serverUtils;
    private final WebSocketService webSocketService;
    private final LanguageService languageService;

    /**
     * A constructor for the FoodItemMainCtrl class using Dependency Injection
     * @param mainCtrl - the instance of the Main Ctrl class (for master methods)
     * @param serverUtils - the instance of the server utilities
     * @param webSocketService - the instance of the service supporting WebSockets
     * @param languageService - the instance of the language service
     */
    @Inject
    public FoodItemMainCtrl(MainCtrl mainCtrl, ServerUtils serverUtils,
                            WebSocketService webSocketService, LanguageService languageService) {
        this.mainCtrl = mainCtrl;
        this.serverUtils = serverUtils;
        this.webSocketService = webSocketService;
        this.languageService = languageService;
    }

    public ListView<FoodItem> getFoodItemListView() {
        return foodItemListView;
    }

    public Label getFoodNameLabel() {
        return foodNameLabel;
    }

    public Label getNutritionLabel() {
        return nutritionLabel;
    }

    public Label getRecipeUsages() {
        return recipeUsages;
    }

    public Label getKcalLabel() {
        return kcalLabel;
    }

    public Label getCarbsLabel() {
        return carbsLabel;
    }

    public Label getProteinsLabel() {
        return proteinsLabel;
    }

    public Label getFatsLabel() {
        return fatsLabel;
    }

    /**
     * An initializer method - tries to load all food items
     */
    @FXML
    public void initialize() {

        allFoodItems = webSocketService.getMasterFoodItemsList();

        allFoodItems.addListener((ListChangeListener <FoodItem>) _ ->
                Platform.runLater(this::updateListView));

        try {
            loadFoodItems();
        } catch (Exception e) {
            System.err.println("Error - Food Items could not be loaded from the database");
        }

        foodItemListView.getSelectionModel().selectedItemProperty().
                addListener((_, _, newVal) -> {
                    if (newVal != null) {
                        currentlySelected = newVal;
                        selectFoodItem(newVal);
                    }
                });

        /*
            Here, the behaviour of how each cell is shown in the UI is overridden
            as we only want to show the name.
            First super.updateItem is called so nothing else goes wrong.
            AI was used for help here
        */
        foodItemListView.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(FoodItem item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });
        updateFoodDetails(null);
        setupLanguageMenu();
    }

    /**
     * A method which updates the UI of the language button
     * @param locale - the locale of the language
     */
    public void updateLanguageButtonUI (Locale locale) {
        String langCode = locale.getLanguage();

        languageButton.setText(languageService.getDisplayName(langCode));
        languageButton.setGraphic(languageService.renderIcon(langCode, 16.0));
    }


    /**
     * A method which sets up the icons for the language menu
     * @param name - the name of the language we are adding
     * @param langCode - the locale code of the language
     * @return MenuItem - returns the ready-made MenuItem with the proper icon
     */
    public MenuItem setupMenuItemIcons (String name, String langCode) {
        MenuItem menuItem = new MenuItem(name);
        menuItem.setGraphic(languageService.renderIcon(langCode, 16.0));
        menuItem.setOnAction(_ -> changeLanguage(Locale.of(langCode)));
        return menuItem;
    }

    /**
     * A setup method for the language menu -
     * adds the constructed MenuItems using the helper methods with the appropriate icon
     */
    public void setupLanguageMenu() {
        languageButton.getItems().clear();

        List<String> codes = List.of("en", "nl", "bg", "ro", "sk", "de");

        for (String code : codes) {
            String displayName = languageService.getDisplayName(code);
            languageButton.getItems().add(setupMenuItemIcons(displayName, code));
        }

        updateLanguageButtonUI(LanguageManager.getLocale());
    }

    /**
     * A method for changing the language of the menus to a new Locale
     * @param newLocale - the new language we want to set the menus to
     */
    public void changeLanguage(Locale newLocale) {
        LanguageManager.setLocale(newLocale);
        updateLanguageButtonUI(newLocale);
        mainCtrl.reloadScenesForNewLanguage();
    }


    /**
     * Updates the selectedFoodItem and subscribes to changes
     * @param foodItem - the FoodItem to be selected
     */
    public void selectFoodItem (FoodItem foodItem) {
        updateFoodDetails(foodItem);

        // If we are currently viewing another foodItem, unsubscribe from it
        if (lastSubscribedID == null || !lastSubscribedID.equals(currentlySelected.getId())) {
            unsubscribeFromFoodItem();
        }

        foodItemSubscription = webSocketService.
                subscribeToFoodItem(currentlySelected.getId(), updated -> Platform.runLater(() -> {
                    updateFoodDetails(updated);
                    for (int i = 0; i < allFoodItems.size(); i++) {
                        if (allFoodItems.get(i).getId().equals(updated.getId())) {
                            allFoodItems.set(i, updated);
                            break;
                        }
                    }
                }));
        lastSubscribedID = currentlySelected.getId();
    }

    /**
     * Updates the details screen to show the details
     * of the currently selected FoodItem
     * @param item - the FoodItem whose details have been selected
     */
    @FXML
    public void updateFoodDetails(FoodItem item) {
        if (item == null) {
            foodNameLabel.setText(resources.getString("foodItem.selectPrompt"));
            recipeUsages.setText("");
            nutritionLabel.setText("");
            kcalLabel.setText("");
            carbsLabel.setText("");
            proteinsLabel.setText("");
            fatsLabel.setText("");
        } else {
            foodNameLabel.setText(MessageFormat.format(
                    resources.getString("foodItem.label.name"), item.getName()));

            if (item.getId() == null || item.getId() < 0) {
                recipeUsages.setText(resources.getString("foodItem.usedIn.zero"));
            } else {
                int usage = serverUtils.getFoodItemRecipeUsage(item);
                recipeUsages.setText(MessageFormat.format(
                        resources.getString("foodItem.usedIn.n"), usage));
            }

            nutritionLabel.setText(resources.getString("foodItem.nutritionHeader"));
            kcalLabel.setText(MessageFormat.format(
                    resources.getString("foodItem.label.kcal"), item.getKcal()));
            carbsLabel.setText(MessageFormat.format(
                    resources.getString("foodItem.label.carbs"), item.getCarbs()));
            proteinsLabel.setText(MessageFormat.format(
                    resources.getString("foodItem.label.proteins"), item.getProteins()));
            fatsLabel.setText(MessageFormat.format(
                    resources.getString("foodItem.label.fats"), item.getFats()));
        }
    }

    /**
     * Updates the List of FoodItems, listening to the server pushing any changes
     */
    public void updateListView () {
        // Only show all recipes if we are in "All Recipes" tab
        // Updates the sidebar when it senses changes in the database
        foodItemListView.getItems().setAll(allFoodItems);

        // Updates the recipe with all of its parameters
        if (currentlySelected != null) {
            allFoodItems.stream()
                    .filter(f -> f.getId().equals(currentlySelected.getId()))
                    .findFirst()
                    .ifPresent(updated -> {
                        this.currentlySelected = updated;
                        foodNameLabel.setText(updated.getName());
                    });
        }

    }

    /**
     * Switches the screen to the AddFoodItem UI - applied
     * when the user wants to add a new FoodItem to the list
     */
    @FXML
    public void addFoodItem() {
        mainCtrl.showAddFoodItem();
        mainCtrl.setCameFromFoodItemMain(true);
    }

    /**
     * Deletes a selected FoodItem the list and the database
     */
    @FXML
    public void deleteFoodItem() {
        FoodItem selectedFoodItem = foodItemListView.getSelectionModel().getSelectedItem();
        if (selectedFoodItem == null) {
            Alert noSelection = new Alert(Alert.AlertType.WARNING);
            noSelection.setTitle(resources.getString
                    ("foodItem.alert.nothingSelected.title"));
            noSelection.setHeaderText(resources.getString
                    ("foodItem.alert.nothingSelected.header"));
            noSelection.setContentText(resources.getString
                    ("foodItem.alert.nothingSelected.delete"));
            noSelection.showAndWait();
            return;
        }

        int recipeUsages = serverUtils.getFoodItemRecipeUsage(selectedFoodItem);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(resources.getString("foodItem.alert.delete.title"));
        alert.setHeaderText(resources.getString("foodItem.alert.delete.header"));
        alert.setContentText(MessageFormat.format(
                resources.getString("foodItem.alert.delete.content"),
                selectedFoodItem.getName()));

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (recipeUsages > 0) {
                Alert alertUsedInRecipe = new Alert(Alert.AlertType.CONFIRMATION);
                alertUsedInRecipe.setTitle(resources.getString
                        ("foodItem.alert.usedInRecipes.title"));
                alertUsedInRecipe.setHeaderText(resources.getString
                        ("foodItem.alert.usedInRecipes.header"));
                alertUsedInRecipe.setContentText(MessageFormat.format(
                        resources.getString("foodItem.alert.usedInRecipes.content"),
                        selectedFoodItem.getName()));

                Optional<ButtonType> result2 = alertUsedInRecipe.showAndWait();
                if (result2.isPresent() && result2.get() == ButtonType.OK) {
                    serverUtils.deleteFoodItem(selectedFoodItem.getId());
                    unsubscribeFromFoodItem();
                }
            } else {
                serverUtils.deleteFoodItem(selectedFoodItem.getId());
                mainCtrl.handleFoodItemBeingDeleted(selectedFoodItem.getId());
                unsubscribeFromFoodItem();
            }
        }
    }

    /**
     * Handles the editing of a selected FoodItem
     */
    @FXML
    public void editFoodItem() {
        FoodItem selectedItem = foodItemListView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            Alert noSelection = new Alert(Alert.AlertType.WARNING);
            noSelection.setTitle(resources.getString("foodItem.alert.nothingSelected.title"));
            noSelection.setHeaderText(resources.getString("foodItem.alert.nothingSelected.header"));
            noSelection.setContentText(resources.getString("foodItem.alert.nothingSelected.edit"));
            noSelection.showAndWait();
            return;
        }
        mainCtrl.handleFoodItemBeingUpdated(selectedItem);
        mainCtrl.showAddFoodItem();
        mainCtrl.getAddFoodItemCtrl().setEditingItem(selectedItem);
    }

    /**
     * Returns the user to the Main UI
     */
    @FXML
    public void showMainUI() {
        mainCtrl.showMainMenu();
    }

    /**
     * Initially loads all FoodItems
     */
    public void loadFoodItems () {
        List<FoodItem> allFoodItems = serverUtils.getAllFoodItems();
        Platform.runLater(() -> webSocketService.
                getMasterFoodItemsList().setAll(allFoodItems));
    }

    /**
     * Refreshes the list of FoodItems by pulling directly from the database
     */
    @FXML
    public void refreshFoodItems(){
        List<FoodItem> allFoodItems = serverUtils.getAllFoodItems();
        ObservableList<FoodItem> items = FXCollections.observableArrayList(allFoodItems);
        foodItemListView.setItems(items);
    }

    /**
     * Handles unsubscribing from the former foodItem instance
     */
    public void unsubscribeFromFoodItem () {
        if (this.foodItemSubscription!= null) {
            foodItemSubscription.unsubscribe();
            foodItemSubscription = null;
        }
    }

}
