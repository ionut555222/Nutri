package com.example.project3;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FruitServiceTest {

    @Mock
    private FruitRepository fruitRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private FruitService fruitService;

    private Category testCategory;
    private FruitData testFruitData;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Test Category");

        testFruitData = new FruitData();
        testFruitData.setName("Test Fruit");
        testFruitData.setDescription("A test fruit");
        testFruitData.setPrice(new BigDecimal("2.50"));
        testFruitData.setStock(10);
        testFruitData.setCategoryId(1L);
        testFruitData.setUnit(Unit.PIECE);
    }

    @Test
    void createFruit_Success() {
        // Arrange
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        
        Fruit savedFruit = new Fruit();
        savedFruit.setId(1L);
        savedFruit.setName("Test Fruit");
        savedFruit.setPrice(new BigDecimal("2.50"));
        savedFruit.setStock(10);
        savedFruit.setCategory(testCategory);
        
        when(fruitRepository.save(any(Fruit.class))).thenReturn(savedFruit);

        // Act
        FruitDTO result = fruitService.createFruit(testFruitData);

        // Assert
        assertNotNull(result);
        assertEquals("Test Fruit", result.getName());
        assertEquals(new BigDecimal("2.50"), result.getPrice());
        assertEquals(10, result.getStock());
        
        verify(fruitRepository).save(any(Fruit.class));
        verify(emailService).sendSimpleMessage(anyString(), anyString(), anyString());
    }

    @Test
    void createFruit_CategoryNotFound() {
        // Arrange
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            fruitService.createFruit(testFruitData);
        });
        
        verify(fruitRepository, never()).save(any(Fruit.class));
    }

    @Test
    void createFruit_InvalidName() {
        // Arrange
        testFruitData.setName("");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            fruitService.createFruit(testFruitData);
        });
        
        verify(fruitRepository, never()).save(any(Fruit.class));
    }

    @Test
    void createFruit_InvalidPrice() {
        // Arrange
        testFruitData.setPrice(new BigDecimal("-1.00"));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            fruitService.createFruit(testFruitData);
        });
        
        verify(fruitRepository, never()).save(any(Fruit.class));
    }

    @Test
    void createFruit_NegativeStock() {
        // Arrange
        testFruitData.setStock(-5);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            fruitService.createFruit(testFruitData);
        });
        
        verify(fruitRepository, never()).save(any(Fruit.class));
    }

    @Test
    void updateFruit_Success() {
        // Arrange
        Fruit existingFruit = new Fruit();
        existingFruit.setId(1L);
        existingFruit.setName("Old Name");
        
        when(fruitRepository.findById(1L)).thenReturn(Optional.of(existingFruit));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        
        Fruit updatedFruit = new Fruit();
        updatedFruit.setId(1L);
        updatedFruit.setName("Test Fruit");
        updatedFruit.setPrice(new BigDecimal("2.50"));
        updatedFruit.setStock(10);
        updatedFruit.setCategory(testCategory);
        
        when(fruitRepository.save(any(Fruit.class))).thenReturn(updatedFruit);

        // Act
        FruitDTO result = fruitService.updateFruit(1L, testFruitData);

        // Assert
        assertNotNull(result);
        assertEquals("Test Fruit", result.getName());
        verify(fruitRepository).save(any(Fruit.class));
    }

    @Test
    void updateFruit_NotFound() {
        // Arrange
        when(fruitRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            fruitService.updateFruit(1L, testFruitData);
        });
    }

    @Test
    void deleteFruit_Success() {
        // Arrange
        when(fruitRepository.existsById(1L)).thenReturn(true);

        // Act
        assertDoesNotThrow(() -> {
            fruitService.deleteFruit(1L);
        });

        // Assert
        verify(fruitRepository).deleteById(1L);
    }

    @Test
    void deleteFruit_NotFound() {
        // Arrange
        when(fruitRepository.existsById(1L)).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            fruitService.deleteFruit(1L);
        });
        
        verify(fruitRepository, never()).deleteById(any());
    }

    @Test
    void createFruit_EmailFailureDoesNotAffectOperation() {
        // Arrange
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        
        Fruit savedFruit = new Fruit();
        savedFruit.setId(1L);
        savedFruit.setName("Test Fruit");
        savedFruit.setPrice(new BigDecimal("2.50"));
        savedFruit.setStock(10);
        savedFruit.setCategory(testCategory);
        
        when(fruitRepository.save(any(Fruit.class))).thenReturn(savedFruit);
        doThrow(new RuntimeException("Email service down")).when(emailService)
            .sendSimpleMessage(anyString(), anyString(), anyString());

        // Act
        FruitDTO result = fruitService.createFruit(testFruitData);

        // Assert
        assertNotNull(result);
        assertEquals("Test Fruit", result.getName());
        verify(fruitRepository).save(any(Fruit.class));
    }
} 