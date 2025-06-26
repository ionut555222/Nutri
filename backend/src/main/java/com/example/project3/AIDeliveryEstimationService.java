package com.example.project3;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.math.BigDecimal;

@Service
public class AIDeliveryEstimationService {
    
    @Autowired
    private DeliveryZoneRepository deliveryZoneRepository;
    
    @Autowired
    private DeliveryAddressRepository deliveryAddressRepository;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private GeminiService geminiService;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    // Store location for distance calculations (configurable)
    private static final double STORE_LATITUDE = 44.4268; // Bucharest, Romania
    private static final double STORE_LONGITUDE = 26.1025;
    
    /**
     * Main method to estimate delivery dates for a customer address
     */
    public DeliveryEstimation estimateDeliveryDates(Long customerId, Long addressId, 
                                                   List<CartItemDTO> cartItems, 
                                                   DeliveryMethod preferredMethod) {
        try {
            // Get customer address
            DeliveryAddress address = deliveryAddressRepository.findById(addressId)
                    .orElseThrow(() -> new IllegalArgumentException("Address not found"));
            
            // Validate address belongs to customer
            if (!address.getCustomer().getId().equals(customerId)) {
                throw new IllegalArgumentException("Address does not belong to customer");
            }
            
            // Find appropriate delivery zone
            DeliveryZone zone = findBestDeliveryZone(address);
            if (zone == null) {
                throw new IllegalArgumentException("No delivery service available for this address");
            }
            
            // Calculate order characteristics
            OrderCharacteristics orderChars = analyzeOrderCharacteristics(cartItems);
            
            // Get current conditions
            DeliveryConditions conditions = getCurrentDeliveryConditions(address, zone);
            
            // Generate AI-powered delivery estimation
            return generateAIDeliveryEstimation(address, zone, orderChars, conditions, preferredMethod);
            
        } catch (Exception e) {
            // Fallback to basic estimation
            return generateFallbackEstimation(addressId, preferredMethod);
        }
    }
    
    /**
     * Find the best delivery zone for an address
     */
    private DeliveryZone findBestDeliveryZone(DeliveryAddress address) {
        // Try zip code first
        List<DeliveryZone> zones = deliveryZoneRepository.findZonesServingZipCode(address.getZipCode());
        
        if (zones.isEmpty()) {
            // Try city
            zones = deliveryZoneRepository.findZonesServingCity(address.getCity());
        }
        
        if (zones.isEmpty() && address.hasCoordinates()) {
            // Calculate distance to store and find zones within range
            double distance = calculateDistanceFromStore(address);
            zones = deliveryZoneRepository.findZonesWithinDistance(distance);
        }
        
        // Return the highest priority zone (lowest priority order number)
        return zones.isEmpty() ? null : zones.get(0);
    }
    
    /**
     * Analyze order characteristics that affect delivery
     */
    private OrderCharacteristics analyzeOrderCharacteristics(List<CartItemDTO> cartItems) {
        OrderCharacteristics chars = new OrderCharacteristics();
        
        chars.totalItems = cartItems.stream().mapToInt(CartItemDTO::getQuantity).sum();
        chars.totalWeight = estimateOrderWeight(cartItems);
        chars.totalVolume = estimateOrderVolume(cartItems);
        chars.hasPerishables = hasPerishableItems(cartItems);
        chars.hasFragileItems = hasFragileItems(cartItems);
        chars.requiresSpecialHandling = chars.hasPerishables || chars.hasFragileItems;
        
        // Complexity score (1-5)
        chars.complexityScore = calculateComplexityScore(chars);
        
        return chars;
    }
    
    /**
     * Get current delivery conditions (weather, traffic, capacity)
     */
    private DeliveryConditions getCurrentDeliveryConditions(DeliveryAddress address, DeliveryZone zone) {
        DeliveryConditions conditions = new DeliveryConditions();
        
        // Get weather conditions
        conditions.weatherData = getWeatherData(address);
        conditions.weatherImpactFactor = calculateWeatherImpact(conditions.weatherData, zone);
        
        // Get traffic conditions
        conditions.trafficFactor = getTrafficFactor(address);
        
        // Get current delivery capacity
        conditions.currentCapacity = getCurrentCapacityUtilization(zone);
        
        // Calculate seasonal factors
        conditions.seasonalFactor = getSeasonalFactor();
        
        // Day of week factor
        conditions.dayOfWeekFactor = getDayOfWeekFactor();
        
        return conditions;
    }
    
