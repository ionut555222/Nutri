package com.example.project3;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {
    
    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private FruitRepository fruitRepository;

    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "project3-backend");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "project3-backend");
        
        // Check database connectivity
        boolean dbHealthy = checkDatabaseHealth();
        response.put("database", dbHealthy ? "UP" : "DOWN");
        
        // Check data availability
        try {
            long fruitCount = fruitRepository.count();
            response.put("dataCheck", Map.of(
                "status", "UP",
                "fruitCount", fruitCount
            ));
        } catch (Exception e) {
            response.put("dataCheck", Map.of(
                "status", "DOWN",
                "error", e.getMessage()
            ));
        }
        
        // Overall status
        String overallStatus = dbHealthy ? "UP" : "DOWN";
        response.put("status", overallStatus);
        
        return ResponseEntity.ok(response);
    }
    
    private boolean checkDatabaseHealth() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(5); // 5 second timeout
        } catch (SQLException e) {
            return false;
        }
    }
} 