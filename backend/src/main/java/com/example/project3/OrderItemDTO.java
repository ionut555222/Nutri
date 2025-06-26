package com.example.project3;

import java.math.BigDecimal;

public class OrderItemDTO {
    private Long id;
    private int quantity;
    private BigDecimal price;
    private Long fruitId;
    private String fruitName;
    private String fruitDescription;
    private String categoryName;
    private String fruitImage;
    private BigDecimal subtotal;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Long getFruitId() {
        return fruitId;
    }

    public void setFruitId(Long fruitId) {
        this.fruitId = fruitId;
    }

    public String getFruitName() {
        return fruitName;
    }

    public void setFruitName(String fruitName) {
        this.fruitName = fruitName;
    }

    public String getFruitDescription() {
        return fruitDescription;
    }

    public void setFruitDescription(String fruitDescription) {
        this.fruitDescription = fruitDescription;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getFruitImage() {
        return fruitImage;
    }

    public void setFruitImage(String fruitImage) {
        this.fruitImage = fruitImage;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    // Utility method to calculate subtotal
    public BigDecimal calculateSubtotal() {
        if (price != null && quantity > 0) {
            return price.multiply(BigDecimal.valueOf(quantity));
        }
        return BigDecimal.ZERO;
    }
} 