package server.service;

import exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import recipes.FoodItem;
import server.api.controllers.IngredientController;
import server.repositories.FoodItemRepository;
import server.repositories.IngredientRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IngredientServiceTest {
    private IngredientRepository ingredientRepo;
    private FoodItemRepository foodItemRepo;
    private IngredientService service;

    @BeforeEach
    public void setup(){
        ingredientRepo = mock(IngredientRepository.class);
        foodItemRepo = mock(FoodItemRepository.class);
        service = new IngredientService(ingredientRepo, foodItemRepo);
    }

    @Test
    public void getFoodItemRecipeUsageTest(){
        FoodItem testItem = new FoodItem();
        testItem.setId(1L);

        when(foodItemRepo.findById(1L)).thenReturn(Optional.of(testItem));
        when(ingredientRepo.countFoodItemRecipeUsages(testItem)).thenReturn(5);

        int usage = service.getFoodItemRecipeUsage(testItem.getId());
        assertEquals(5, usage);
    }

    @Test
    public void getFoodItemRecipeUsageUnsuccessfulTest(){
        FoodItem testItem = new FoodItem();
        testItem.setId(1L);

        when(foodItemRepo.findById(1L)).thenThrow(new ResourceNotFoundException());
        assertThrows(ResourceNotFoundException.class, () -> {
            service.getFoodItemRecipeUsage(testItem.getId());
        });
    }

}
