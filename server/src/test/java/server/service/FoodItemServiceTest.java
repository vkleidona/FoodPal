package server.service;

import exceptions.ExistingFoodItemException;
import exceptions.MalformedRequestException;
import exceptions.ServerErrorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import recipes.FoodItem;

import server.repositories.FoodItemRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

public class FoodItemServiceTest {

    private FoodItemService service;
    private FoodItemRepository repo;

    @BeforeEach
    public void setup() {
        repo = mock(FoodItemRepository.class);
        SimpMessagingTemplate msg = mock(SimpMessagingTemplate.class);
        service = new FoodItemService(repo, msg);
    }

    @Test
    public void addFoodItemTest() {
        FoodItem item = new FoodItem("Milk");
        FoodItem savedItem = new FoodItem("Milk");
        savedItem.setId(2L);

        when(repo.findByName("Milk")).thenReturn(Optional.empty());
        when(repo.save(item)).thenReturn(savedItem);

        FoodItem foodItem = service.addFoodItem(item);
        Optional<FoodItem> lookingInTheDatabase = repo.findByName("Milk");

        assertTrue(lookingInTheDatabase.isEmpty());
        assertEquals(foodItem, savedItem);
    }

    @Test
    public void addFoodItemUnsuccessfulTest() {
        FoodItem savedItem = new FoodItem("Milk");
        savedItem.setId(2L);

        FoodItem nullItem = new FoodItem(null);

        when(repo.findByName(null)).thenThrow(new MalformedRequestException());
        when(repo.findByName(savedItem.getName())).thenThrow(new ExistingFoodItemException());

        assertThrows(MalformedRequestException.class, () -> service.addFoodItem(nullItem));

        assertThrows(ExistingFoodItemException.class, () -> service.addFoodItem(savedItem));
    }

    @Test
    public void getAllFoodItemsTest() {
        List<FoodItem> mockList = List.of(new FoodItem("Bread"));
        when(repo.findAllByOrderByNameAsc()).thenReturn(mockList);

        List<FoodItem> result = service.getAllFoodItems();

        assertEquals(1, result.size());
        assertEquals("Bread", result.getFirst().getName());
    }

    @Test
    public void deleteFoodItemTest() {
        FoodItem test = new FoodItem("Milk");
        test.setId(10L);

        when(repo.existsById(10L)).thenReturn(true);

        assertTrue(service.deleteFoodItem(test.getId()));
    }

    @Test
    public void deleteFoodItemMissingItem() {
        Long id = 99L;
        when(repo.existsById(id)).thenReturn(false);

        assertFalse(service.deleteFoodItem(id));

        verify(repo, times(0)).deleteById(id);
    }

    @Test
    public void updateFoodItemTest() {
        FoodItem old = new FoodItem("lemon");
        old.setId(1L);
        FoodItem updated = new FoodItem("lemon2");

        when(repo.findById(1L)).thenReturn(Optional.of(old));
        when(repo.existsByNameAndIdNot("lemon2", 1L)).thenReturn(false);
        when(repo.save(any())).thenReturn(old);

        FoodItem response = service.updateFoodItem(1L, updated);

        assertEquals("lemon2", response.getName());
    }

    @Test
    public void updateFoodItemUnsuccessfulServerExceptionTest() {
        FoodItem old = new FoodItem("lemon");
        old.setId(1L);
        FoodItem updated = new FoodItem("lemon2");
        updated.setId(2L);

        FoodItem notFound = new FoodItem("cheese");

        when(repo.findByName(notFound.getName())).thenThrow(new ServerErrorException());
        when(repo.findById(1L)).thenReturn(Optional.of(updated));
        when(repo.existsByNameAndIdNot("lemon2", 1L)).thenThrow(new ExistingFoodItemException());

        assertThrows(ServerErrorException.class, () -> service.updateFoodItem(2L, notFound));
    }

    @Test
    public void updateFoodItemUnsuccessfulExistingFoodItemTest() {
        FoodItem old = new FoodItem("lemon");
        old.setId(1L);
        FoodItem updated = new FoodItem("lemon2");
        updated.setId(2L);

        FoodItem notFound = new FoodItem("cheese");

        when(repo.findByName(notFound.getName())).thenThrow(new ServerErrorException());
        when(repo.findById(1L)).thenReturn(Optional.of(updated));
        when(repo.existsByNameAndIdNot("lemon2", 1L)).thenThrow(new ExistingFoodItemException());

        assertThrows(ExistingFoodItemException.class, () -> service.updateFoodItem(1L, updated));
    }

    @Test
    public void testSearch () {
        FoodItem foodItem = new FoodItem("orange");
        when(repo.findByName(foodItem.getName())).thenReturn(Optional.of(foodItem));

        FoodItem foodItem1 = service.searchByName("orange");
        assertNotNull(foodItem1);
        assertEquals("orange", foodItem1.getName());
    }

}