    /**
     * Generate AI-powered delivery estimation using Gemini
     */
    private DeliveryEstimation generateAIDeliveryEstimation(DeliveryAddress address, DeliveryZone zone,
                                                           OrderCharacteristics orderChars, 
                                                           DeliveryConditions conditions,
                                                           DeliveryMethod preferredMethod) {
        
        // Build context for AI
        String aiContext = buildAIContext(address, zone, orderChars, conditions, preferredMethod);
        
        // Get AI analysis - using mock response for now
        String aiResponse = getMockAIResponse(orderChars, conditions);
        
        // Parse AI response and generate estimation
        DeliveryEstimation estimation = parseAIResponse(aiResponse, zone, preferredMethod);
        
        // Apply business rules and validation
        return validateAndAdjustEstimation(estimation, zone, orderChars, conditions);
    }
    
    /**
     * Build context string for AI analysis
     */
    private String buildAIContext(DeliveryAddress address, DeliveryZone zone, 
                                 OrderCharacteristics orderChars, DeliveryConditions conditions,
                                 DeliveryMethod preferredMethod) {
        
        StringBuilder context = new StringBuilder();
        context.append("As a delivery logistics AI, estimate delivery dates for a fruit order.\n\n");
        
        context.append("DELIVERY ZONE: ").append(zone.getName()).append("\n");
        context.append("Base delivery time: ").append(zone.getBaseDeliveryHours()).append(" hours\n");
        context.append("Zone capacity: ").append(conditions.currentCapacity).append("%\n\n");
        
        context.append("DELIVERY ADDRESS:\n");
        context.append("City: ").append(address.getCity()).append("\n");
        context.append("Zip: ").append(address.getZipCode()).append("\n");
        context.append("Residential: ").append(address.getIsResidential()).append("\n");
        context.append("Difficulty score: ").append(address.getDeliveryDifficultyScore()).append("/5\n");
        if (address.getFloorNumber() != null) {
            context.append("Floor: ").append(address.getFloorNumber());
            context.append(", Elevator: ").append(address.getHasElevator()).append("\n");
        }
        context.append("\n");
        
        context.append("ORDER DETAILS:\n");
        context.append("Total items: ").append(orderChars.totalItems).append("\n");
        context.append("Estimated weight: ").append(orderChars.totalWeight).append(" kg\n");
        context.append("Has perishables: ").append(orderChars.hasPerishables).append("\n");
        context.append("Requires special handling: ").append(orderChars.requiresSpecialHandling).append("\n");
        context.append("Complexity score: ").append(orderChars.complexityScore).append("/5\n\n");
        
        context.append("CURRENT CONDITIONS:\n");
        context.append("Weather impact: ").append(conditions.weatherImpactFactor).append("x\n");
        context.append("Traffic factor: ").append(conditions.trafficFactor).append("x\n");
        context.append("Seasonal factor: ").append(conditions.seasonalFactor).append("x\n");
        context.append("Day factor: ").append(conditions.dayOfWeekFactor).append("x\n\n");
        
        context.append("PREFERRED METHOD: ").append(preferredMethod.getDisplayName()).append("\n\n");
        
        context.append("Please provide estimated delivery hours from now (number only).\n");
        context.append("Current time: ").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        return context.toString();
    }
    
