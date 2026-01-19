package shopping;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import recipes.Ingredient;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ShoppingList implements Serializable {

    private List<Ingredient> ingredients = new ArrayList<>();
    private final File fileLocation;

    /**
     * A constructor for the ShoppingList class for default production
     */
    public ShoppingList() {
        this(new File("./shopping_list.json"));
    }

    /**
     * A constructor for the ShoppingList class for testing
     * @param customFile - File - where should the ShoppingList be
     * stored / saved at
     */
    public ShoppingList (File customFile) {
        this.fileLocation = customFile;

        File folder = fileLocation.getParentFile();
        if (folder != null && !folder.exists()) {
            if (folder.mkdirs()) {
                System.out.println("Configuration folder successfully initialized");
            }else{
                System.err.println("Could not initialize directory");
            }
        }

        if (!fileLocation.exists()) {
            try {
                if (fileLocation.createNewFile()) {
                    System.out.println("Shopping list file initialized successfully");
                }else{
                    System.err.println("Error: Shopping list file failed to initialize");
                }
                java.nio.file.Files.writeString(fileLocation.toPath(), "[]");
            } catch (IOException e) {
                System.err.println("Error: Could not create shopping list file: " + e.getMessage());
            }
        }

        loadFile();
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
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            if (fileLocation.getParentFile().mkdirs()) {
                System.out.println("Directory for saving initialized successfully");
            }else{
                System.err.println("Error - could not initialize directory for saving");
            }
            objectMapper.writeValue(fileLocation, ingredients);
        } catch (IOException e) {
            System.err.println("Error - Shopping List file could not be stored" + e.getMessage());
        }
    }

    /**
     * Loads the local file into the Shopping list
     */
    public void loadFile () {
        if (!fileLocation.exists()) {
            ingredients.clear();
            return;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            List<Ingredient> loaded = objectMapper.readValue(fileLocation,
                    new TypeReference<>() {
                    });
            this.ingredients.clear();
            this.ingredients.addAll(loaded);
        }catch (IOException e) {
            System.err.println("Error - Shopping List could not be loaded" + e.getMessage());
            ingredients = new ArrayList<>();
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
