package client;

import client.service.WebSocketService;
import client.scenes.food_item.FoodItemMainCtrl;
import client.scenes.main.CreateRecipeCtrl;
import client.scenes.main.EditIngredientsCtrl;
import client.scenes.main.EditInstructionsCtrl;
import client.scenes.main.MainUICtrl;
import client.scenes.nutrition.InformationCtrl;
import client.scenes.shopping_list.AddRecipeToShoppingListCtrl;
import client.scenes.shopping_list.ShoppingListMainCtrl;
import client.service.FavoritesService;
import client.service.FilterService;
import client.utils.MainCtrl;
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
        binder.bind(FavoritesService.class).in(Scopes.SINGLETON);
        binder.bind(FilterService.class).in(Scopes.SINGLETON);
    }
}