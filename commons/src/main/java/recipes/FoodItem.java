package recipes;

import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Entity
public class FoodItem {
    @Id
    private String name;

    @OneToMany(mappedBy = "foodType", cascade = CascadeType.ALL)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Ingredient> ingredientsWithFoodItem = new ArrayList<>();

    private int kcal;
    private int carbs;
    private int proteins;
    private int fats;

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
     * @param kcal - the number of kilocalories this FoodItem contains per unit
     * @param carbs - the grams of carbohydrates this FoodItem contains per unit
     * @param proteins - the grams of proteins this FoodItem contains per unit
     * @param fats - the grams of fats this FoodItem contains per unit
     */
    public FoodItem(String name, int kcal, int carbs, int proteins, int fats) {
        this.name = name;
        this.kcal = kcal;
        this.carbs = carbs;
        this.proteins = proteins;
        this.fats = fats;
    }

    /**
     * Secondary constructor for the FoodItem entity that does not require the nutritional values.
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

    public void setIngredientsWithFoodItem(List<Ingredient> ingredientsWithFoodItem) {
        this.ingredientsWithFoodItem = ingredientsWithFoodItem;
    }

    public int getKcal() {
        return kcal;
    }

    public int getCarbs() {
        return carbs;
    }

    public int getProteins() {
        return proteins;
    }

    public int getFats() {
        return fats;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setKcal(int kcal) {
        this.kcal = kcal;
    }

    public void setCarbs(int carbs) {
        this.carbs = carbs;
    }

    public void setProteins(int proteins) {
        this.proteins = proteins;
    }

    public void setFats(int fats) {
        this.fats = fats;
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
        return Objects.hash(name, kcal, carbs, proteins, fats);
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
