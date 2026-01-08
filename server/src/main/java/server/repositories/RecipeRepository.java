package server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import recipes.Recipe;

import java.util.List;
import java.util.Optional;

// created so we can use all the standard methods
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    List<Recipe> findAllByName(String name);

}
