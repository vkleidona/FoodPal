package server.api.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import recipes.FoodItem;
import server.repositories.FoodItemRepository;

import java.util.List;

@RestController
@RequestMapping("api/fooditems")
public class FoodItemController {

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
        return repo.findAll();
    }

}
