package server.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import recipes.FoodItem;
import server.api.controllers.FoodItemController;
import server.repositories.FoodItemRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

public class FoodItemServiceTest {

    private FoodItemController ctrl;
    private FoodItemRepository repo;

    @BeforeEach
    public void setup() {
        repo = mock(FoodItemRepository.class);
        SimpMessagingTemplate msg = mock(SimpMessagingTemplate.class);
        FoodItemService service = new FoodItemService(repo, msg);
        ctrl = new FoodItemController(service);
    }

    @Test
    public void addFoodItemTest() {
        FoodItem item = new FoodItem("Milk");
        FoodItem savedItem = new FoodItem("Milk");
        savedItem.setId(2L);

        when(repo.findByName("Milk")).thenReturn(Optional.empty());
        when(repo.save(item)).thenReturn(savedItem);

        ResponseEntity<FoodItem> response = ctrl.issueAddFoodItem(item);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(savedItem, response.getBody());

    }

    @Test
    public void issueGetAllFoodItemsTest() {
        List<FoodItem> mockList = List.of(new FoodItem("Bread"));
        when(repo.findAllByOrderByNameAsc()).thenReturn(mockList);

        List<FoodItem> result = ctrl.issueGetAllFoodItems();

        assertEquals(1, result.size());
        assertEquals("Bread", result.getFirst().getName());
    }

    @Test
    public void issueDeleteFoodItemTest() {
        FoodItem test = new FoodItem("Milk");
        test.setId(10L);

        when(repo.existsById(10L)).thenReturn(true);

        ResponseEntity<Void> response = ctrl.issueDeleteFoodItem(10L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void issueDeleteFoodItemMissingItem() {
        Long id = 99L;
        when(repo.existsById(id)).thenReturn(false);

        ResponseEntity<Void> response = ctrl.issueDeleteFoodItem(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(repo, times(0)).deleteById(id);
    }

    @Test
    public void issueUpdateFoodItemTest() {
        FoodItem old = new FoodItem("lemon");
        old.setId(1L);
        FoodItem updated = new FoodItem("lemon2");

        when(repo.findById(1L)).thenReturn(Optional.of(old));
        when(repo.existsByNameAndIdNot("lemon2", 1L)).thenReturn(false);
        when(repo.save(any())).thenReturn(old);

        ResponseEntity<?> response = ctrl.issueUpdateFoodItem(1L, updated);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("lemon2", old.getName());
    }
}
