package com.example.project3;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/fruits")
public class FruitController {

    @Autowired
    private FruitRepository fruitRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private FruitService fruitService;

    @GetMapping
    @Cacheable(value = "products", key = "#categoryId ?: 'all'")
    public ResponseEntity<List<FruitDTO>> getAllFruits(@RequestParam(required = false) Long categoryId) {
        try {
            List<Fruit> fruits;
            if (categoryId != null) {
                fruits = fruitRepository.findByCategoryId(categoryId);
            } else {
                fruits = fruitRepository.findAll();
            }
            
            List<FruitDTO> fruitDTOs = fruits.stream()
                    .map(FruitDTO::new)
                    .collect(Collectors.toList());
                    
            return ResponseEntity.ok(fruitDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EMPLOYEE')")
    @CacheEvict(value = "products", allEntries = true)
    public ResponseEntity<FruitDTO> addFruit(@Valid @RequestBody FruitData fruitData) {
        try {
            FruitDTO savedFruit = fruitService.createFruit(fruitData);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedFruit);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EMPLOYEE')")
    @CacheEvict(value = "products", allEntries = true)
    public ResponseEntity<FruitDTO> updateFruit(@PathVariable Long id, @Valid @RequestBody FruitData fruitData) {
        try {
            FruitDTO updatedFruit = fruitService.updateFruit(id, fruitData);
            return ResponseEntity.ok(updatedFruit);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_EMPLOYEE')")
    @CacheEvict(value = "products", allEntries = true)
    public ResponseEntity<Void> deleteFruit(@PathVariable Long id) {
        try {
            fruitService.deleteFruit(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/units")
    @Cacheable("units")
    public ResponseEntity<List<String>> getAllUnits() {
        List<String> units = Arrays.stream(Unit.values())
                .map(Unit::getDisplayName)
                .collect(Collectors.toList());
        return ResponseEntity.ok(units);
    }

    @GetMapping("/{id}")
    @Cacheable(value = "products", key = "#id")
    public ResponseEntity<FruitDTO> getFruitById(@PathVariable Long id) {
        try {
            Fruit fruit = fruitRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Fruit not found with id: " + id));
            return ResponseEntity.ok(new FruitDTO(fruit));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 