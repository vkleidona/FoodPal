package recipes;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Entity
public class Recipe {
    @Id
    @GeneratedValue (strategy = GenerationType.AUTO)
    private long id;

    @Column
    private String name;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL)
    private List <Ingredient> ingredients;
    private List <String> instructions;

    /*
    To be implemented at a later date. We will probably use a specific
    built-in class for languages, not String
    private String language;
     */


    /**
     * A constructor for the ObjectMapper (for conversions to JSON)
     */
    private Recipe() {

    }

    /**
     * A constructor for the Recipe class
     * @param name - a String which represents the name of the Recipe
     */
    public Recipe(String name) {
        this.name = name;
        this.instructions = new ArrayList<>();
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public List<String> getInstructions() {
        return instructions;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public void setId(long id) {
        this.id = id;
    }

    /**
     * An implementation of the equals method for the Recipe class
     * @param o - the reference object with which to compare.
     * @return boolean - returns whether the two Objects are equal or not
     */
    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    /**
     * An implementation of the hashCode method for the Recipe class
     * @return int - returns the hashCode of the Recipe
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, instructions);
    }

    /**
     * An implementation of the toString method for the Recipe class
     * @return String - returns a String translation of the Recipe class
     * in a human-readable format
     */
    public String toString () {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
