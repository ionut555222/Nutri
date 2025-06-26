package com.example.project3;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryZoneRepository extends JpaRepository<DeliveryZone, Long> {
    
    // Find active zones ordered by priority
    List<DeliveryZone> findByIsActiveTrueOrderByPriorityOrderAsc();
    
    // Find zones that serve a specific zip code
    @Query("SELECT dz FROM DeliveryZone dz WHERE dz.isActive = true AND " +
           "(dz.zipCodes IS NULL OR dz.zipCodes LIKE %:zipCode%) " +
           "ORDER BY dz.priorityOrder ASC")
    List<DeliveryZone> findZonesServingZipCode(@Param("zipCode") String zipCode);
    
    // Find zones that serve a specific city
    @Query("SELECT dz FROM DeliveryZone dz WHERE dz.isActive = true AND " +
           "(dz.cityNames IS NULL OR LOWER(dz.cityNames) LIKE LOWER(CONCAT('%', :city, '%'))) " +
           "ORDER BY dz.priorityOrder ASC")
    List<DeliveryZone> findZonesServingCity(@Param("city") String city);
    
    // Find zones within a certain distance (requires coordinates)
    @Query("SELECT dz FROM DeliveryZone dz WHERE dz.isActive = true AND " +
           "dz.maxDistanceKm >= :distance ORDER BY dz.priorityOrder ASC")
    List<DeliveryZone> findZonesWithinDistance(@Param("distance") Double distance);
    
    // Find the best zone for an address (combination of zip code and city)
    @Query("SELECT dz FROM DeliveryZone dz WHERE dz.isActive = true AND " +
           "((dz.zipCodes IS NOT NULL AND dz.zipCodes LIKE %:zipCode%) OR " +
           "(dz.cityNames IS NOT NULL AND LOWER(dz.cityNames) LIKE LOWER(CONCAT('%', :city, '%')))) " +
           "ORDER BY dz.priorityOrder ASC")
    List<DeliveryZone> findBestZonesForAddress(@Param("zipCode") String zipCode, @Param("city") String city);
    
    // Find zones with available capacity for a specific day
    @Query("SELECT dz FROM DeliveryZone dz WHERE dz.isActive = true AND " +
           "dz.dailyCapacity > (SELECT COUNT(o) FROM Order o WHERE " +
           "CAST(o.deliveryDate AS date) = :deliveryDate AND o.deliveryZone = dz) " +
           "ORDER BY dz.priorityOrder ASC")
    List<DeliveryZone> findZonesWithCapacityForDate(@Param("deliveryDate") java.time.LocalDate deliveryDate);
    
    // Statistics and analytics queries
    @Query("SELECT dz.name, COUNT(o) as orderCount FROM DeliveryZone dz " +
           "LEFT JOIN Order o ON o.deliveryZone = dz " +
           "WHERE dz.isActive = true " +
           "GROUP BY dz.id, dz.name " +
           "ORDER BY orderCount DESC")
    List<Object[]> getZoneOrderStatistics();
    
    @Query("SELECT AVG(dz.baseDeliveryHours) FROM DeliveryZone dz WHERE dz.isActive = true")
    Double getAverageDeliveryHours();
    
    // Find zones that need capacity updates
    @Query("SELECT dz FROM DeliveryZone dz WHERE dz.isActive = true AND " +
           "dz.dailyCapacity <= (SELECT COUNT(o) FROM Order o WHERE " +
           "CAST(o.deliveryDate AS date) = CURRENT_DATE AND o.deliveryZone = dz)")
    List<DeliveryZone> findZonesAtCapacity();
} 