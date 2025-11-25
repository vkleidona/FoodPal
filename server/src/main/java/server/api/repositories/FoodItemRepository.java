package server.api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import recipes.FoodItem;
import java.util.Optional;
// importing the JpaRepository interface so we will
// have methods like save, delete, update etc
public interface FoodItemRepository extends JpaRepository<FoodItem, String> {

    // the method looks for a foodItem in the database, it can be there or not
    Optional<FoodItem> findByName(String name);

}
