package server.api.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import recipes.FoodItem;
import server.repositories.FoodItemRepository;
import java.util.List;

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

    @DeleteMapping("/{name}")
    public ResponseEntity<Void> deleteFoodItem(@PathVariable String name) {
        if (!repo.existsById(name)) {
            return ResponseEntity.notFound().build();
        }

        repo.deleteById(name);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{name}")
    public ResponseEntity<FoodItem> updateFoodItem(@PathVariable String name, @RequestBody FoodItem updatedItem) {
        if (!repo.existsById(name)) {
            return ResponseEntity.notFound().build();
        }

        repo.findById(name).map(item -> {
            item.setName(updatedItem.getName());
            item.setKcal(updatedItem.getKcal());
            item.setCarbs(updatedItem.getCarbs());
            item.setProteins(updatedItem.getProteins());
            item.setFats(updatedItem.getFats());

            FoodItem saved = repo.save(item);
            return ResponseEntity.ok(saved);
        });
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public FoodItem searchByName(@RequestParam("name") String foodType) {
        return repo.findByName(foodType).orElse(null);
    }
}
