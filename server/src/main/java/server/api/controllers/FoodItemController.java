package server.api.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import recipes.FoodItem;
import server.repositories.FoodItemRepository;

import java.lang.annotation.Repeatable;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/fooditems")
public class FoodItemController {

    @Autowired
    private final FoodItemRepository repo;

    @Autowired
    private SimpMessagingTemplate messageTemplate;

    public FoodItemController(FoodItemRepository repo) {
        this.repo = repo;
    }

    @PostMapping(path = { "", "/" })
    public ResponseEntity<FoodItem> addFoodItem(@RequestBody FoodItem foodItem) {

        if(foodItem.getName() == null || foodItem.getName().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        FoodItem saved = repo.save(foodItem);
        messageTemplate.convertAndSend("/topic/food_items", foodItem);
        messageTemplate.convertAndSend("/topic/food_items/" + foodItem.getId(), foodItem);
        return ResponseEntity.ok(saved);
    }

    @GetMapping(path = { "", "/"})
    public List<FoodItem> getAllFoodItems() {
        return repo.findAllByOrderByNameAsc();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFoodItem(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        repo.deleteById(id);
        messageTemplate.convertAndSend("/topic/food_items", "DELETED_"+id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateFoodItem(@PathVariable Long id, @RequestBody FoodItem updatedItem) {

        Optional<FoodItem> foodItem = repo.findById(id);
        if (foodItem.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (repo.existsByNameAndIdNot(updatedItem.getName(), id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("A fooditem with the same already exists");
        }

        FoodItem item = foodItem.get();
        item.setName(updatedItem.getName());
        item.setKcal(updatedItem.getKcal());
        item.setCarbs(updatedItem.getCarbs());
        item.setProteins(updatedItem.getProteins());
        item.setFats(updatedItem.getFats());

        FoodItem saved = repo.save(item);
        messageTemplate.convertAndSend("/topic/food_items", saved);
        messageTemplate.convertAndSend("/topic/food_items/" + item.getId(), saved);
        return ResponseEntity.ok(saved);
    }

    /**
     * Searches a food item in the database by name
     * @param foodType - the String denoting the name of the FoodItem to be sent
     * @return FoodItem - returns the found FoodItem by name
     */
    @GetMapping("/search")
    public FoodItem searchByName(@RequestParam("name") String foodType) {
        return repo.findByName(foodType).orElse(null);
    }
}
