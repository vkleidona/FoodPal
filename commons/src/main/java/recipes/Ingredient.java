package recipes;

import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Objects;

/*
I removed the recipeId field, because to define the relationship, we do not need
a separate field, having a Recipe object reference in the
Ingredient class is enough.
Also, by having multiple Ids, Jpa expects a composite key, which is
not needed here.
 */


@Entity
public class Ingredient {
    @Id
    @GeneratedValue (strategy = GenerationType.AUTO)
    private long IngredientID;
/*
    here was an @Id, I remove it
    private long recipeID;
 */

    /*
    Jpa automatically adds a foreign key column called recipeId to
    the Ingredient table
     */
    @ManyToOne
    @JoinColumn(name = "foodType", referencedColumnName = "name")
    private FoodItem foodType;

    private int quantity;

    @Column(name = "measurement_type")
    private String measurement;

    @ManyToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "RECIPE_ID", referencedColumnName = "ID")

    @com.fasterxml.jackson.annotation.JsonBackReference
    private Recipe recipe;

    /**
     * A Constructor for the ObjectMapper to use
     */
    private Ingredient () {

    }

    /**
     * A Constructor for the Ingredient class
     * @param quantity - used to determine the quantity of an Ingredient
     * @param measurement - provides the measurement of an Ingredient
     */
    public Ingredient(int quantity, String measurement) {
        this.quantity = quantity;
        this.measurement = measurement;
    }

   /*
      public long getRecipeID() {
        return recipeID;
    }
    */

    public long getIngredientID() {
        return IngredientID;
    }

    public FoodItem getFoodType() {
        return foodType;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getMeasurement() {
        return measurement;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public void setFoodType(FoodItem foodType) {
        this.foodType = foodType;
    }

   /* public void setRecipeID(long id) {
        this.recipeID = id;
    }
    */

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
        /*
        if(this.recipeID!=this.getRecipe().getId()) {
            this.setRecipeID(this.getRecipe().getId());
        }
        */
    }


    /**
     * An implementation of the equals method for the Ingredient class
     * @param o - the reference object with which to compare.
     * @return boolean - returns whether the two Objects compared are equal.
     */
    public boolean equals (Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    /**
     * An implementation of the hashCode method for the Ingredient class
     * @return int - returns the hashCode of the Ingredient
     */
    public int hashCode () {
        return Objects.hash(quantity, measurement);
    }

    /**
     * An implementation of the toString method for the Ingredient class
     * @return String - returns a String translation of the Ingredient
     * in a human-readable format
     */
    public String toString () {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
