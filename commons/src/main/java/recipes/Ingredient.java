package recipes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import java.util.Objects;

/*
I removed the recipeId field, because to define the relationship, we do not need
a separate field, having a Recipe object reference in the
Ingredient class is enough.
Also, by having multiple Ids, Jpa expects a composite key, which is
not needed here.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
public class Ingredient {
    @Id
    @GeneratedValue (strategy = GenerationType.AUTO)
    /*
    I am converting the long into Long, so data has null values instead of 0
    That way, we can test ingredientID in the database and locally,
    IDs won't be the same (all 0)
     */
    private Long ingredientID;

    /*
    Jpa automatically adds a foreign key column called recipeId to
    the Ingredient table
     */
    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "foodType", referencedColumnName = "name")
    private FoodItem foodType;

    private double quantity;

    @Column(name = "measurement_type")
    private String measurement;

    // a new column, here all the ingredients will have the equivalent in grams
    // this will help us calculate the nutritional value
    // so if somebody entered 1kg of potatoes, in this column we will have 1000 (grams)
    @Column(name = "quantity_in_grams")
    private double quantityInGrams;

    @ManyToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "RECIPE_ID", referencedColumnName = "ID")

    @com.fasterxml.jackson.annotation.JsonBackReference
    private Recipe recipe;

    /**
     * A Constructor for the ObjectMapper to use
     */
    public Ingredient () {

    }

    /**
     * constructor for our Ingredient, we will now also save the equivalent to grams
     * @param quantity here, still int, can be changed to long
     * @param measurement the chosen unit
     * @param quantityInGrams the equivalent in grams
     */
    public Ingredient(double quantity, String measurement, double quantityInGrams) {
        this.quantity = quantity;
        this.measurement = measurement;
        this.quantityInGrams = quantityInGrams;
    }

    /**
     * getter
     * @return the quantity
     */
    public double getQuantityInGrams() {
        return quantityInGrams;
    }

    /**
     * setter
     * @param quantityInGrams double
     */
    public void setQuantityInGrams(double quantityInGrams) {
        this.quantityInGrams = quantityInGrams;
    }

    /**
     * A Constructor for the Ingredient class
     * @param foodType - provides the foodType of an Ingredient
     * @param quantity - used to determine the quantity of an Ingredient
     * @param measurement - provides the measurement of an Ingredient
     */
    public Ingredient(FoodItem foodType, double quantity, String measurement) {
        this.foodType = foodType;
        this.quantity = quantity;
        this.measurement = measurement;
    }

    public Ingredient(double quantity, String measurement) {
        this.quantity = quantity;
        this.measurement = measurement;
    }

    public Long getIngredientID() {
        return ingredientID;
    }

    public FoodItem getFoodType() {
        return foodType;
    }

    public double getQuantity() {
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

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public void setMeasurement(String measurement) {
        this.measurement = measurement;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    public void setIngredientID(Long ingredientID) {
        this.ingredientID = ingredientID;
    }

    public String getFoodName(){
        return foodType.getName();
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
     * A custom implementation of the toString method for the Ingredient class
     * @return String - returns a custom translation, as that will be presented
     * to the user in the ShoppingList
     */
    public String toString () {
        return this.getFoodName() + " (" +
                this.getQuantity() + " " + this.getMeasurement() + ")";
    }
}
