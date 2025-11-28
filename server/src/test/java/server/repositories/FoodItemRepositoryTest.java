package server.repositories;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import recipes.FoodItem;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
@ActiveProfiles("test")
@DataJpaTest
public class FoodItemRepositoryTest {

    @Autowired
    private FoodItemRepository foodItemRepository;

    @Test
    void testSaveAndFindByName() {
        FoodItem apple = new FoodItem("apple", 95, 25, 1, 0);
        foodItemRepository.save(apple);

        Optional<FoodItem> found = foodItemRepository.findByName("apple");
        assertTrue(found.isPresent());
        assertEquals(95, found.get().getKcal());
    }

    @Test
    void testFindByNameNotFound() {
        Optional<FoodItem> found = foodItemRepository.findByName("banana");
        assertFalse(found.isPresent());
    }

}
