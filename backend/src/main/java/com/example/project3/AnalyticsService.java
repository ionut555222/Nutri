package com.example.project3;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private FruitRepository fruitRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    // Dashboard Overview
    public Map<String, Object> getDashboardData() {
        Map<String, Object> dashboard = new HashMap<>();
        
        // Basic KPIs
        dashboard.put("totalRevenue", getTotalRevenue());
        dashboard.put("totalOrders", getTotalOrders());
        dashboard.put("totalCustomers", getTotalCustomers());
        dashboard.put("averageOrderValue", getAverageOrderValue());
        
        // Recent trends (last 30 days)
        dashboard.put("recentSales", getRecentSalesData());
        dashboard.put("topProducts", getTopSellingProducts());
        dashboard.put("customerSegments", getCustomerSegments());
        
        return dashboard;
    }

    // Sales Analytics
    public BigDecimal getTotalRevenue() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Long getTotalOrders() {
        return orderRepository.count();
    }

    public Long getTotalCustomers() {
        return customerRepository.count();
    }

    public BigDecimal getAverageOrderValue() {
        BigDecimal totalRevenue = getTotalRevenue();
        Long totalOrders = getTotalOrders();
        if (totalOrders == 0) return BigDecimal.ZERO;
        return totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, BigDecimal.ROUND_HALF_UP);
    }

    // Sales trend for last 30 days
    public List<Map<String, Object>> getRecentSalesData() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Order> recentOrders = orderRepository.findByOrderDateAfter(thirtyDaysAgo);
        
        // Group by date
        Map<LocalDate, List<Order>> ordersByDate = recentOrders.stream()
                .collect(Collectors.groupingBy(order -> order.getOrderDate().toLocalDate()));
        
        List<Map<String, Object>> salesData = new ArrayList<>();
        for (Map.Entry<LocalDate, List<Order>> entry : ordersByDate.entrySet()) {
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", entry.getKey().toString());
            dayData.put("orders", entry.getValue().size());
            dayData.put("revenue", entry.getValue().stream()
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            salesData.add(dayData);
        }
        
        // Sort by date
        salesData.sort((a, b) -> ((String) a.get("date")).compareTo((String) b.get("date")));
        return salesData;
    }

    // Product Analytics
    public List<Map<String, Object>> getTopSellingProducts() {
        List<OrderItem> allOrderItems = orderItemRepository.findAll();
        
        // Group by product and sum quantities
        Map<String, Integer> productSales = new HashMap<>();
        Map<String, BigDecimal> productRevenue = new HashMap<>();
        
        for (OrderItem item : allOrderItems) {
            String productName = item.getFruit().getName();
            productSales.merge(productName, item.getQuantity(), Integer::sum);
            productRevenue.merge(productName, 
                item.getFruit().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())), 
                BigDecimal::add);
        }
        
        List<Map<String, Object>> topProducts = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : productSales.entrySet()) {
            Map<String, Object> product = new HashMap<>();
            product.put("name", entry.getKey());
            product.put("quantitySold", entry.getValue());
            product.put("revenue", productRevenue.get(entry.getKey()));
            topProducts.add(product);
        }
        
        // Sort by quantity sold (descending)
        topProducts.sort((a, b) -> 
            Integer.compare((Integer) b.get("quantitySold"), (Integer) a.get("quantitySold")));
        
        // Return top 10
        return topProducts.stream().limit(10).collect(Collectors.toList());
    }

    // Customer Analytics
    public Map<String, Object> getCustomerSegments() {
        List<Customer> customers = customerRepository.findAll();
        Map<String, Object> segments = new HashMap<>();
        
        // Simple segmentation based on order count
        int newCustomers = 0;
        int regularCustomers = 0;
        int vipCustomers = 0;
        
        for (Customer customer : customers) {
            List<Order> customerOrders = orderRepository.findByCustomerId(customer.getId());
            int orderCount = customerOrders.size();
            
            if (orderCount == 0) {
                newCustomers++;
            } else if (orderCount <= 3) {
                regularCustomers++;
            } else {
                vipCustomers++;
            }
        }
        
        segments.put("new", newCustomers);
        segments.put("regular", regularCustomers);
        segments.put("vip", vipCustomers);
        segments.put("total", customers.size());
        
        return segments;
    }

    // Monthly sales report
    public List<Map<String, Object>> getMonthlySalesReport() {
        List<Order> orders = orderRepository.findAll();
        
        Map<String, List<Order>> ordersByMonth = orders.stream()
                .collect(Collectors.groupingBy(order -> 
                    order.getOrderDate().getYear() + "-" + 
                    String.format("%02d", order.getOrderDate().getMonthValue())));
        
        List<Map<String, Object>> monthlyData = new ArrayList<>();
        for (Map.Entry<String, List<Order>> entry : ordersByMonth.entrySet()) {
            Map<String, Object> monthData = new HashMap<>();
            monthData.put("month", entry.getKey());
            monthData.put("orders", entry.getValue().size());
            monthData.put("revenue", entry.getValue().stream()
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
            monthlyData.add(monthData);
        }
        
        // Sort by month
        monthlyData.sort((a, b) -> ((String) a.get("month")).compareTo((String) b.get("month")));
        return monthlyData;
    }

    // Category performance
    public List<Map<String, Object>> getCategoryPerformance() {
        List<OrderItem> allOrderItems = orderItemRepository.findAll();
        
        Map<String, Integer> categorySales = new HashMap<>();
        Map<String, BigDecimal> categoryRevenue = new HashMap<>();
        
        for (OrderItem item : allOrderItems) {
            String categoryName = item.getFruit().getCategory().getName();
            categorySales.merge(categoryName, item.getQuantity(), Integer::sum);
            categoryRevenue.merge(categoryName,
                item.getFruit().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())),
                BigDecimal::add);
        }
        
        List<Map<String, Object>> categoryData = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : categorySales.entrySet()) {
            Map<String, Object> category = new HashMap<>();
            category.put("name", entry.getKey());
            category.put("quantitySold", entry.getValue());
            category.put("revenue", categoryRevenue.get(entry.getKey()));
            categoryData.add(category);
        }
        
        // Sort by revenue (descending)
        categoryData.sort((a, b) -> 
            ((BigDecimal) b.get("revenue")).compareTo((BigDecimal) a.get("revenue")));
        
        return categoryData;
    }
} 