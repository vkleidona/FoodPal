package recipes;

import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.List;
import java.util.Objects;

@Entity
public class FoodItem {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String name;

    @OneToMany(mappedBy = "foodType", cascade = CascadeType.ALL)
    private List<Ingredient> ingredientsWithFoodItem;

    @OneToOne(mappedBy = "correspondingFoodItem")
    private Nutrition nutritionalValues;

    /*
    Do we want to store which food is in which recipe?
    Probably at a later date, we will have to (if we implement Shopping List),
    but for now?

    @OneToMany(mappedBy = "recipe")
    private List<Recipe> recipesWithFoodItem;
    */

    /**
     * Constructor for the FoodItem entity
     * @param name - the name of the FoodItem
     */
    public FoodItem(String name) {
        this.name = name;
    }

    /**
     * Constructor for the FoodItem entity used by the ObjectMapper
     */
    private FoodItem () {

    }

    public String getName() {
        return name;
    }

    public List<Ingredient> getIngredientsWithFoodItem() {
        return ingredientsWithFoodItem;
    }

    public Nutrition getNutritionalValues() {
        return nutritionalValues;
    }

    public void setNutritionalValues(Nutrition nutritionalValues) {
        this.nutritionalValues = nutritionalValues;
       if (nutritionalValues.getCorrespondingFoodItem()!=this) {
           nutritionalValues.setCorrespondingFoodItem(this);
        }
    }

    public void setIngredientsWithFoodItem(List<Ingredient> ingredientsWithFoodItem) {
        this.ingredientsWithFoodItem = ingredientsWithFoodItem;
    }

    /**
     * An implementation of the equals method for FoodItem
     * @param o - the reference object with which to compare.
     * @return boolean - returns whether the two Objects are equal or not
     */
    @Override
    public boolean equals (Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    /**
     * An implementation of the hashCode method for the FoodItem class
     * @return int - returns the hashCode of the FoodItem
     */
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    /**
     * An implementation of the toString method for the FoodItem class
     * @return String - returns a String translation of the FoodItem
     * in a human-readable format
     */
    public String toString () {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
