package server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import recipes.Ingredient;

import java.util.Optional;

public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
    Optional<Ingredient> findById (long i);
}

