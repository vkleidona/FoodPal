package server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import recipes.Recipe;

import java.util.List;
import java.util.Optional;

// created so we can use all the standard methods
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    List<Recipe> findAllByName(String name);

    // find recipes which contain ingredient where the name of that ingredient is the same as
    // the name entered in the search.
    @Query("SELECT DISTINCT r FROM Recipe AS r JOIN r.ingredients AS i JOIN i.foodType AS f " +
            "WHERE LOWER(f.name) LIKE LOWER(:ingredientName)")
    List<Recipe> findAllByIngredientName(@Param("ingredientName") String ingredientName);

}
