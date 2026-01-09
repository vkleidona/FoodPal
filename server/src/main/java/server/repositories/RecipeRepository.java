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
            "WHERE LOWER(f.name) LIKE LOWER(CONCAT('%', :ingredientName, '%'))")
    List<Recipe> findAllByIngredientName(@Param("ingredientName") String ingredientName);

    @Query("SELECT DISTINCT r FROM Recipe AS r JOIN r.instructions AS instr " +
            "WHERE LOWER(instr) LIKE LOWER(CONCAT('%', :instructionTerm, '%'))")
    List<Recipe> findAllByInstruction(@Param("instructionTerm") String instructionTerm);

    @Query("SELECT DISTINCT r FROM Recipe AS r " +
            "JOIN r.ingredients AS i " +
            "JOIN i.foodType AS f " +
            "JOIN r.instructions AS instr " +
            "WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(instr) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Recipe> findAllByTerm(@Param("keyword") String keyword);
}
