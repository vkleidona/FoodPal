package server.api.controllerTests;

import exceptions.ExistingFoodItemException;
import exceptions.MalformedRequestException;
import exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import recipes.FoodItem;
import server.api.controllers.FoodItemController;
import server.service.FoodItemService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class FoodItemControllerTest {
    private FoodItemController ctrl;
    private FoodItemService service;

    @BeforeEach
    public void setup(){
        service = mock(FoodItemService.class);
        ctrl = new FoodItemController(service);
    }

    @Test
    public void addFoodItemTest(){
        new FoodItem("Milk");
        FoodItem savedItem = new FoodItem("Milk");
        savedItem.setId(2L);

        when(service.addFoodItem(savedItem)).thenReturn(savedItem);

        ResponseEntity<FoodItem> response = ctrl.issueAddFoodItem(savedItem);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(savedItem, response.getBody());
    }

    @Test
    public void addFoodItemTestUnsuccessfully(){
        FoodItem savedItem = new FoodItem("Milk");
        savedItem.setId(2L);

        FoodItem foodItem1 = new FoodItem("");
        FoodItem foodItem2 = new FoodItem("Milk");

        when(service.addFoodItem(foodItem1)).thenThrow
                (new MalformedRequestException());

        when(service.addFoodItem(foodItem2)).thenThrow
                (new ExistingFoodItemException());

        assertThrows(MalformedRequestException.class, () -> ctrl.issueAddFoodItem(foodItem1));
        assertThrows(ExistingFoodItemException.class, () -> ctrl.issueAddFoodItem(foodItem2));
    }

    @Test
    public void issueGetAllFoodItemsTest(){
        List<FoodItem> mockList = List.of(new FoodItem("Bread"));
        when(service.getAllFoodItems()).thenReturn(mockList);

        List<FoodItem> result = ctrl.issueGetAllFoodItems();

        assertEquals(1, result.size());
        assertEquals("Bread", result.getFirst().getName());
    }

    @Test
    public void issueDeleteFoodItemTest(){
        FoodItem test = new FoodItem("Milk");
        test.setId(10L);

        when(service.deleteFoodItem(10L)).thenReturn(true);

        ResponseEntity<Void> response = ctrl.issueDeleteFoodItem(10L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void issueDeleteFoodItemMissingItem(){
        Long id = 99L;
        when(service.deleteFoodItem(id)).thenReturn(false);

        ResponseEntity<Void> response = ctrl.issueDeleteFoodItem(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void issueUpdateFoodItemTest(){
        FoodItem old = new FoodItem("lemon");
        old.setId(1L);
        FoodItem updated = new FoodItem("lemon2");

        when(service.updateFoodItem(1L, updated)).thenReturn(updated);

        ResponseEntity<FoodItem> response = ctrl.issueUpdateFoodItem(1L, updated);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("lemon2", response.getBody().getName());
    }

    @Test
    public void issueUpdateFoodItemTestUnsuccessful(){
        FoodItem old = new FoodItem("lemon");
        old.setId(1L);
        FoodItem updated = new FoodItem("lemon2");

        when(service.updateFoodItem(3L, updated)).thenThrow(new ResourceNotFoundException());

        FoodItem sameName = new FoodItem("lemon");
        sameName.setId(4L);

        when(service.updateFoodItem(1L, sameName)).thenThrow(new ExistingFoodItemException ());

        assertThrows(ResourceNotFoundException.class, () -> ctrl.issueUpdateFoodItem(3L, updated));

        assertThrows(ExistingFoodItemException.class, () -> ctrl.issueUpdateFoodItem(1L, sameName));
    }

    @Test
    public void searchByTermTest() {
        FoodItem f1 = new FoodItem ("egg");
        FoodItem f2 = new FoodItem("Potato");

        List <FoodItem> testList = List.of(f1, f2);

        when(service.searchByName("egg")).thenReturn(f1);

        FoodItem returned = ctrl.issueSearchByName("egg");
        assertEquals(testList.getFirst(), returned);
    }

}
