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

    /**
     * A method which finds all food items and orders them ascending
     * @return List <FoodItem> - a list of all food items in the database, ordered alphabetically
     */
    List<FoodItem> findAllByOrderByNameAsc();

    /**
     * A method which checks for the existence of a FoodItem with the same name
     * @param name - the name of the updated FoodItem
     * @param id - the ID of the updated FoodItem
     * @return boolean - returns whether the name of the new FoodItem matches
     * a name of any FoodItem already in the database with a different ID
     */
    boolean existsByNameAndIdNot(String name, Long id);
}