    /**
     * Parse AI response into delivery estimation
     */
    private DeliveryEstimation parseAIResponse(String aiResponse, DeliveryZone zone, DeliveryMethod method) {
        DeliveryEstimation estimation = new DeliveryEstimation();
        
        // Extract estimated hours (with fallback)
        Integer estimatedHours = extractEstimatedHours(aiResponse, zone, method);
        
        // Calculate delivery dates
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime estimatedDelivery = now.plusHours(estimatedHours);
        
        // Adjust for business hours and delivery days
        estimatedDelivery = adjustForBusinessHours(estimatedDelivery, zone);
        
        estimation.estimatedDeliveryDate = estimatedDelivery.toLocalDate();
        estimation.estimatedDeliveryTime = estimatedDelivery.toLocalTime();
        
        // Generate delivery window options
        estimation.deliveryWindows = generateDeliveryWindows(estimatedDelivery, zone, method);
        
        // Set additional properties
        estimation.confidenceLevel = 8;
        estimation.riskFactors = Arrays.asList("Standard delivery conditions expected");
        estimation.recommendations = Arrays.asList("Order during off-peak hours for faster delivery");
        
        // Set costs
        estimation.deliveryCost = zone.getDeliveryCostForMethod(method);
        estimation.deliveryMethod = method;
        estimation.deliveryZone = zone;
        
        return estimation;
    }
    
    /**
     * Generate multiple delivery window options
     */
    private List<DeliveryWindow> generateDeliveryWindows(LocalDateTime baseTime, DeliveryZone zone, DeliveryMethod method) {
        List<DeliveryWindow> windows = new ArrayList<>();
        
        // Standard window (earliest)
        LocalDateTime standardTime = adjustForBusinessHours(baseTime, zone);
        windows.add(new DeliveryWindow(
                standardTime.toLocalDate(),
                standardTime.toLocalTime(),
                standardTime.toLocalTime().plusHours(2),
                zone.getDeliveryCostForMethod(DeliveryMethod.STANDARD),
                "Standard Delivery"
        ));
        
        // Express window (if available and different)
        if (method.isExpress()) {
            LocalDateTime expressTime = adjustForBusinessHours(baseTime.minusHours(4), zone);
            if (!expressTime.equals(standardTime)) {
                windows.add(new DeliveryWindow(
                        expressTime.toLocalDate(),
                        expressTime.toLocalTime(),
                        expressTime.toLocalTime().plusHours(1),
                        zone.getDeliveryCostForMethod(DeliveryMethod.EXPRESS),
                        "Express Delivery"
                ));
            }
        }
        
        // Next day options
        LocalDate nextDay = baseTime.toLocalDate().plusDays(1);
        for (int i = 0; i < 2 && nextDay.isBefore(baseTime.toLocalDate().plusDays(7)); i++) {
            if (zone.isDeliveryDay(nextDay.getDayOfWeek().getValue())) {
                LocalTime startTime = zone.getDeliveryStartTime() != null ? 
                        zone.getDeliveryStartTime() : LocalTime.of(9, 0);
                
                windows.add(new DeliveryWindow(
                        nextDay,
                        startTime,
                        startTime.plusHours(2),
                        zone.getDeliveryCostForMethod(DeliveryMethod.STANDARD),
                        "Next Day Delivery"
                ));
            }
            nextDay = nextDay.plusDays(1);
        }
        
        return windows;
    }
    
    /**
     * Validate and adjust estimation based on business rules
     */
    private DeliveryEstimation validateAndAdjustEstimation(DeliveryEstimation estimation, 
                                                          DeliveryZone zone,
                                                          OrderCharacteristics orderChars,
                                                          DeliveryConditions conditions) {
        
        // Ensure minimum delivery time
        LocalDateTime minDelivery = LocalDateTime.now().plusHours(zone.getMinDeliveryHours());
        LocalDateTime currentEstimate = LocalDateTime.of(estimation.estimatedDeliveryDate, estimation.estimatedDeliveryTime);
        
        if (currentEstimate.isBefore(minDelivery)) {
            minDelivery = adjustForBusinessHours(minDelivery, zone);
            estimation.estimatedDeliveryDate = minDelivery.toLocalDate();
            estimation.estimatedDeliveryTime = minDelivery.toLocalTime();
        }
        
        return estimation;
    }
    
