package com.example.project3;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FruitRepository extends JpaRepository<Fruit, Long> {
    List<Fruit> findByCategoryId(Long categoryId);
} 