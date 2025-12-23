package server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import recipes.FoodItem;
import recipes.Ingredient;

import java.util.Optional;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
    /**
     * Tries to find an Ingredient Object in the repository, searching by ID
     * @param id - the ID of the Ingredient we want to find
     * @return Optional - returns an Optional containing either an Ingredient
     * with the given ID or a null value
     */
    Optional<Ingredient> findById (long id);

    @Query("SELECT COUNT(DISTINCT i.recipe.id) FROM Ingredient AS i WHERE i.foodType = :foodItem")
    int countFoodItemRecipeUsages(@Param("foodItem") FoodItem foodType);
}

