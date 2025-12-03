package client.scenes;

import client.utils.ServerUtils;
import jakarta.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import recipes.FoodItem;

public class AddFoodItemCTRL {

    @FXML
    private Label sceneTitle;
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

    private final MainCtrl mainCtrl;
    private final ServerUtils serverUtils = new ServerUtils();

    private FoodItem editingItem;

    @Inject
    public AddFoodItemCTRL(MainCtrl mainCtrl) {
        this.mainCtrl = mainCtrl;
    }

    @FXML
    public void confirmChanges(){
        try {
            FoodItem newItem = new FoodItem(
                    foodItemName.getText(),
                    Integer.parseInt(kcalValue.getText()),
                    Double.parseDouble(carbsValue.getText()),
                    Double.parseDouble(proteinsValue.getText()),
                    Double.parseDouble(fatsValue.getText())
            );

            foodItemName.clear();
            kcalValue.clear();
            carbsValue.clear();
            proteinsValue.clear();
            fatsValue.clear();

            if(editingItem == null) {
                serverUtils.addFoodItem(newItem);
            } else {
                serverUtils.updateFoodItem(editingItem.getName(), newItem);
            }

            confirmButton.setText("Confirm");
            sceneTitle.setText("Adding Food Item");

            mainCtrl.getFoodItemMainCtrl().refreshFoodItems();
            mainCtrl.showFoodItemMain();

        } catch (NumberFormatException e) {
            Alert invalidInput = new Alert(Alert.AlertType.WARNING);
            invalidInput.setTitle("Error");
            invalidInput.setHeaderText("Invalid Input");
            invalidInput.setContentText("Please enter a valid number as shown in the examples.");
            invalidInput.showAndWait();
        }
    }

    @FXML
    public void cancelChanges(){
        editingItem = null;

        foodItemName.clear();
        kcalValue.clear();
        carbsValue.clear();
        proteinsValue.clear();
        fatsValue.clear();

        confirmButton.setText("Confirm");
        sceneTitle.setText("Adding Food Item");
        mainCtrl.showFoodItemMain();
    }

    public void setEditingItem(FoodItem item){
        this.editingItem = item;

        foodItemName.setText(item.getName());
        kcalValue.setText(String.valueOf(item.getKcal()));
        carbsValue.setText(String.valueOf(item.getCarbs()));
        proteinsValue.setText(String.valueOf(item.getProteins()));
        fatsValue.setText(String.valueOf(item.getFats()));

        confirmButton.setText("Save");
        sceneTitle.setText("Editing Food Item");
    }

}
