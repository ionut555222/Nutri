package com.example.project3;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByCustomerOrderByTimestampAsc(Customer customer);
    
    // Backward compatibility method (deprecated)
    default List<ChatMessage> findByUserOrderByTimestampAsc(User user) {
        // Return empty list for backward compatibility - chat messages are now customer-specific
        return List.of();
    }
} 