package client.scenes;

import client.utils.ServerUtils;
import jakarta.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import recipes.FoodItem;

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

    private final MainCtrl mainCtrl;
    private final ServerUtils serverUtils = new ServerUtils();

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
                    Integer.parseInt(carbsValue.getText()),
                    Integer.parseInt(proteinsValue.getText()),
                    Integer.parseInt(fatsValue.getText())
            );

            serverUtils.addFoodItem(newItem);

            mainCtrl.showFoodItemMain();

        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid input");
        }
    }

    @FXML
    public void cancelChanges(){
        mainCtrl.showFoodItemMain();
    }

    public void setEditingItem(FoodItem item){
        foodItemName.setText(item.getName());
        kcalValue.setText(String.valueOf(item.getKcal()));
        carbsValue.setText(String.valueOf(item.getCarbs()));
        proteinsValue.setText(String.valueOf(item.getProteins()));
        fatsValue.setText(String.valueOf(item.getFats()));
    }

}
