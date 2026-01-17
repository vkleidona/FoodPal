package client;

import client.network.WebSocketService;
import client.scenes.*;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;
import shopping.ShoppingList;

public class MyModule implements Module {

    /**
     * Binds all controller classes and the shopping list as singletons
     * @param binder - the binder which makes the classes singletons
     */
    @Override
    public void configure(Binder binder) {
        binder.bind(MainCtrl.class).in(Scopes.SINGLETON);
        binder.bind(WebSocketService.class).in(Scopes.SINGLETON);
        binder.bind(MainUICtrl.class).in(Scopes.SINGLETON);
        binder.bind(CreateRecipeCtrl.class).in(Scopes.SINGLETON);
        binder.bind(EditIngredientsCtrl.class).in(Scopes.SINGLETON);
        binder.bind(EditInstructionsCtrl.class).in(Scopes.SINGLETON);
        binder.bind(FoodItemMainCtrl.class).in(Scopes.SINGLETON);
        binder.bind(InformationCtrl.class).in(Scopes.SINGLETON);
        binder.bind(ShoppingListMainCtrl.class).in(Scopes.SINGLETON);
        binder.bind(AddRecipeToShoppingListCtrl.class).in(Scopes.SINGLETON);
        binder.bind(ShoppingList.class).in(Scopes.SINGLETON);
    }
}