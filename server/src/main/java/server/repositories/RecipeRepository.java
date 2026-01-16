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

    @Query("SELECT DISTINCT r FROM Recipe AS r " +
            "LEFT JOIN r.ingredients AS i " +
            "LEFT JOIN i.foodType AS f " +
            "LEFT JOIN r.instructions AS instr " +
            "WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(instr) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Recipe> findAllByTerm(@Param("keyword") String keyword);
}
