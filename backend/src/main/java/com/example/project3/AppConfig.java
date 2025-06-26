package com.example.project3;

import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

        // Mapping from Fruit to FruitDTO
        PropertyMap<Fruit, FruitDTO> fruitToDtoMap = new PropertyMap<Fruit, FruitDTO>() {
            protected void configure() {
                map().setCategoryId(source.getCategory().getId());
                map().setCategoryName(source.getCategory().getName());
            }
        };

        modelMapper.addMappings(fruitToDtoMap);

        // Mapping from OrderItem to OrderItemDTO
        PropertyMap<OrderItem, OrderItemDTO> orderItemToDtoMap = new PropertyMap<OrderItem, OrderItemDTO>() {
            protected void configure() {
                map().setFruitId(source.getFruit().getId());
                map().setFruitName(source.getFruit().getName());
                map().setFruitDescription(source.getFruit().getDescription());
                map().setCategoryName(source.getFruit().getCategory().getName());
                map().setFruitImage(source.getFruit().getImageUrl()); // Explicitly map imageUrl to fruitImage
                map().setPrice(source.getPrice());
            }
        };
        modelMapper.addMappings(orderItemToDtoMap);

        // Mapping from Order to OrderDTO
        PropertyMap<Order, OrderDTO> orderToDtoMap = new PropertyMap<Order, OrderDTO>() {
            protected void configure() {
                map().setUsername(source.getUser().getUsername());
            }
        };
        modelMapper.addMappings(orderToDtoMap);

        return modelMapper;
    }
} 