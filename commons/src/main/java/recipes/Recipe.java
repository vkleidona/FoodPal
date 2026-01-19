package recipes;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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


    @ElementCollection
    @CollectionTable(name = "RECIPE_INSTRUCTIONS", joinColumns = @JoinColumn(name = "RECIPE_ID"))
    @Column(name = "instruction")
    private List<String> instructions = new ArrayList<>();

    // new column, number of servings
    @Column(name = "servings")
    private Integer servings;

    @Column(name = "locale", nullable = false)
    private Locale locale;




    /**
     * A public constructor for the Recipe - now, the recipe should be able to be fetched properly
     */
    public Recipe() {
    }


     /**
     * A constructor for the Recipe class
     * @param name - a String which represents the name of the Recipe
     */
    public Recipe(String name) {
        this.name = name;
        this.instructions = new ArrayList<>();
    }

    /**
     * A constructor to be used in tests (as the locale is a non-nullable property
     * @param name - the name of the recipe
     * @param locale - the locale (language) of the recipe
     */
    public Recipe (String name, Locale locale) {
        this.name = name;
        this.instructions = new ArrayList<>();
        this.locale = locale;
    }

    /**
     * A "cloner" of recipes - produces cloned copies of recipes
     * @param other - the other recipe to be cloned
     */
    @SuppressWarnings("CopyConstructorMissesField")
    // The only warning is that we miss the ID - we don't wish to have
    // two recipes with the same ID
    public Recipe(Recipe other){
        this.name = other.name;

        this.ingredients = new ArrayList<>();
        List<Ingredient> old = other.getIngredients();
        if (old != null){
            for (Ingredient current : old) {
                Ingredient copy = new Ingredient(current.getFoodType(), current.getQuantity(),
                        current.getMeasurement());
                copy.setRecipe(this);
                this.ingredients.add(copy);
            }
        }
        this.instructions = new ArrayList<>(other.getInstructions()) ;
        this.servings = other.servings;
        this.locale = other.locale;
    }


    /**
     * A getter for the recipe ID
     * @return Long - returns the ID of the recipe
     */
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

    public Integer getServings() {
        return servings;
    }

    public void setServings(Integer servings) {
        this.servings = servings;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Locale getLocale () {
        return this.locale;
    }

    public void setLocale (Locale locale) {
        this.locale = locale;
    }

    /**
     * Adds the given ingredient to the list of ingredients in this recipe.
     * Cannot add Null or duplicate Ingredients.
     * @param ingredient - Ingredient to be added to the recipe
     */
    public void addIngredient(Ingredient ingredient) {
        if (ingredient == null){
            return;
        }
        ingredient.setRecipe(this);
        this.ingredients.add(ingredient);
    }

    /**
     * A method which removes the ingredient from the Recipe if it is present
     * after a user has deleted a Food Item
     * @param foodItemID - the FoodItem ID of the item just deleted
     * @return boolean - returns true if the item was successfully deleted and false otherwise
     */
    public boolean removeIngredientIfPresent (Long foodItemID) {
        return this.ingredients.removeIf(i -> i.getFoodType().getId().equals(foodItemID));
    }



    /**
     * A method which removes the ingredient from the Recipe if it is present
     * after a user has deleted a Food Item
     * @param foodItemID - the FoodItem ID of the item just deleted
     * @param foodItemName - the new name of the FoodItem
     * @return boolean - returns true if the item was successfully deleted and false otherwise
     */
    public boolean changeIngredientIfPresent (Long foodItemID, String foodItemName) {
        for (int i = 0; i < this.getIngredients().size();i++) {
            if (this.ingredients.get(i).getFoodType().getId().equals(foodItemID)) {
                this.ingredients.get(i).getFoodType().setName(foodItemName);
                return true;
            }
        }
        return false;
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
