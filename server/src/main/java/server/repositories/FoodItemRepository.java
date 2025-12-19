package server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import recipes.FoodItem;
import java.util.List;
import java.util.Optional;
// importing the JpaRepository interface so we will
// have methods like save, delete, update etc
public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {

    /**
     * Searches for a FoodItem by a given name
     * @param name - the name (unique ID) of the FoodItem we search for
     * @return Optional - returns either a FoodItem object found by the name or a null value
     * if no FoodItem with such a name exists
     */
    Optional<FoodItem> findByName(String name);

    List<FoodItem> findAllByOrderByNameAsc();
}
