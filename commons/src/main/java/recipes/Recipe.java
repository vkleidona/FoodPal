package recipes;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Entity
public class Recipe implements Serializable {
    @Id
    @GeneratedValue (strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonManagedReference
    private List <Ingredient> ingredients = new ArrayList<>();

    /*
    new table, recipe and its instructions
     */
    @ElementCollection
    @CollectionTable(name = "RECIPE_INSTRUCTIONS", joinColumns =
    @JoinColumn(name = "RECIPE_ID"))
    @Column(name = "instruction")
    private List<String> instructions = new ArrayList<>();


    /*
    To be implemented at a later date. We will probably use a specific
    built-in class for languages, not String
    private String language;
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

    public Long getId() {
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

    public void setInstructions(List<String> instructions) {
        this.instructions = instructions;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Adds the given ingredient to the list of ingredients in this recipe.
     * Cannot add Null or duplicate Ingredients.
     * @param ingredient - Ingredient to be added to the recipe
     * @return - True if the Ingredient was successfully added, otherwise False
     */
    public boolean addIngredient(Ingredient ingredient) {
        if(ingredient == null || this.ingredients.contains(ingredient)){
            return false;
        } else {
            ingredient.setRecipe(this);
            return this.ingredients.add(ingredient);
        }
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
