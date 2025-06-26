package com.example.project3;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerId(Long customerId);
    List<Order> findByCustomerOrderByOrderDateDesc(Customer customer);
    List<Order> findAllByFulfilledFalse();
    List<Order> findAllByFulfilledTrue();
    List<Order> findByCustomerAndFulfilledTrueOrderByOrderDateDesc(Customer customer);
    List<Order> findByCustomerAndFulfilledFalseOrderByOrderDateDesc(Customer customer);
    List<Order> findByOrderDateAfter(LocalDateTime date);
    List<Order> findByCouponCode(String couponCode);
    
    default List<Order> findByUserId(Long userId) {
        return findByCustomerId(userId);
    }
    
    default List<Order> findByUserOrderByOrderDateDesc(User user) {
        return List.of();
    }
    
    default List<Order> findByUserAndFulfilledTrueOrderByOrderDateDesc(User user) {
        return List.of();
    }
    
    default List<Order> findByUserAndFulfilledFalseOrderByOrderDateDesc(User user) {
        return List.of();
    }
} 