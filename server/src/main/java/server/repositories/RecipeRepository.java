package server.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import recipes.Recipe;

import java.util.List;

// created so we can use all the standard methods
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    /**
     * A method which finds all recipes by name
     * @param name - the name entered into the search bar
     * @return List <Recipe> - all recipes which have at least a partially
     * matching name
     */
    List<Recipe> findAllByName(String name);

    /**
     * A method which queries the database for all recipes with the shared name,
     * ingredients or instructions
     * @param keyword - the keyword which provides the search term
     * @return - List <Recipe> - the list of all recipes with matching terms
     */
    @Query("SELECT DISTINCT r FROM Recipe AS r " +
            "LEFT JOIN r.ingredients AS i " +
            "LEFT JOIN i.foodType AS f " +
            "LEFT JOIN r.instructions AS instr " +
            "WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(instr) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Recipe> findAllByTerm(@Param("keyword") String keyword);
}
