package server.api.controllerTests;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import recipes.FoodItem;
import server.api.controllers.IngredientController;
import server.service.IngredientService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IngredientControllerTest {

    private IngredientController ctrl;
    private IngredientService service;

    @BeforeEach
    public void setup(){
        service = mock(IngredientService.class);
        ctrl = new IngredientController(service);
    }

    @Test
    public void getFoodItemRecipeUsageTest(){
        FoodItem testItem = new FoodItem();
        testItem.setId(1L);

        when(service.getFoodItemRecipeUsage(testItem.getId())).thenReturn(5);
        ResponseEntity<Integer> response = ctrl.issueGetFoodItemRecipeUsage(1L);

        assertFalse(response.getStatusCode().isError());
        assertNotNull(response.getBody());
        int result = response.getBody();
        assertEquals(5, result);
    }

    @Test
    public void getFoodItemRecipeUsageTestFalse(){
        FoodItem testItem = new FoodItem();
        testItem.setId(1L);

        when(service.getFoodItemRecipeUsage(99L)).
                thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Food Item not found"));

        ResponseEntity <Integer> response = ctrl.issueGetFoodItemRecipeUsage(99L);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

}
