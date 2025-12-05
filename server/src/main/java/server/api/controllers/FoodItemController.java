package server.api.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import recipes.FoodItem;
import server.repositories.FoodItemRepository;

@RestController
@RequestMapping("/api/food_items")
public class FoodItemController {

    @Autowired
    private FoodItemRepository repo;

    public FoodItemController (FoodItemRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public FoodItem searchByName(@RequestParam("name") String foodType) {
        return repo.findByName(foodType).orElse(null);
    }
}
