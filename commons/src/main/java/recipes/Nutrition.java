package recipes;

import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Objects;

@Entity
public class Nutrition {

    @Id
    private String foodName;

    private int kcal;
    private int carbs;
    private int proteins;
    private int fats;

    @OneToOne
    @PrimaryKeyJoinColumn(name = "foodName", referencedColumnName = "name")
    private FoodItem correspondingFoodItem;

    /**
     * The default constructor for the Nutrition class
     * @param kcal - the number of kilocalories this FoodItem contains per unit
     * @param carbs - the grams of carbohydrates this FoodItem contains per unit
     * @param proteins - the grams of proteins this FoodItem contains per unit
     * @param fats - the grams of fats this FoodItem contains per unit
     */
    public Nutrition(int kcal, int carbs, int proteins, int fats) {
        this.kcal = kcal;
        this.carbs = carbs;
        this.proteins = proteins;
        this.fats = fats;
    }

    /**
     * A constructor to be used by the ObjectMapper translations
     */
    private Nutrition () {

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

    public FoodItem getCorrespondingFoodItem() {
        return correspondingFoodItem;
    }

    public void setCorrespondingFoodItem(FoodItem correspondingFoodItem) {
        this.correspondingFoodItem = correspondingFoodItem;
    }

    /**
     * An implementation of the equals method for the Nutrition object
     * @param o - the reference object with which to compare.
     * @return boolean - returns whether the two objects are equal or not
     */
    @Override
    public boolean equals (Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    /**
     * An implementation of the hashCode() method for the Nutrition class
     * @return int - returns a hashCode for the Nutrition class
     */
    @Override
    public int hashCode() {
        return Objects.hash(kcal, carbs, proteins, fats);
    }

    /**
     * An implementation of the toString() method for the Nutrition class
     * @return String - returns a String for the Nutrition class
     */
    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