    // Helper methods
    private double calculateDistanceFromStore(DeliveryAddress address) {
        if (!address.hasCoordinates()) return Double.MAX_VALUE;
        
        double lat1Rad = Math.toRadians(STORE_LATITUDE);
        double lat2Rad = Math.toRadians(address.getLatitude());
        double deltaLatRad = Math.toRadians(address.getLatitude() - STORE_LATITUDE);
        double deltaLonRad = Math.toRadians(address.getLongitude() - STORE_LONGITUDE);
        
        double a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLonRad / 2) * Math.sin(deltaLonRad / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 6371.0 * c; // Earth's radius in km
    }
    
    private LocalDateTime adjustForBusinessHours(LocalDateTime dateTime, DeliveryZone zone) {
        LocalTime startTime = zone.getDeliveryStartTime() != null ? zone.getDeliveryStartTime() : LocalTime.of(9, 0);
        LocalTime endTime = zone.getDeliveryEndTime() != null ? zone.getDeliveryEndTime() : LocalTime.of(18, 0);
        
        // If before business hours, move to start of business day
        if (dateTime.toLocalTime().isBefore(startTime)) {
            dateTime = dateTime.with(startTime);
        }
        
        // If after business hours, move to next business day
        if (dateTime.toLocalTime().isAfter(endTime)) {
            dateTime = dateTime.plusDays(1).with(startTime);
        }
        
        // Skip non-delivery days
        while (!zone.isDeliveryDay(dateTime.getDayOfWeek().getValue())) {
            dateTime = dateTime.plusDays(1).with(startTime);
        }
        
        return dateTime;
    }
    
    // Mock implementations for external services
    private Map<String, Object> getWeatherData(DeliveryAddress address) {
        Map<String, Object> weather = new HashMap<>();
        weather.put("condition", "clear");
        weather.put("temperature", 20);
        return weather;
    }
    
    private double calculateWeatherImpact(Map<String, Object> weather, DeliveryZone zone) {
        String condition = (String) weather.get("condition");
        double impact = 1.0;
        
        switch (condition.toLowerCase()) {
            case "rain": case "snow": impact = 1.3; break;
            case "storm": impact = 1.8; break;
            case "fog": impact = 1.2; break;
        }
        
        return impact * zone.getWeatherImpactFactor();
    }
    
    private double getTrafficFactor(DeliveryAddress address) {
        LocalTime now = LocalTime.now();
        if ((now.isAfter(LocalTime.of(7, 0)) && now.isBefore(LocalTime.of(9, 0))) ||
            (now.isAfter(LocalTime.of(17, 0)) && now.isBefore(LocalTime.of(19, 0)))) {
            return 1.4; // Rush hour
        }
        return 1.0;
    }
    
    private double getCurrentCapacityUtilization(DeliveryZone zone) {
        LocalDate today = LocalDate.now();
        // Mock capacity calculation
        return 60.0; // 60% capacity
    }
    
    private double getSeasonalFactor() {
        int month = LocalDate.now().getMonthValue();
        if (month >= 11 || month <= 2) return 1.2; // Winter
        if (month >= 6 && month <= 8) return 1.1; // Summer
        return 1.0; // Spring/Fall
    }
    
    private double getDayOfWeekFactor() {
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        switch (today) {
            case MONDAY: case FRIDAY: return 1.2; // Busy days
            case SATURDAY: case SUNDAY: return 0.9; // Lighter days
            default: return 1.0;
        }
    }
    
    // Estimation helper methods
    private double estimateOrderWeight(List<CartItemDTO> items) {
        return items.stream().mapToDouble(item -> item.getQuantity() * 0.5).sum(); // 0.5kg per item average
    }
    
    private double estimateOrderVolume(List<CartItemDTO> items) {
        return items.stream().mapToDouble(item -> item.getQuantity() * 0.01).sum(); // 0.01m³ per item average
    }
    
    private boolean hasPerishableItems(List<CartItemDTO> items) {
        return !items.isEmpty(); // All fruits are perishable
    }
    
