package server.service;

import commons.MiniUpdate;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import recipes.FoodItem;
import server.repositories.FoodItemRepository;

import java.util.List;
import java.util.Optional;

@Service
public class FoodItemService {

    private final FoodItemRepository repo;

    private final SimpMessagingTemplate messageTemplate;

    /**
     * A constructor for the FoodItemService class
     * @param repo - the repository which is to be saved
     * @param messageTemplate - the communication tool to push server changes to the client
     */
    @Autowired
    public FoodItemService(FoodItemRepository repo, SimpMessagingTemplate messageTemplate) {
        this.repo = repo;
        this.messageTemplate = messageTemplate;
    }

    /**
     * Creates a new FoodItem
     * @param foodItem - the FoodItem item to be saved
     * @return ResponseEntity <FoodItem> - a response containing the FoodItem and the status
     */
    public FoodItem addFoodItem(FoodItem foodItem) {

        if(foodItem.getName() == null || foodItem.getName().isEmpty()) {
            throw new BadRequestException();
        }

        if(repo.findByName(foodItem.getName()).isPresent()) {
            throw new DuplicateKeyException("Found an item with the same name");
        }

        FoodItem saved = repo.save(foodItem);
        MiniUpdate miniUpdate = new MiniUpdate (saved.getId(), saved.getName());
        messageTemplate.convertAndSend("/topic/food_items", miniUpdate);
        messageTemplate.convertAndSend("/topic/food_items/" + foodItem.getId(), saved);
        messageTemplate.convertAndSend("/topic/food_items/new", saved);
        return saved;
    }

    /**
     * Retrieves all FoodItems from the database
     * @return List <FoodItem> - a List of all FoodItems from the database
     */
    public List<FoodItem> getAllFoodItems() {
        return repo.findAllByOrderByNameAsc();
    }

    /**
     * Deletes a FoodItem from the database
     * @param id - the id of the FoodItem entity to be deleted
     * @return ResponseEntity - returns whether the method was a success or a failure
     */
    public boolean deleteFoodItem(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            return false;
        }

        repo.deleteById(id);
        messageTemplate.convertAndSend("/topic/food_items/delete", id);
        return true;
    }

    /**
     * Saves updates to FoodItem in the database
     * @param id - the id of the FoodItem to be saved
     * @param updatedItem - the new version of the FoodItem
     * @return ResponseEntity - returns either the updated FoodItem
     * or a warning for unsuccessful operation
     */
    public FoodItem updateFoodItem(@PathVariable Long id,
                                            @RequestBody FoodItem updatedItem) {

        Optional<FoodItem> foodItem = repo.findById(id);
        if (foodItem.isEmpty()) {
            throw new NotFoundException();
        }

        if (repo.existsByNameAndIdNot(updatedItem.getName(), id)) {
            throw new DuplicateKeyException("A food item with the same name already exists");
        }

        FoodItem item = foodItem.get();
        item.setName(updatedItem.getName());
        item.setKcal(updatedItem.getKcal());
        item.setCarbs(updatedItem.getCarbs());
        item.setProteins(updatedItem.getProteins());
        item.setFats(updatedItem.getFats());

        FoodItem saved = repo.save(item);
        MiniUpdate miniUpdate = new MiniUpdate (saved.getId(), saved.getName());
        messageTemplate.convertAndSend("/topic/food_items", miniUpdate);
        messageTemplate.convertAndSend("/topic/food_items/" + saved.getId(), saved);
        return saved;
    }

    /**
     * Searches a food item in the database by name
     * @param foodType - the String denoting the name of the FoodItem to be sent
     * @return FoodItem - returns the found FoodItem by name
     */
    public FoodItem searchByName(@RequestParam("name") String foodType) {
        return repo.findByName(foodType).orElse(null);
    }
}

