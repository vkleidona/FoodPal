package client.service;

import client.configuration.ConfigManager;
import jakarta.inject.Inject;
import org.springframework.stereotype.Service;
import recipes.Recipe;

import java.util.*;

@Service
public class FavoritesService {

    private final WebSocketService webSocketService;

    /**
     * A constructor for the Favorites service using WebSockets
     * @param webSocketService - a service handling Web Sockets
     */
    @Inject
    public FavoritesService (WebSocketService webSocketService) {
        this.webSocketService = webSocketService;
    }

    public List <Long> getFavorites() {
        return ConfigManager.getConfig().getFavoriteRecipeIds();
    }


    /**
     * Cleans up the local favorites list by removing recipe IDs that no longer
     * exist in the current master recipe list received from the server.
     * This method detects favorites that were deleted by someone else on the
     * server, removes those IDs from {@code favoriteRecipeIds}, updates the
     * persisted favorites file, refreshes the favorites filter, and shows a
     * warning alert to inform the user that some favorite recipes are gone.
     * @return boolean - true if deleted, false if not
     */
    public boolean cleanupDeletedFavorites() {
        List<Recipe> recipesFromMaster = webSocketService.getMasterRecipeList();

        List<Long> stillFavoriteIds = new ArrayList<>();
        boolean anyDeleted = false;

        for (Long favId : getFavorites()) {
            // Check if this ID still exists in currentRecipes (master list from WebSocket)
            boolean existsInList = recipesFromMaster.stream()
                    .anyMatch(r -> Objects.equals(r.getId(), favId));

            if (existsInList) {
                stillFavoriteIds.add(favId);
            } else {
                anyDeleted = true;
            }
        }

        if (anyDeleted) {
            ConfigManager.getConfig().setFavoriteRecipeIds(stillFavoriteIds);
            ConfigManager.save();
            return true;  // so favorites tab hides dead ones
        }
        return false;
    }

    /**
     * A method which adds the recipe to the list of favorites
     * @param id - the favorite recipe to be added
     */
    public void addFavorite (Long id) {
        List <Long> favorites = ConfigManager.getConfig().getFavoriteRecipeIds();
        if (!favorites.contains(id)) {
            favorites.add(id);
            ConfigManager.save();
        }
    }

    /**
     * A method which removes a given recipe from the favorites list
     * @param id - the recipe to be removed from the favorites
     */
    public void removeFavorite (Long id) {
        List<Long> favorites = ConfigManager.getConfig().getFavoriteRecipeIds();
        if (favorites.remove(id)) {
            ConfigManager.save();
        }
    }
}
