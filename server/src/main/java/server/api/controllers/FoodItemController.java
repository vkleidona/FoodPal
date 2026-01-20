package server.api.controllers;

import org.springframework.dao.DuplicateKeyException;
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
     * Issues the creation of a new FoodItem
     * @param foodItem - the FoodItem item to be saved in the database
     * @return ResponseEntity <FoodItem> - a response containing the FoodItem and the status
     */
    @PostMapping(path = { "", "/" })
    public ResponseEntity<FoodItem> issueAddFoodItem(@RequestBody FoodItem foodItem) {
        FoodItem addedFoodItem = service.addFoodItem(foodItem);
        return ResponseEntity.ok(addedFoodItem);
    }

    /**
     * Issues the retrieval of all FoodItems from the database
     * @return List <FoodItem> - a List of all FoodItems from the database
     */
    @GetMapping(path = { "", "/"})
    public List<FoodItem> issueGetAllFoodItems() {
        return service.getAllFoodItems();
    }

    /**
     * Issues the deletion of a FoodItem from the database
     * @param id - the id of the FoodItem entity to be deleted
     * @return ResponseEntity - returns whether the method was a success or a failure
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> issueDeleteFoodItem(@PathVariable Long id) {
        boolean deleted = service.deleteFoodItem(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Issues an update to a specific FoodItem in the database
     * @param id - the id of the FoodItem to be saved
     * @param updatedItem - the new version of the FoodItem
     * @return ResponseEntity - returns either the updated FoodItem
     * or a warning for an unsuccessful operation
     */
    @PutMapping("/{id}")
    public ResponseEntity<FoodItem> issueUpdateFoodItem(@PathVariable Long id,
                                                 @RequestBody FoodItem updatedItem) {
        FoodItem addedFoodItem = service.updateFoodItem(id, updatedItem);
        return ResponseEntity.ok(addedFoodItem);
    }

    /**
     * Issues the search a food item in the database by name
     * @param foodType - the String denoting the name of the FoodItem to be sent
     * @return FoodItem - returns the found FoodItem by name
     */
    @GetMapping("/search")
    public FoodItem issueSearchByName(@RequestParam("name") String foodType) {
        return service.searchByName(foodType);
    }
}
