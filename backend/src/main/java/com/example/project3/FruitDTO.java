package com.example.project3;

import java.math.BigDecimal;

public class FruitDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private Unit unit;
    private Long categoryId;
    private String categoryName;
    private String imageFilename;
    private String imageUrl;

    public FruitDTO(Fruit fruit) {
        this.id = fruit.getId();
        this.name = fruit.getName();
        this.description = fruit.getDescription();
        this.price = fruit.getPrice();
        this.stock = fruit.getStock();
        this.unit = fruit.getUnit();
        this.categoryId = fruit.getCategory().getId();
        this.categoryName = fruit.getCategory().getName();
        this.imageFilename = fruit.getImageFilename();
        this.imageUrl = fruit.getImageUrl();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getImageFilename() {
        return imageFilename;
    }

    public void setImageFilename(String imageFilename) {
        this.imageFilename = imageFilename;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
} 