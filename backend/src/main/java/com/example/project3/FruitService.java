package com.example.project3;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FruitService {

    @Autowired
    private FruitRepository fruitRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private EmailService emailService;

    public FruitDTO createFruit(FruitData fruitData) {
        validateFruitData(fruitData);
        
        Fruit fruit = new Fruit();
        fruit.setName(fruitData.getName());
        fruit.setDescription(fruitData.getDescription());
        fruit.setPrice(fruitData.getPrice());
        fruit.setStock(fruitData.getStock());
        
        if (fruitData.getUnit() != null) {
            fruit.setUnit(fruitData.getUnit());
        }
        
        Category category = categoryRepository.findById(fruitData.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + fruitData.getCategoryId()));
        fruit.setCategory(category);
        
        Fruit savedFruit = fruitRepository.save(fruit);
        
        // Send notification email asynchronously
        try {
            sendNewFruitNotification(savedFruit);
        } catch (Exception e) {
            // Log error but don't fail the operation
            // In a real app, this would use proper logging
            System.err.println("Failed to send notification email: " + e.getMessage());
        }
        
        return new FruitDTO(savedFruit);
    }

    public FruitDTO updateFruit(Long id, FruitData fruitData) {
        validateFruitData(fruitData);
        
        Fruit fruit = fruitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fruit not found with id: " + id));
        
        fruit.setName(fruitData.getName());
        fruit.setDescription(fruitData.getDescription());
        fruit.setPrice(fruitData.getPrice());
        fruit.setStock(fruitData.getStock());
        
        if (fruitData.getUnit() != null) {
            fruit.setUnit(fruitData.getUnit());
        }
        
        Category category = categoryRepository.findById(fruitData.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + fruitData.getCategoryId()));
        fruit.setCategory(category);
        
        Fruit updatedFruit = fruitRepository.save(fruit);
        
        return new FruitDTO(updatedFruit);
    }

    public void deleteFruit(Long id) {
        if (!fruitRepository.existsById(id)) {
            throw new IllegalArgumentException("Fruit not found with id: " + id);
        }
        fruitRepository.deleteById(id);
    }

    private void validateFruitData(FruitData fruitData) {
        if (fruitData.getName() == null || fruitData.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Fruit name cannot be empty");
        }
        if (fruitData.getPrice() == null || fruitData.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }
        if (fruitData.getStock() < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }
        if (fruitData.getCategoryId() == null) {
            throw new IllegalArgumentException("Category is required");
        }
    }

    private void sendNewFruitNotification(Fruit fruit) {
        // In a real application, get admin emails from configuration or database
        String adminEmail = "admin@example.com";
        String subject = "New Fruit Added to Store";
        String message = String.format("A new fruit has been added to the store:\n\n" +
                "Name: %s\n" +
                "Price: $%.2f\n" +
                "Stock: %d\n" +
                "Category: %s", 
                fruit.getName(), 
                fruit.getPrice(), 
                fruit.getStock(), 
                fruit.getCategory().getName());
        
        emailService.sendSimpleMessage(adminEmail, subject, message);
    }
} 