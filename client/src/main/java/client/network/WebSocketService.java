package client.network;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import recipes.FoodItem;
import recipes.Recipe;

import java.lang.reflect.Type;
import java.util.List;

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
        });
    }

    /**
     * Subscribes the client to listen to the global channels - for the recipes and FoodItems
     */
    public void subscribeToGlobalTopics () {
        session.subscribe("/topic/food_items", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return FoodItem.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                FoodItem item = (FoodItem) payload;
                Platform.runLater(() -> syncItemInList(item));
            }
        });
        session.subscribe("/topic/recipes", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Recipe.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                Recipe recipe = (Recipe) payload;
                Platform.runLater(() -> syncRecipeInList(recipe));
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
    }

    public void syncItemInList (FoodItem foodItem) {
        masterFoodItemsList.removeIf(x -> x.getId().equals(foodItem.getId()));
        masterFoodItemsList.add(foodItem);
    }

    public void syncRecipeInList (Recipe recipe) {
        masterRecipeList.removeIf(x -> x.getId().equals(recipe.getId()));
        masterRecipeList.add(recipe);
    }

    public void removeItemFromList (Long id) {
        masterFoodItemsList.removeIf(x -> x.getId().equals(id));
    }

    public void removeRecipeFromList (Long id) {
        masterRecipeList.removeIf(x -> x.getId().equals(id));
    }
}
