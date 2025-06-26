package com.example.project3;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private ModelMapper modelMapper;

    @GetMapping("/categories")
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .collect(Collectors.toList());
    }
}