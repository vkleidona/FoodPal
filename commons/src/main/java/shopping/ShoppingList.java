package shopping;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import recipes.Ingredient;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ShoppingList {
    private List<Ingredient> ingredients;

    /**
     * A constructor for the ShoppingList class
     */
    public ShoppingList() {
        this.ingredients = new ArrayList<>();
    }

    public List <Ingredient> getIngredients () {
        return this.ingredients;
    }

    /**
     * A method to add an ingredient from the ShoppingList
     * @param i - an Ingredient entity to add to the ShoppingList
     */
    public void addIngredient (Ingredient i) {
        //TODO: Possibly implement the joining together of ingredients of the same FoodType
        this.ingredients.add(i);
    }

    /**
     * A method to remove an ingredient from the ShoppingList
     * @param i - which ingredient should be removed from the ShoppingList
     */
    public void removeIngredient (Ingredient i) {
        this.ingredients.remove(i);
    }

    /**
     * An implementation of the equals method for ShoppingList
     * @param other - the reference object with which to compare.
     * @return boolean - returns whether the two Objects are equal or not
     */
    public boolean equals (Object other) {
        return EqualsBuilder.reflectionEquals(this, other);
    }

    /**
     * An implementation of the hashCode method for the ShoppingList class
     * @return int - returns the hashCode of the ShoppingList
     */
    public int hashCode () {
        return Objects.hash(ingredients);
    }

    /**
     * An implementation of the toString method for the ShoppingList class
     * @return String - returns a String translation of the ShoppingList
     * in a human-readable format
     */
    public String toString () {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
