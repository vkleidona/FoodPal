package client.service;

import client.network.WebSocketService;
import client.utils.ServerUtils;
import jakarta.inject.Inject;
import javafx.collections.transformation.FilteredList;
import org.springframework.stereotype.Service;
import recipes.Recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class FilterService {

    private List<Locale> filteredLocales = new ArrayList<>();
    private final FilteredList<Recipe> filteredRecipes;
    private final List <Long> searchedRecipeIds = new ArrayList<>();
    private final FavoritesService favoritesService;
    private final ServerUtils server;

    private boolean favoritesSelected = false;

    /**
     * A dependency-injection using constructor for the Filtering Service
     * @param favoritesService - provides the favorites
     * @param webSocketService - provides the web socket status
     * @param serverUtils - provides the server utilities
     */
    @Inject
    public FilterService (FavoritesService favoritesService, WebSocketService webSocketService,
                          ServerUtils serverUtils) {
        this.favoritesService = favoritesService;
        this.server = serverUtils;
        filteredRecipes = new FilteredList<>(webSocketService.getMasterRecipeList());
    }

    public FilteredList<Recipe> getFilteredList() {
        return filteredRecipes;
    }

    public List<Locale> getFilteredLocales() {
        return filteredLocales;
    }

    public void setFilteredLocales(List<Locale> filteredLocales) {
        this.filteredLocales = new ArrayList<>(filteredLocales);
    }

    public void setFavoritesSelected(boolean selected) {
        this.favoritesSelected = selected;
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
        filteredRecipes.setPredicate(recipe -> {
            boolean matchesLanguage = filteredLocales.isEmpty() ||
                    filteredLocales.contains(recipe.getLocale());
            boolean matchesTab = true;
            if (favoritesSelected) {
                matchesTab = favoritesService.getFavoriteRecipeIds().contains(recipe.getId());
            }
            boolean matchesSearch = searchedRecipeIds.isEmpty() ||
                    searchedRecipeIds.contains(recipe.getId());

            return matchesLanguage && matchesTab && matchesSearch;
        });
    }

    /**
     * A method which searches for recipe using the search bar
     * @param searchQuery - what we want to search for
     */
    public void searchForRecipe(String searchQuery) {

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
}
