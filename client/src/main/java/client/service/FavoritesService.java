package client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import org.springframework.stereotype.Service;
import recipes.Recipe;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class FavoritesService {

    // File path for saving favorites
    private static final File FAVORITES_FILE = new File("favorites.json");

    private Set<Long> favoriteRecipeIds = new HashSet<>();
    private final WebSocketService webSocketService;

    /**
     * A constructor for the Favorites service using WebSockets
     * @param webSocketService - a service handling Web Sockets
     */
    @Inject
    public FavoritesService (WebSocketService webSocketService) {
        this.webSocketService = webSocketService;
    }

    public Set<Long> getFavoriteRecipeIds() {
        return favoriteRecipeIds;
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

        Set<Long> stillFavoriteIds = new HashSet<>();
        boolean anyDeleted = false;

        for (Long favId : favoriteRecipeIds) {
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
            favoriteRecipeIds = stillFavoriteIds;
            saveFavorites();
            return true;  // so favorites tab hides dead ones
        }
        return false;
    }


    /**
     * reads a list of favorite recipe IDs from a local file (favorites.json)
     * and loads them into the application's memory.
     */
    public void loadFavorites() {
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
    public void saveFavorites() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(FAVORITES_FILE, favoriteRecipeIds);
        } catch (IOException e) {
            System.err.println("Error - favorites file could not be found - aborting save");
        }
    }
}
