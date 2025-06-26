package com.example.project3;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CartItemDTO {
    @JsonProperty("fruitId")
    private Long fruitId;
    
    @JsonProperty("productId") 
    private Long productId;
    
    private int quantity;

    // Getter that handles both field names
    public Long getFruitId() {
        return productId != null ? productId : fruitId;
    }

    public void setFruitId(Long fruitId) {
        this.fruitId = fruitId;
    }
    
    public Long getProductId() {
        return productId != null ? productId : fruitId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
} 