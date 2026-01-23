package client.configuration;

import shopping.ShoppingList;

import java.util.ArrayList;
import java.util.List;


public class Config {
    private String language = "en";
    private List <String> filteredLocales = new ArrayList<>();
    private String serverUrl = "http://localhost:8080/";
    private List<Long> favoriteRecipeIds = new ArrayList<>();
    private ShoppingList shoppingList = new ShoppingList();

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public ShoppingList getShoppingList() {
        return shoppingList;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public List<Long> getFavoriteRecipeIds() {
        return favoriteRecipeIds;
    }

    public void setFavoriteRecipeIds(List<Long> recipeIds) {
        this.favoriteRecipeIds = recipeIds;
    }

    public List<String> getFilteredLocales() {
        return filteredLocales;
    }

    public void setFilteredLocales(List<String> filteredLocales) {
        this.filteredLocales = filteredLocales;
    }

    public void setShoppingList(ShoppingList shoppingList) {
        this.shoppingList = shoppingList;
    }
}
