package server.api.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import recipes.FoodItem;
import server.service.FoodItemService;

import java.util.List;

@RestController
@RequestMapping("api/fooditems")
public class FoodItemController {

    private final FoodItemService service;

    /**
     * A constructor for the FoodItemController class
     * @param service - the injected Service class which handles this controller's requests
     */
    @Autowired
    public FoodItemController(FoodItemService service) {
        this.service = service;
    }

    /**
     * Creates a new FoodItem
     * @param foodItem - the FoodItem item to be saved
     * @return ResponseEntity <FoodItem> - a response containing the FoodItem and the status
     */
    @PostMapping(path = { "", "/" })
    public ResponseEntity<FoodItem> addFoodItem(@RequestBody FoodItem foodItem) {
        return service.addFoodItem(foodItem);
    }

    /**
     * Retrieves all FoodItems from the database
     * @return List <FoodItem> - a List of all FoodItems from the database
     */
    @GetMapping(path = { "", "/"})
    public List<FoodItem> getAllFoodItems() {
        return service.getAllFoodItems();
    }

    /**
     * Deletes a FoodItem from the database
     * @param id - the id of the FoodItem entity to be deleted
     * @return ResponseEntity - returns whether the method was a success or a failure
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFoodItem(@PathVariable Long id) {
        return service.deleteFoodItem(id);
    }

    /**
     * Saves updates to FoodItem in the database
     * @param id - the id of the FoodItem to be saved
     * @param updatedItem - the new version of the FoodItem
     * @return ResponseEntity - returns either the updated FoodItem
     * or a warning for unsuccessful operation
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateFoodItem(@PathVariable Long id,
                                            @RequestBody FoodItem updatedItem) {

        return service.updateFoodItem(id, updatedItem);
    }

    /**
     * Searches a food item in the database by name
     * @param foodType - the String denoting the name of the FoodItem to be sent
     * @return FoodItem - returns the found FoodItem by name
     */
    @GetMapping("/search")
    public FoodItem searchByName(@RequestParam("name") String foodType) {
        return service.searchByName(foodType);
    }
}
