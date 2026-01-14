package server.api.controllerTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import recipes.FoodItem;
import server.api.controllers.IngredientController;
import server.repositories.FoodItemRepository;
import server.repositories.IngredientRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IngredientControllerTest {

    private IngredientController ctrl;
    private IngredientRepository ingredientRepo;
    private FoodItemRepository foodItemRepo;

    @BeforeEach
    public void setup(){
        ingredientRepo = mock(IngredientRepository.class);
        foodItemRepo = mock(FoodItemRepository.class);
        ctrl = new IngredientController(ingredientRepo, foodItemRepo);
    }

    @Test
    public void getFoodItemRecipeUsageTest(){
        FoodItem testItem = new FoodItem();
        testItem.setId(1L);

        when(foodItemRepo.findById(1L)).thenReturn(Optional.of(testItem));
        when(ingredientRepo.countFoodItemRecipeUsages(testItem)).thenReturn(5);

        int result = ctrl.getFoodItemRecipeUsage(1L);
        assertEquals(5, result);
    }

}