    private boolean hasFragileItems(List<CartItemDTO> items) {
        // Mock fragile item detection - in production, would check fruit names from database
        return items.size() > 5; // Assume larger orders have more fragile items
    }
    
    private int calculateComplexityScore(OrderCharacteristics chars) {
        int score = 1;
        if (chars.totalItems > 10) score++;
        if (chars.totalWeight > 5) score++;
        if (chars.hasPerishables) score++;
        if (chars.hasFragileItems) score++;
        return Math.min(5, score);
    }
    
    // AI response parsing methods
    private Integer extractEstimatedHours(String response, DeliveryZone zone, DeliveryMethod method) {
        try {
            // Look for numbers in the response
            String[] words = response.split("\\s+");
            for (String word : words) {
                try {
                    int hours = Integer.parseInt(word.replaceAll("[^0-9]", ""));
                    if (hours > 0 && hours <= 168) { // Max 1 week
                        return hours;
                    }
                } catch (NumberFormatException ignored) {}
            }
        } catch (Exception ignored) {}
        
        // Fallback to zone defaults
        return zone.getDeliveryHoursForMethod(method);
    }
    
    private String getMockAIResponse(OrderCharacteristics orderChars, DeliveryConditions conditions) {
        // Mock AI response that returns estimated hours based on conditions
        int baseHours = 24;
        
        // Adjust based on conditions
        if (conditions.weatherImpactFactor > 1.2) baseHours += 6;
        if (conditions.trafficFactor > 1.2) baseHours += 2;
        if (conditions.currentCapacity > 80) baseHours += 4;
        if (orderChars.complexityScore > 3) baseHours += 2;
        
        return String.valueOf(baseHours);
    }
    
    private DeliveryEstimation generateFallbackEstimation(Long addressId, DeliveryMethod method) {
        DeliveryEstimation estimation = new DeliveryEstimation();
        
        int hours = method == DeliveryMethod.EXPRESS ? 12 : 
                   method == DeliveryMethod.PREMIUM ? 6 : 24;
        
        LocalDateTime deliveryTime = LocalDateTime.now().plusHours(hours);
        estimation.estimatedDeliveryDate = deliveryTime.toLocalDate();
        estimation.estimatedDeliveryTime = deliveryTime.toLocalTime();
        estimation.confidenceLevel = 5;
        estimation.riskFactors = Arrays.asList("Estimation based on standard delivery times");
        estimation.recommendations = Arrays.asList("Contact customer service for more accurate timing");
        estimation.deliveryCost = new BigDecimal("5.00");
        estimation.deliveryMethod = method;
        
        return estimation;
    }
    
    // Inner classes for data structures
    public static class OrderCharacteristics {
        public int totalItems;
        public double totalWeight;
        public double totalVolume;
        public boolean hasPerishables;
        public boolean hasFragileItems;
        public boolean requiresSpecialHandling;
        public int complexityScore;
    }
    
    public static class DeliveryConditions {
        public Map<String, Object> weatherData;
        public double weatherImpactFactor;
        public double trafficFactor;
        public double currentCapacity;
        public double seasonalFactor;
        public double dayOfWeekFactor;
    }
    
    public static class DeliveryEstimation {
        public LocalDate estimatedDeliveryDate;
        public LocalTime estimatedDeliveryTime;
        public List<DeliveryWindow> deliveryWindows = new ArrayList<>();
        public int confidenceLevel;
        public List<String> riskFactors = new ArrayList<>();
        public List<String> recommendations = new ArrayList<>();
        public BigDecimal deliveryCost;
        public DeliveryMethod deliveryMethod;
        public DeliveryZone deliveryZone;
    }
    
    public static class DeliveryWindow {
        public LocalDate date;
        public LocalTime startTime;
        public LocalTime endTime;
        public BigDecimal cost;
        public String description;
        
        public DeliveryWindow(LocalDate date, LocalTime startTime, LocalTime endTime, 
                             BigDecimal cost, String description) {
            this.date = date;
            this.startTime = startTime;
            this.endTime = endTime;
            this.cost = cost;
            this.description = description;
        }
    }
} 