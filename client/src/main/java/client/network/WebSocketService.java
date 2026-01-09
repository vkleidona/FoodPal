package client.network;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import commons.MiniUpdate;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import recipes.FoodItem;
import recipes.Recipe;

import java.lang.reflect.Type;
import java.util.function.Consumer;

public class WebSocketService {
    private static WebSocketService instance;
    private StompSession session;
    private final String url = "ws://localhost:8080/ws-updates";

    // The Lists with the current data - every other list needs to be fetched
    private final ObservableList<Recipe> masterRecipeList = FXCollections.observableArrayList();
    private final ObservableList<FoodItem> masterFoodItemsList = FXCollections.observableArrayList();

    /**
     * Constructor for the WebSocketService class
     * using dependency injection
     */
    private WebSocketService () {

    }

    public static WebSocketService getInstance() {
        if (instance == null) {
            instance = new WebSocketService();
        }
        return instance;
    }

    public StompSession getSession() {
        return session;
    }

    public ObservableList<Recipe> getMasterRecipeList() {
        return masterRecipeList;
    }

    public ObservableList<FoodItem> getMasterFoodItemsList() {
        return masterFoodItemsList;
    }

    /**
     * Initial connection of every client to the server
     */
    public void connect () {
        WebSocketClient client = new StandardWebSocketClient();
        WebSocketStompClient stompClient = new WebSocketStompClient(client);

        // For converting objects to JSON
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        stompClient.connectAsync(url, new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                WebSocketService.this.session = session;
                System.out.println("Global connection established.");

                subscribeToGlobalTopics();
            }

            @Override
            public void handleTransportError(StompSession session, Throwable exception) {
                System.err.println("!!! TRANSPORT ERROR !!! The connection was lost.");
                exception.printStackTrace();
                // In a real app, you would call connect() again here to auto-reconnect
            }

            @Override
            public void handleException(StompSession session, StompCommand command,
                                        StompHeaders headers, byte[] payload, Throwable exception) {
                System.err.println("!!! STOMP EXCEPTION !!! Error in message processing.");
                exception.printStackTrace();
            }
        });
    }

    /**
     * Subscribes the client to listen to the global channels - for the recipes and FoodItems
     */
    public void subscribeToGlobalTopics () {
        subscribeToRecipesGlobal();
        subscribeToFoodItemsGlobal();
    }

    /**
     * Subscribes to the global updates to the list
     */
    public void subscribeToRecipesGlobal () {
        session.subscribe("/topic/recipes", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return MiniUpdate.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                MiniUpdate miniUpdate = (MiniUpdate) payload;
                Platform.runLater(() -> syncRecipeNameInList(miniUpdate));
            }
        });

        session.subscribe("/topic/recipes/delete", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Long.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                Long recipeID = (Long) payload;
                Platform.runLater(() -> removeRecipeFromList(recipeID));
            }
        });

        session.subscribe("/topic/recipes/new", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Recipe.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                Recipe recipe = (Recipe) payload;
                Platform.runLater(() -> masterRecipeList.add(recipe));
            }
        });
    }

    /**
     * Creates global subscriptions to the list of FoodItems
     */
    public void subscribeToFoodItemsGlobal () {
        session.subscribe("/topic/food_items", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return MiniUpdate.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                MiniUpdate miniUpdate = (MiniUpdate) payload;
                Platform.runLater(() -> syncFoodItemNameInList(miniUpdate));
            }
        });
        session.subscribe("/topic/food_items/delete", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Long.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                Long id = (Long) payload;
                Platform.runLater(() -> removeItemFromList(id));
            }
        });
        session.subscribe("/topic/food_items/new", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return FoodItem.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                FoodItem foodItem = (FoodItem) payload;
                Platform.runLater(() -> masterFoodItemsList.add(foodItem));
            }
        });
    }


    /**
     * Synchronizes the FoodItem in the master list by replacing its name
     * @param update - the updated version of the FoodItem
     */
    public void syncFoodItemNameInList (MiniUpdate update) {
        for (int i = 0; i < masterFoodItemsList.size(); i++) {
            FoodItem f = masterFoodItemsList.get(i);
            if (f.getId().equals(update.getId())) {
                f.setName(update.getTitle());
                masterFoodItemsList.set(i, f);
                return;
            }
        }
    }

    /**
     * Synchronizes the recipe in the master list by replacing its name
     * @param update - the updated version of the recipe
     */
    public void syncRecipeNameInList (MiniUpdate update) {
        for (int i = 0; i < masterRecipeList.size(); i++) {
            Recipe r = masterRecipeList.get(i);
            if (r.getId().equals(update.getId())) {
                r.setName(update.getTitle());
                masterRecipeList.set(i, r);
                return;
            }
        }
    }

    /**
     * Removes a food item from the master list
     * @param id - the item to be removed from the list
     */
    public void removeItemFromList (Long id) {
        Platform.runLater(() -> {
            masterFoodItemsList.removeIf(x -> x.getId().equals(id));
        });
    }

    /**
     * Remove a recipe from the master list
     * @param id - the id of the recipe to be removed from the list
     */
    public void removeRecipeFromList (Long id) {
        Platform.runLater(() -> {
            masterRecipeList.removeIf(x -> x.getId().equals(id));
        });
    }

    /**
     * A method for subscribing to changes in a particular recipe
     * @param id - the ID of the recipe subscription
     * @param onUpdate - what to do on update
     * @return Subscription - the newly created Subscription object
     */
    public StompSession.Subscription subscribeToRecipe (Long id, Consumer<Recipe> onUpdate) {
        return session.subscribe("/topic/recipes/" + id, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Recipe.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                Recipe recipe = (Recipe) payload;
                Platform.runLater(() -> onUpdate.accept(recipe));
            }
        });
    }

}
