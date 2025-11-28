package client.scenes;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import recipes.FoodItem;

import java.io.IOException;

public class AddFoodItemCTRL {

    @FXML
    private TextField foodItemName;

    @FXML
    private TextField kcalValue;

    @FXML
    private TextField carbsValue;

    @FXML
    private TextField proteinsValue;

    @FXML
    private TextField fatsValue;

    @FXML
    private Button cancelButton;

    @FXML
    private Button confirmButton;

    private FoodItem editingItem;

    @FXML
    public void confirmChanges(){
        try {
            FoodItem newItem = new FoodItem(
                    foodItemName.getText(),
                    Integer.parseInt(kcalValue.getText()),
                    Integer.parseInt(carbsValue.getText()),
                    Integer.parseInt(proteinsValue.getText()),
                    Integer.parseInt(fatsValue.getText())
            );
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid input");
        }

        // TO DO: Actually doing stuff with the inputted values

        showFoodItemMainUI();
    }

    @FXML
    public void cancelChanges(){
        showFoodItemMainUI();
    }

    @FXML
    public void showFoodItemMainUI() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/client/scenes/FoodItemMain.fxml"));

            Stage stage = (Stage) cancelButton.getScene().getWindow();

            stage.setTitle("Food Items");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setEditingItem(FoodItem item){
        this.editingItem = item;

        foodItemName.setText(item.getName());
        kcalValue.setText(String.valueOf(item.getKcal()));
        carbsValue.setText(String.valueOf(item.getCarbs()));
        proteinsValue.setText(String.valueOf(item.getProteins()));
        fatsValue.setText(String.valueOf(item.getFats()));
    }

}
