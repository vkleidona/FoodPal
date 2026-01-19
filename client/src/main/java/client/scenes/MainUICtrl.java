package client.scenes;

import client.network.WebSocketService;
import client.utils.ServerUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import javafx.application.Platform;
import client.i18n.LanguageManager;

import java.util.*;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.messaging.simp.stomp.StompSession;
import recipes.Recipe;
import recipes.Ingredient;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;

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

    private final Client client = ClientBuilder.newClient();
    private final String recipeEndpointUrl = "http://localhost:8080/api/recipes";

    private ObservableList<Recipe> currentRecipes;
    private FilteredList<Recipe> filteredRecipesByLanguage;
    private final List <Long> searchedRecipeIds = new ArrayList<>();
    private Recipe selectedRecipe;

    private final ServerUtils server;
    private final MainCtrl mainCtrl;
    private final ServerService serverService;
    private final WebSocketService webSocketService;

    private Set<Long> favoriteRecipeIds = new HashSet<>();
    private List <Locale> filteredLocales = new ArrayList<>();

    private Long lastSubscribedID = null;
    private StompSession.Subscription recipeSubscription;

    @FXML
    private MenuButton languageButton;


    // File path for saving favorites
    private static final File FAVORITES_FILE = new File("favorites.json");

    /**
     * Cleans up the local favorites list by removing recipe IDs that no longer
     * exist in the current master recipe list received from the server.
     * This method detects favorites that were deleted by someone else on the
     * server, removes those IDs from {@code favoriteRecipeIds}, updates the
     * persisted favorites file, refreshes the favorites filter, and shows a
     * warning alert to inform the user that some favorite recipes are gone.
     */
    private void cleanupDeletedFavorites() {
        Set<Long> stillFavoriteIds = new HashSet<>();
        boolean anyDeleted = false;

        for (Long favId : favoriteRecipeIds) {
            // Check if this ID still exists in currentRecipes (master list from WebSocket)
            boolean existsInList = currentRecipes.stream()
                    .anyMatch(r -> Objects.equals(r.getId(), favId));

            if (existsInList) {
                stillFavoriteIds.add(favId);
            } else {
                anyDeleted = true;
            }
        }

        if (anyDeleted) {
            favoriteRecipeIds = stillFavoriteIds;
            saveFavorites();
            updatePredicate();  // so favorites tab hides dead ones
            showFavoriteDeletedWarning();
        }
    }

    private void showFavoriteDeletedWarning() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(LanguageManager.getString("main.favoriteDeleted.title"));
        alert.setHeaderText(LanguageManager.getString("main.favoriteDeleted.header"));
        alert.setContentText(LanguageManager.getString("main.favoriteDeleted.content"));
        alert.showAndWait();
    }



    /**
     * reads a list of favorite recipe IDs from a local file (favorites.json)
     * and loads them into the application's memory.
     */
    private void loadFavorites() {
        if (!FAVORITES_FILE.exists()) return;

        // Creates a new Jackson ObjectMapper instance, the tool used to
        // convert between Java objects and JSON text
        ObjectMapper mapper = new ObjectMapper();
        try {
            // Reads the file content and converts the JSON data into a Set of Long integers.
            // The TypeReference is required because Set<Long> is a generic type,
            // and Java needs this extra help to understand exactly
            // what type of collection to create.
            favoriteRecipeIds = mapper.readValue(FAVORITES_FILE, new TypeReference<>() {
            });
        } catch (IOException e) {
            System.err.println("Error - favorites file not found - aborting load");
        }
    }

    /**
     *  takes the current set of favorite recipe IDs from memory and
     *  writes them into the favorites.json file.
     */
    private void saveFavorites() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(FAVORITES_FILE, favoriteRecipeIds);
        } catch (IOException e) {
            System.err.println("Error - favorites file could not be found - aborting save");
        }
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
            favoriteRecipeIds.add(current.getId());
            favoriteButton.setText("★");
        } else {
            favoriteRecipeIds.remove(current.getId());
            favoriteButton.setText("☆");
        }

        saveFavorites();
        updatePredicate();

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
        updatePredicate();
    }

    /**
     * A method for toggling to just the favorite recipes
     */
    @FXML
    public void showFavoriteRecipes() {
        allRecipesTab.setSelected(false);
        favoritesTab.setSelected(true);
        updatePredicate();
    }

    /**
     * A constructor for the MainUI Controller class, using dependency injection
     * @param server - the injected instance of the ServerUtils
     * @param mainCtrl - the injected instance of the Main Controller
     * @param webSocketService - the injected instance of the WebSocketService
     */
    @Inject
    public MainUICtrl (ServerUtils server, MainCtrl mainCtrl, WebSocketService webSocketService) {
        this.server = server;
        this.mainCtrl = mainCtrl;
        this.webSocketService = webSocketService;
        this.serverService = new RealServerService();
    }

    /**
     * Initializes the main UI by loading all recipes
     */
    @FXML
    public void initialize() {

        this.currentRecipes = webSocketService.getMasterRecipeList();
        currentRecipes.addListener((ListChangeListener<Recipe>) _ ->
                Platform.runLater(this::updateRecipeListView));

        filteredRecipesByLanguage = new FilteredList<>(currentRecipes);
        recipeListView.setItems(filteredRecipesByLanguage);
        updatePredicate();

        loadFavorites();
        allRecipesTab.setSelected(true);

        // Sidebar selection updates main UI
        recipeListView.getSelectionModel().selectedItemProperty().
                addListener((_, _, newVal) -> {
                    if (newVal != null) {
                        selectedRecipe = newVal;
                        selectRecipe(newVal);
                        boolean isFav = favoriteRecipeIds.contains(newVal.getId());
                        favoriteButton.setSelected(isFav);
                        favoriteButton.setText(isFav ? "★" : "☆");
                        changeRecipeLanguage(selectedRecipe);
                    }else{
                        languageIndicator.setImage(null);
                    }
                });

        refreshButton.setOnAction(_ -> {
            initialRecipeFetch();
            cleanupDeletedFavorites();          // check after manual refresh
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
                    boolean isFavorite = favoriteRecipeIds.contains(item.getId());
                    String prefix = isFavorite ? "★ " : "";

                    setText(prefix + item.getName());
                }
            }
        });

        setupLanguageMenu();
        setupFilterLanguages();

    }

    /**
     * A method which sets up the icons for the language menu
     * @param name - the name of the language we are adding
     * @param langCode - the locale code of the language
     * @param iconFileName - the encoding for the name of the icon file
     * @return MenuItem - returns the ready-made MenuItem with the proper icon
     */
    public MenuItem setupMenuItemIcons (String name, String langCode, String iconFileName) {
        MenuItem menuItem = new MenuItem(name);

        Image flagImage = new Image(Objects.requireNonNull
                (getClass().getResourceAsStream("/flags/" + iconFileName + "-flag.jpg")));

        ImageView icon = new ImageView(flagImage);
        icon.setFitWidth(16);
        icon.setFitHeight(16);

        Circle clip = new Circle(8, 8, 8);
        icon.setClip(clip);

        menuItem.setGraphic(icon);

        menuItem.setOnAction(_ -> changeLanguage(new Locale(langCode)));

        return menuItem;
    }

    /**
     * A method which updates the flag icon of the language button when necessary
     * @param iconFileName - the encoding for the name of the icon file
     */
    public void updateButtonGraphic(String iconFileName) {
        Image img = new Image (Objects.requireNonNull
                (getClass().getResourceAsStream("/flags/" + iconFileName + "-flag.jpg")));
        ImageView imageView = new ImageView(img);

        imageView.setFitWidth(16);
        imageView.setFitHeight(16);

        Circle clip = new Circle(8, 8, 8);
        imageView.setClip(clip);

        // Set the graphic to the MenuButton itself
        languageButton.setGraphic(imageView);
    }

    /**
     * A setup method for the language menu -
     * adds the constructed MenuItems using the helper methods with the appropriate icon
     */
    public void setupLanguageMenu() {
        languageButton.getItems().clear();

        MenuItem enItem = setupMenuItemIcons("English", "en", "english");
        MenuItem nlItem = setupMenuItemIcons("Nederlands", "nl", "dutch");
        MenuItem bgItem = setupMenuItemIcons("Български", "bg", "bulgarian");
        MenuItem roItem = setupMenuItemIcons("Română", "ro", "romanian");
        MenuItem skItem = setupMenuItemIcons("Slovenčina", "sk", "slovak");
        MenuItem deItem = setupMenuItemIcons("Deutsch", "de", "german");

        languageButton.getItems().addAll(enItem, nlItem, bgItem, roItem, skItem, deItem);

        Locale current = LanguageManager.getLocale();

        switch (current.getLanguage()) {
            case "nl" -> {
                languageButton.setText("Nederlands");
                updateButtonGraphic("dutch");
            }
            case "ro" -> {
                languageButton.setText("Română");
                updateButtonGraphic("romanian");
            }
            case "bg" -> {
                languageButton.setText("Български");
                updateButtonGraphic("bulgarian");
            }
            case "sk" -> {
                languageButton.setText("Slovenčina");
                updateButtonGraphic("slovak");
            }
            case "de" -> {
                languageButton.setText("Deutsch");
                updateButtonGraphic("german");
            }
            default -> {
                languageButton.setText("English");
                updateButtonGraphic("english");
            }
        }
    }

    /**
     * A method for changing the language of the menus to a new Locale
     * @param newLocale - the new language we want to set the menus to
     */
    public void changeLanguage(Locale newLocale) {
        LanguageManager.setLocale(newLocale);
        switch (newLocale.getLanguage()) {
            case "nl" -> languageButton.setText("Nederlands");
            case "ro" -> languageButton.setText("Română");
            case "bg" -> languageButton.setText("Български");
            case "sk" -> languageButton.setText("Slovenčina");
            case "de" -> languageButton.setText("Deutsch");
            default -> languageButton.setText("English");
        }
        mainCtrl.reloadScenesForNewLanguage();
    }

    /**
     * A method for updating the indicator of the recipe language
     * @param iconFileName - the name of the flag icon
     */
    public void updateIndicator(String iconFileName) {
        Image img = new Image (Objects.requireNonNull
                (getClass().getResourceAsStream("/flags/" + iconFileName + "-flag.jpg")));

        languageIndicator.setImage(img);

        languageIndicator.setFitHeight(30);
        languageIndicator.setFitWidth(30);

        Circle clip = new Circle(15, 15, 15);
        languageIndicator.setClip(clip);
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
            showFavoriteRecipes();
        } else {
            // Only show all recipes if we are in "All Recipes" tab
            // Updates the sidebar when it senses changes in the database
            updatePredicate();

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
                    mainCtrl.handleRecipeBeingDeleted(id);
                    Response serverResponse = client.target(recipeEndpointUrl + "/" + id)
                            .request()
                            .delete();

                    if (serverResponse.getStatus() == 204) {
                        unsubscribeFromRecipe();
                    }
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

        int indexClicked = currentRecipes.indexOf(selectedRecipe);

        dialogBox.showAndWait().ifPresent(result -> {
            if (indexClicked >= 0) {
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
                LanguageManager.getString("main.clone.defaultPrefix") + " " + selectedRecipe.getName()
        );
        dialog.setTitle(LanguageManager.getString("main.clone.title"));
        dialog.setHeaderText(LanguageManager.getString("main.clone.header"));
        dialog.setContentText(LanguageManager.getString("main.clone.content"));

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String newName = result.get();
            Recipe cloneRecipe = new Recipe(selectedRecipe);
            cloneRecipe.setName(newName);
            serverService.sendRecipe(cloneRecipe);
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
        int selectedIndex = recipeListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < currentRecipes.size()) {
            selectedRecipe = currentRecipes.get(selectedIndex);
        }
    }

    /**
     * A method which searches for recipe using the search bar
     */
    public void searchForRecipe() {
        String searchQuery = recipeSearchBar.getText().trim();

        if (searchQuery.isEmpty()) {
            searchedRecipeIds.clear();
            updatePredicate();
            return;
        }

        String[] recipeTerms = searchQuery.split(",");
        List<String> trimmedRecipeTerms = new ArrayList<>();
        for(String term : recipeTerms){
            String editedTerm = term.trim();
            if (!editedTerm.isEmpty()){
                trimmedRecipeTerms.add(editedTerm);
            }
        }

        boolean firstItem = true;
        for (String term : trimmedRecipeTerms) {
            List<Recipe> termResults = server.searchRecipesByTerm(term);
            if (firstItem) {
                searchedRecipeIds.clear();
                searchedRecipeIds.addAll(termResults.stream().map(Recipe::getId).toList());
                firstItem = false;
            }else{
                searchedRecipeIds.retainAll(termResults.stream().map(Recipe::getId).toList());
            }
        }

        updatePredicate();
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
     * Changes the visible indicator of the recipe language
     * @param selectedRecipe - the recipe which updates the language indicator
     */
    public void changeRecipeLanguage (Recipe selectedRecipe) {
        switch(selectedRecipe.getLocale().getLanguage()) {
            case "bg" -> updateIndicator("bulgarian");
            case "nl" -> updateIndicator("dutch");
            case "ro" -> updateIndicator("romanian");
            case "sk" -> updateIndicator("slovak");
            default -> updateIndicator("english");
        }
    }

    /**
     * Filters the selected languages
     */
    public void setupFilterLanguages () {
        filterLanguagesButton.getItems().clear();

        Set<Locale> savedFilters = LanguageManager.loadFilterLocales();
        this.filteredLocales = new ArrayList<>(savedFilters);

        CheckMenuItem enItem = setupCheckMenuItemIcons("English", "en", "english");
        CheckMenuItem nlItem = setupCheckMenuItemIcons("Nederlands", "nl", "dutch");
        CheckMenuItem roItem = setupCheckMenuItemIcons("Română", "ro", "romanian");
        CheckMenuItem bgItem = setupCheckMenuItemIcons("Български", "bg", "bulgarian");
        CheckMenuItem skItem = setupCheckMenuItemIcons("Slovenčina", "sk", "slovak");
        CheckMenuItem deItem = setupCheckMenuItemIcons("Deutsch", "de", "german");

        filterLanguagesButton.getItems().addAll(enItem, nlItem, bgItem, roItem, skItem, deItem);
        updatePredicate();
    }

    /**
     * Sets up the icons for the filtering languages
     * @param name - the name of the language (to be put on the button)
     * @param langCode - the language code - will result in a locale filter
     * @param iconFileName - the name of the file containing the country's flag
     * @return CheckMenuItem - a new CheckMenuItem with the language
     */
    public CheckMenuItem setupCheckMenuItemIcons(String name,
                                                  String langCode, String iconFileName) {
        CheckMenuItem menuItem = new CheckMenuItem(name);

        menuItem.setSelected(filteredLocales.contains(new Locale(langCode)));

        Image flagImage = new Image(Objects.requireNonNull
                (getClass().getResourceAsStream("/flags/" + iconFileName + "-flag.jpg")));

        ImageView icon = new ImageView(flagImage);
        icon.setFitWidth(16);
        icon.setFitHeight(16);

        Circle clip = new Circle(8, 8, 8);
        icon.setClip(clip);

        menuItem.setGraphic(icon);

        Locale tempLocale = new Locale (langCode);

        menuItem.setOnAction(_ -> {
            if (menuItem.isSelected()) {
                filterLocales(tempLocale);
            }else{
                filteredLocales.remove(tempLocale);
            }
            LanguageManager.saveFilterLocales(filteredLocales);
            updatePredicate();
        });

        return menuItem;
    }

    /**
     * A method which handles the filtering of languages
     * @param locale - the locale we want to filter
     */
    public void filterLocales (Locale locale) {
        if (!filteredLocales.contains(locale)) {
            filteredLocales.add(locale);
        }
    }

    /**
     * Updates the predicate of the filtered list
     */
    public void updatePredicate () {
        filteredRecipesByLanguage.setPredicate(recipe -> {
            boolean matchesLanguage = filteredLocales.isEmpty() ||
                    filteredLocales.contains(recipe.getLocale());
            boolean matchesTab = true;
            if (favoritesTab.isSelected()) {
                matchesTab = favoriteRecipeIds.contains(recipe.getId());
            }
            boolean matchesSearch = searchedRecipeIds.isEmpty() ||
                    searchedRecipeIds.contains(recipe.getId());

            return matchesLanguage && matchesTab && matchesSearch;
        });
    }
}
