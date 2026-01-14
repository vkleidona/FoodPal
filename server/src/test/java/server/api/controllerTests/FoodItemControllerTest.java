package server.api.controllerTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import recipes.FoodItem;
import server.api.controllers.FoodItemController;
import server.repositories.FoodItemRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FoodItemControllerTest {
    private FoodItemController ctrl;
    private FoodItemRepository repo;
    private SimpMessagingTemplate msg;

    @BeforeEach
    public void setup(){
        repo = mock(FoodItemRepository.class);
        msg = mock(SimpMessagingTemplate.class);

        ctrl = new FoodItemController(repo, msg);
    }

    @Test
    public void getAllFoodItemsTest(){
        List<FoodItem> mockList = List.of(new FoodItem("Bread"));
        when(repo.findAllByOrderByNameAsc()).thenReturn(mockList);

        List<FoodItem> result = ctrl.getAllFoodItems();

        assertEquals(1, result.size());
        assertEquals("Bread", result.get(0).getName());
    }

    @Test
    public void deleteFoodItemTest(){
        FoodItem test = new FoodItem("Milk");
        test.setId(10L);

        when(repo.existsById(10L)).thenReturn(true);

        ResponseEntity<Void> response = ctrl.deleteFoodItem(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

}
