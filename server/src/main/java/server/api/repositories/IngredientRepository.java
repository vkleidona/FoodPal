
package server.api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import recipes.Ingredient;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
}


/*
review Ingredient class
 */
