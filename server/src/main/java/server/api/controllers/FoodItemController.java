package server.api.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
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

    public FoodItemController(FoodItemRepository repo) {
        this.repo = repo;
    }

    @PostMapping(path = { "", "/" })
    public ResponseEntity<FoodItem> addFoodItem(@RequestBody FoodItem foodItem) {

        if(foodItem.getName() == null || foodItem.getName().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        FoodItem saved = repo.save(foodItem);
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
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/search")
    public FoodItem searchByName(@RequestParam("name") String foodType) {
        return repo.findByName(foodType).orElse(null);
    }
}
