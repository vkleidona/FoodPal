package shopping;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import recipes.Ingredient;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ShoppingList implements Serializable {

    /**
     * I know Sebastian said we shouldn't use static, but I thought it
     * was a good implementation of the fact that the ShoppingList is a Singleton
     * Whoever is reviewing this, share your opinion and if I should change it
     */
    private static ShoppingList shoppingList;
    private List<Ingredient> ingredients;

    /**
     * A constructor for the ShoppingList class
     */
    public ShoppingList() {
        this.ingredients = new ArrayList<>();
    }

    public ShoppingList(List <Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public static ShoppingList getShoppingList () {
        if (shoppingList == null) {
            shoppingList = new ShoppingList();
        }
        return shoppingList;
    }

    public List <Ingredient> getIngredients () {
        return this.ingredients;
    }

    /**
     * I need this method to reset all Ingredient entities to null
     * @param ingredients - the ingredients I am setting
     */
    public void setIngredients (List <Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    /**
     * Sets an ingredient at a specific index to the provided ingredient
     * @param index      - the index of the ingredient to be replaced
     * @param ingredient - ingredient to be set
     */
    public void setIngredient (int index, Ingredient ingredient) {
        this.ingredients.set(index, ingredient);
    }

    /**
     * A method to add an ingredient from the ShoppingList
     * @param i - an Ingredient entity to add to the ShoppingList
     */
    public void addIngredient (Ingredient i) {
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
     * Creates a local file which stores the ShoppingList
     */
    public void storeFile () {
        try (ObjectOutputStream outputStream = new ObjectOutputStream(
                new FileOutputStream("shopping_list_ingredients.bin"))) {
            outputStream.writeObject(ingredients);
        } catch (FileNotFoundException e) {
            System.err.println("File not found!");
        } catch (IOException e) {
            System.err.println("Something went wrong!");
        }
    }

    /**
     * Loads the local file into the Shopping list
     */
    public void loadFile () {
        try (ObjectInputStream inputStream =
                     new ObjectInputStream(new FileInputStream("shopping_list_ingredients.bin"))) {
            List <Ingredient> fileIngredients = (List<Ingredient>) inputStream.readObject();
            if (fileIngredients == null) {
                ingredients = new ArrayList<>();
            }else{
                ingredients = fileIngredients;
            }
        }catch (FileNotFoundException e) {
            System.err.println("File not found!");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Class not initialized!");
            shoppingList = getShoppingList();
        }
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
