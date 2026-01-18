package client.scenes;

import com.google.inject.Inject;
import com.google.inject.Provider; // Import Provider
import com.google.inject.Singleton;

/**
 * @param mainCtrlProvider - a provider of the MainCtrl class
 */
@Singleton
public record RecipeActions(Provider<MainCtrl> mainCtrlProvider) {

    /**
     * A constructor for RecipeActions using Dependency Injection
     * @param mainCtrlProvider - the provider of the mainCtrl class
     */
    @Inject
    public RecipeActions {
    }

    /**
     * Handles returning to the Main UI
     */
    public void goToMainUI() {
        mainCtrlProvider.get().showMainMenu(); // Call .get() to access the instance
    }
}
