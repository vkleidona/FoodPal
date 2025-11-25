package server.api.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import recipes.Recipe;
// created so we can use all the standard methods
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
}
