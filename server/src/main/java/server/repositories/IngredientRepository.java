package server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
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
}

