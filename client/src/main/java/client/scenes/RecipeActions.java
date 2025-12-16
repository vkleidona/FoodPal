package client.scenes;

import client.utils.ServerUtils;
import com.google.inject.Inject;
import com.google.inject.Provider; // Import Provider
import com.google.inject.Singleton;
import recipes.Recipe;

@Singleton
public class RecipeActions {

    private final ServerUtils server;
    private final Provider<MainCtrl> mainCtrlProvider; // Use Provider here

    @Inject
    public RecipeActions(ServerUtils server, Provider<MainCtrl> mainCtrlProvider) {
        this.server = server;
        this.mainCtrlProvider = mainCtrlProvider;
    }

    public void goToMainUI() {
        mainCtrlProvider.get().showMainMenu(); // Call .get() to access the instance
    }
}
