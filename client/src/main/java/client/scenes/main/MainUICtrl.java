package client.scenes.main;

import client.network.WebSocketService;
import client.utils.MainCtrl;
import client.service.FavoritesService;
import client.service.FilterService;
import client.service.LanguageService;
import client.utils.ServerUtils;
import jakarta.inject.Inject;
import javafx.application.Platform;
import client.i18n.LanguageManager;

import java.util.*;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.messaging.simp.stomp.StompSession;
import recipes.Recipe;
import recipes.Ingredient;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class MainUICtrl {

    @FXML private ListView<Recipe> recipeListView;

    @FXML private Button addRecipeButton;
    @FXML private Button deleteRecipeButton;
    @FXML private Button editNameButton;
    @FXML private Button editIngredientsButton;
    @FXML private Button editInstructionsButton;
    @FXML private Button foodItemButton;
    @FXML private Button shoppingListButton;
    @FXML private Button addToShoppingListButton;

    @FXML private Button refreshButton;

    @FXML private Label recipeNameLabel;
    @FXML private VBox ingredientsVBox;
    @FXML private VBox instructionsVBox;

    @FXML private Label servingsLabel;

    @FXML private TextField recipeSearchBar;

    @FXML private ImageView languageIndicator;
    @FXML private MenuButton filterLanguagesButton;

    private ObservableList<Recipe> currentRecipes;
    private Recipe selectedRecipe;

    private final ServerUtils server;
    private final MainCtrl mainCtrl;
    private final WebSocketService webSocketService;

    private Long lastSubscribedID = null;
    private StompSession.Subscription recipeSubscription;

    private final FavoritesService favoritesService;
    private final FilterService filterService;
    private final LanguageService languageService;

    @FXML
    private MenuButton languageButton;

    /**
     * Cleans up the local favorites list by removing recipe IDs that no longer
     * exist in the current master recipe list received from the server.
     * This method detects favorites that were deleted by someone else on the
     * server, removes those IDs from {@code favoriteRecipeIds}, updates the
     * persisted favorites file, refreshes the favorites filter, and shows a
     * warning alert to inform the user that some favorite recipes are gone.
     */
    private void cleanupDeletedFavorites() {

        boolean deleted = favoritesService.cleanupDeletedFavorites();

        if (deleted) {
            showFavoriteDeletedWarning();
            filterService.updatePredicate();
        }
    }

    private void showFavoriteDeletedWarning() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(LanguageManager.getString("main.favoriteDeleted.title"));
            alert.setHeaderText(LanguageManager.getString("main.favoriteDeleted.header"));
            alert.setContentText(LanguageManager.getString("main.favoriteDeleted.content"));
            alert.showAndWait();
        });
    }


    @FXML private ToggleButton favoriteButton;
    @FXML private ToggleButton allRecipesTab;
    @FXML private ToggleButton favoritesTab;

    /**
     * A method for changing the recipe status to favorite and the inverse
     */
    @FXML
    public void toggleFavorite() {
        Recipe current = recipeListView.getSelectionModel().getSelectedItem();
        if (current == null) return;

        if (favoriteButton.isSelected()) {
            favoritesService.getFavoriteRecipeIds().add(current.getId());
            favoriteButton.setText("★");
        } else {
            favoritesService.getFavoriteRecipeIds().remove(current.getId());
            favoriteButton.setText("☆");
        }

        favoritesService.saveFavorites();
        recipeListView.refresh();

        if (favoritesTab.isSelected()) {
            unsubscribeFromRecipe();
        }
    }

    /**
     * A method for toggling to showing all recipes
     */
    @FXML
    public void showAllRecipes() {
        allRecipesTab.setSelected(true);
        favoritesTab.setSelected(false);
        filterService.setFavoritesSelected(false);
        filterService.updatePredicate();
    }

    /**
     * A method for toggling to just the favorite recipes
     */
    @FXML
    public void showFavoriteRecipes() {
        allRecipesTab.setSelected(false);
        favoritesTab.setSelected(true);
        filterService.setFavoritesSelected(true);
        filterService.updatePredicate();
    }

    /**
     * A constructor for the MainUI Controller class, using dependency injection
     * @param server - the injected instance of the ServerUtils
     * @param mainCtrl - the injected instance of the Main Controller
     * @param webSocketService - the injected instance of the WebSocketService
     * @param favoritesService - the injected instance of the service supporting favorites
     * @param filterService - the injected instance of the service supplying the search
     * @param languageService - the injected instance of the service providing icons
     */
    @Inject
    public MainUICtrl (ServerUtils server, MainCtrl mainCtrl, WebSocketService webSocketService,
                       FavoritesService favoritesService, FilterService filterService,
                       LanguageService languageService) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.webSocketService = webSocketService;
        this.favoritesService = favoritesService;
        this.filterService = filterService;
        this.languageService = languageService;
    }

    /**
     * Initializes the main UI by loading all recipes
     */
    @FXML
    public void initialize() {

        this.currentRecipes = webSocketService.getMasterRecipeList();
        currentRecipes.addListener((ListChangeListener<Recipe>) _ ->
                Platform.runLater(this::updateRecipeListView));

        recipeListView.setItems(filterService.getFilteredList());
        filterService.updatePredicate();

        favoritesService.loadFavorites();
        allRecipesTab.setSelected(true);

        // Sidebar selection updates main UI
        recipeListView.getSelectionModel().selectedItemProperty().
                addListener((_, _, newVal) -> {
                    if (newVal != null) {
                        selectedRecipe = newVal;
                        selectRecipe(newVal);
                        boolean isFav = favoritesService.getFavoriteRecipeIds().
                                contains(newVal.getId());
                        favoriteButton.setSelected(isFav);
                        favoriteButton.setText(isFav ? "★" : "☆");
                        changeRecipeLanguage(selectedRecipe);
                    }else{
                        languageIndicator.setImage(null);
                    }
                });

        refreshButton.setOnAction(_ -> {
            Recipe currentlySelected = recipeListView.getSelectionModel().getSelectedItem();

            initialRecipeFetch();
            cleanupDeletedFavorites();          // check after manual refresh

            if (currentlySelected != null) {
                recipeListView.getSelectionModel().select(currentlySelected);
                selectRecipe(currentlySelected);
            }
        });

        // Bottom sidebar buttons
        addRecipeButton.setOnAction(_ -> openCreateRecipeUI());
        deleteRecipeButton.setOnAction(_ -> handleDeleteRecipe());

        // Placeholders for edit buttons
        editNameButton.setOnAction(_ -> handleEditName());
        editIngredientsButton.setOnAction(_ -> {
            if (selectedRecipe != null) {
                handleEditIngredients(selectedRecipe);
            } else {
                System.out.println("Please select a recipe to edit");
            }
        });

        editInstructionsButton.setOnAction(_ -> {
            if (selectedRecipe != null) {
                handleEditInstructions(selectedRecipe);
            } else {
                System.out.println("Please select a recipe to edit");
            }
        });
        foodItemButton.setOnAction(_ -> handleGoToFoodItems());


        recipeSearchBar.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ESCAPE) {
                recipeSearchBar.clear();
                initialRecipeFetch();
            }
        });

        // Tell the list: "When you have a Recipe, just show its name (and maybe a star!)"
        recipeListView.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(Recipe item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null || item.getName() == null) {
                    setText(null);
                } else {
                    // Check if it's a favorite to add the star prefix
                    boolean isFavorite = favoritesService.getFavoriteRecipeIds().
                            contains(item.getId());
                    String prefix = isFavorite ? "★ " : "";

                    setText(prefix + item.getName());
                }
            }
        });

        addDeleteListener();

        setupLanguageMenu();
        setupFilterLanguages();
        updateLanguageButtonUI(LanguageManager.getLocale());

    }

    /**
     * When a recipe is deleted, the app detects it and warns the user about the favorite
     * being deleted, and also saves all changes
     */
    public void addDeleteListener () {
        currentRecipes.addListener((ListChangeListener<Recipe>) change -> {
            while (change.next()) {
                if (change.wasRemoved()) {
                    for (Recipe removedRecipe : change.getRemoved()) {
                        Long id = removedRecipe.getId();

                        if (favoritesService.getFavoriteRecipeIds().contains(id)) {
                            showFavoriteDeletedWarning();
                            favoritesService.getFavoriteRecipeIds().remove(id);
                            favoritesService.saveFavorites();
                        }
                    }
                }
            }
        });
    }



    /**
     * Loads the recipes initially from the master list using a standard GET request
     */
    public void initialRecipeFetch () {
        // Gets all initial recipes from the server
        List <Recipe> fetched = server.getAllRecipes();

        // The master recipe list is set to match the server ones
        // The initial display of the recipes is also triggered
        Platform.runLater(() -> webSocketService.
                getMasterRecipeList().setAll(fetched));
        if (!currentRecipes.isEmpty()) {
            selectedRecipe = currentRecipes.getFirst();
            recipeListView.getSelectionModel().select(0);
            selectRecipe(currentRecipes.getFirst());
        } else {
            clearRecipeDisplay();
        }
    }

    /**
     * Refreshes the List View
     */
    public void updateRecipeListView () {

        if (favoritesTab.isSelected()) {
            // If we are in Favorites tab, run the filter immediately!
            filterService.setFavoritesSelected(true);
            showFavoriteRecipes();
        } else {
            // Only show all recipes if we are in "All Recipes" tab
            // Updates the sidebar when it senses changes in the database
            filterService.setFavoritesSelected(false);

            // Updates the recipe with all of its parameters
            if (selectedRecipe != null) {
                currentRecipes.stream()
                        .filter(r -> r.getId().equals(selectedRecipe.getId()))
                        .findFirst()
                        .ifPresent(updated -> {
                            this.selectedRecipe = updated;
                            recipeNameLabel.setText(updated.getName());
                        });
            }
        }
    }

    /**
     * Selects a recipe from the ListView
     * @param selectedRecipe - the recipe which was clicked by the user
     */
    public void selectRecipe (Recipe selectedRecipe) {
        displayRecipe(selectedRecipe);

        // If we are currently viewing another recipe, unsubscribe from it
        if (lastSubscribedID == null || !lastSubscribedID.equals(selectedRecipe.getId())) {
            unsubscribeFromRecipe();
        }

        if (webSocketService.getSession() == null) {
            System.out.println("Wait: Session not established yet. Detail subscription skipped.");
            return;
        }

        StompSession.Subscription sub = webSocketService.
                subscribeToRecipe(selectedRecipe.getId(), updated -> Platform.runLater(() -> {
                    displayRecipe(updated);
                    mainCtrl.handleRecipeBeingUpdated(updated);
                    for (int i = 0; i < currentRecipes.size(); i++) {
                        if (currentRecipes.get(i).getId().equals(updated.getId())) {
                            currentRecipes.set(i, updated);
                            break;
                        }
                    }
                }));
        if (sub != null) {
            this.recipeSubscription = sub;
            this.lastSubscribedID = selectedRecipe.getId();
        }
    }

    private void displayRecipe(Recipe recipe) {
        recipeNameLabel.setText(recipe.getName());

        // if the serving is null, then it becomes 1 automatically
        int servings = (recipe.getServings() != null) ? recipe.getServings() : 1;
        servingsLabel.setText(String.valueOf(servings));

        ingredientsVBox.getChildren().clear();
        for (Ingredient ing : recipe.getIngredients()) {
            ingredientsVBox.getChildren().add(new Label(ing.getFoodType().getName() +
                    " - " + ing.getQuantity() + " " + ing.getMeasurement()));
        }

        instructionsVBox.getChildren().clear();
        int stepNum = 1;
        for (String step : recipe.getInstructions()) {
            instructionsVBox.getChildren().add(new Label(stepNum++ + ". " + step));
        }
    }

    private void clearRecipeDisplay() {
        recipeNameLabel.setText("");
        ingredientsVBox.getChildren().clear();
        instructionsVBox.getChildren().clear();
        servingsLabel.setText("");
    }

    private void openCreateRecipeUI() {
        mainCtrl.showCreateRecipe();
    }

    // delete button
    private void handleDeleteRecipe() {
        int selectedIndex = recipeListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex < 0 || selectedIndex >= currentRecipes.size()) return;

        Recipe selectedRecipe = currentRecipes.get(selectedIndex);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(LanguageManager.getString("main.delete.title"));
        alert.setHeaderText(
                java.text.MessageFormat.format(
                        LanguageManager.getString("main.delete.header"),
                        selectedRecipe.getName()
                )
        );
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    long id = selectedRecipe.getId();
                    System.out.println("Deleting recipe with ID: " + id);

                    if (favoritesService.getFavoriteRecipeIds().contains(id)) {
                        showFavoriteDeletedWarning();
                    }

                    mainCtrl.handleRecipeBeingDeleted(id);
                    server.deleteRecipe(id);
                    //Response serverResponse = client.target(recipeEndpointUrl + "/" + id)
                            //.request()
                            //.delete();

                    //if (serverResponse.getStatus() == 204) {
                    unsubscribeFromRecipe();
                    //}
                } catch (Exception e) {
                    System.err.println("Error - could not delete recipe");
                }
            }
        });
    }


    /**
     * Handles the functionality of the Edit Name button
     */
    public void handleEditName() {

        refreshSelectedRecipe();

        Dialog <String> dialogBox = new Dialog<>();
        dialogBox.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Button okButton = (Button) dialogBox.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(false);

        TextField editNameNameField = new TextField(selectedRecipe.getName());
        Label nameLabel = new Label(LanguageManager.getString("main.editName.label"));


        HBox hBox = new HBox();
        hBox.setSpacing(15);
        hBox.setPadding(new Insets(20));
        hBox.getChildren().addAll(nameLabel, editNameNameField);
        dialogBox.getDialogPane().setContent(hBox);

        dialogBox.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                return editNameNameField.getText();
            }
            return null;
        });

        dialogBox.showAndWait().ifPresent(result -> {
            if (selectedRecipe != null && !result.isBlank()) {
                selectedRecipe.setName(result);
                server.updateRecipe(selectedRecipe);
            }
        });
    }

    private void handleEditIngredients(Recipe recipe) {
        if(recipe == null){
            System.out.println("Recipe is null");
            return;
        }
        mainCtrl.showEditIngredients(recipe);
    }

    private void handleEditInstructions(Recipe recipe) {
        if(recipe == null){
            System.out.println("Recipe is null");
            return;
        }
        mainCtrl.showEditInstructions(recipe);
    }

    private void handleGoToFoodItems() {
        mainCtrl.showFoodItemMain();
    }

    /**
     * The Shopping List is connected to the corresponding interface
     */
    @FXML
    private void handleGoToShoppingList() {
        mainCtrl.showShoppingList();
    }

    /**
     * The Add to Shopping List button is connected to the corresponding interface
     */
    @FXML
    private void handleAddRecipeToShoppingList () {
        mainCtrl.showAddRecipeToShoppingList();
    }

    /**
     * A getter for the selected recipe
     * @return - Recipe - returns the currently selected recipe -
     * useful for displaying ingredients, instructions, etc.
     */
    public Recipe getSelectedRecipe () {
        return selectedRecipe;
    }

    /**
     * Downloads a recipe in a user-chosen directory
     */
    @FXML
    public void downloadRecipe() {
        int index = recipeListView.getSelectionModel().getSelectedIndex();
        if (index < 0 || index >= currentRecipes.size()) return;
        Recipe selectedRecipe = currentRecipes.get(index);

        StringBuilder content = new StringBuilder("Recipe: " + selectedRecipe.getName() + "\n\n");

        content.append("Ingredients:\n");
        appendList(content, selectedRecipe.getIngredients(), "");

        content.append("\nInstructions:\n");
        appendList(content, selectedRecipe.getInstructions(), "Step ");

        FileChooser fileChooser = new  FileChooser();
        fileChooser.getExtensionFilters().
                add(new FileChooser.ExtensionFilter("Recipe", "*.txt"));
        //ensures the file is saved as txt

        fileChooser.setInitialFileName((selectedRecipe.getName() + ".txt"));

        Stage stage = (Stage) addRecipeButton.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file!=null) {
            try (FileWriter fw = new FileWriter(file)) {
                fw.write(content.toString());
            } catch (IOException e) {
                System.err.println("Error - could not download recipe");
            }
        }
    }

    /**
     * A helper method for constructing a list
     * @param sb - the initial StringBuilder object which would be appended to
     * @param items - the list of items added to the content
     * @param prefix - the initial String the list starts with
     */
    public void appendList (StringBuilder sb, List<?> items, String prefix) {
        if (items==null) {
            return;
        }
        for (int i = 0; i < items.size(); i++) {
            sb.append(prefix).append(i + 1).append(". ")
                    .append(items.get(i)).append("\n");
        }
    }

    /**
     * A method for creating a clone of the currently selected recipe
     */
    @FXML
    public void cloneRecipe(){
        int index = recipeListView.getSelectionModel().getSelectedIndex();
        if (index < 0 || index >= currentRecipes.size()) return;
        Recipe selectedRecipe = currentRecipes.get(index);

        TextInputDialog dialog = new TextInputDialog(
                LanguageManager.getString("main.clone.defaultPrefix")
                        + " " + selectedRecipe.getName()
        );
        dialog.setTitle(LanguageManager.getString("main.clone.title"));
        dialog.setHeaderText(LanguageManager.getString("main.clone.header"));
        dialog.setContentText(LanguageManager.getString("main.clone.content"));

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String newName = result.get();
            Recipe cloneRecipe = new Recipe(selectedRecipe);
            cloneRecipe.setName(newName);
            server.sendRecipe(cloneRecipe);
        }
    }


    /**
     * A method for accessing the Information of a recipe
     */
    @FXML
    public void handleInformation() {
        if (recipeListView.getSelectionModel().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(LanguageManager.getString("main.info.noSelection.title"));
            alert.setHeaderText(null);
            alert.setContentText(LanguageManager.getString("main.info.noSelection.content"));
            alert.showAndWait();
            return;
        }

        int selectedIndex = recipeListView.getSelectionModel().getSelectedIndex();

        if (selectedIndex < 0 || selectedIndex >= currentRecipes.size()) {
            return;
        }

        Recipe selectedRecipe = currentRecipes.get(selectedIndex);

        if (selectedRecipe == null ||
                selectedRecipe.getName() == null ||
                selectedRecipe.getName().trim().isEmpty()) {

            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(LanguageManager.getString("main.info.invalid.title"));
            alert.setHeaderText(null);
            alert.setContentText(LanguageManager.getString("main.info.invalid.content"));
            alert.showAndWait();
            return;
        }

        mainCtrl.showInformation(selectedRecipe);
    }

    /**
     * A method for refreshing the selected recipe, so the parameter can be safely used
     */
    public void refreshSelectedRecipe() {
        selectedRecipe = recipeListView
                .getSelectionModel()
                .getSelectedItem();
    }

    /**
     * A method for unsubscribing from a recipe every time it is clicked
     */
    public void unsubscribeFromRecipe () {
        if (this.recipeSubscription!= null) {
            recipeSubscription.unsubscribe();
            recipeSubscription = null;
        }
    }

    /**
     * A method which searches for recipe using the search bar
     */
    public void searchForRecipe() {
        String searchQuery = recipeSearchBar.getText();
        filterService.searchForRecipe(searchQuery);
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

        mainCtrl.setSelectedRecipeForReload(getSelectedRecipe());
        updateLanguageButtonUI(newLocale);

        mainCtrl.reloadScenesForNewLanguage();
    }

    /**
     * A method for updating the indicator of the recipe language
     * @param langCode - the name of the language
     */
    public void updateIndicator(String langCode) {
        languageIndicator.setImage(languageService.renderIcon(langCode, 30.0).getImage());
    }

    /**
     * Changes the visible indicator of the recipe language
     * @param selectedRecipe - the recipe which updates the language indicator
     */
    public void changeRecipeLanguage (Recipe selectedRecipe) {
        updateIndicator(selectedRecipe.getLocale().getLanguage());
    }

    /**
     * Filters the selected languages
     */
    public void setupFilterLanguages () {
        filterLanguagesButton.getItems().clear();

        Set<Locale> savedFilters = LanguageManager.loadFilterLocales();
        this.filterService.setFilteredLocales(savedFilters.stream().toList());

        List<String> supportedCodes = List.of("en", "nl", "bg", "ro", "sk", "de");

        for (String code : supportedCodes) {
            String displayName = languageService.getDisplayName(code);
            filterLanguagesButton.getItems().add(setupCheckMenuItemIcons(displayName, code));
        }

        filterService.updatePredicate();
    }

    /**
     * Sets up the icons for the filtering languages
     * @param name - the name of the language (to be put on the button)
     * @param langCode - the language code - will result in a locale filter
     * @return CheckMenuItem - a new CheckMenuItem with the language
     */
    public CheckMenuItem setupCheckMenuItemIcons(String name, String langCode) {
        CheckMenuItem menuItem = new CheckMenuItem(name);
        Locale locale = Locale.of(langCode);

        menuItem.setSelected(filterService.getFilteredLocales().contains(locale));
        menuItem.setGraphic(languageService.renderIcon(langCode, 16.0));

        menuItem.setOnAction(_ -> {
            if (menuItem.isSelected()) {
                filterService.filterLocales(locale);
            } else {
                filterService.getFilteredLocales().remove(locale);
            }
            LanguageManager.saveFilterLocales(filterService.getFilteredLocales());
            filterService.updatePredicate();
        });

        return menuItem;
    }
}
