package com.yann.smart_valuator_api.repository;

import com.yann.smart_valuator_api.entity.Estimation;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JPA repository for {@link com.yann.smart_valuator_api.entity.Estimation}.
 * Provides standard CRUD operations via JpaRepository.
 */
public interface EstimationRepository extends JpaRepository<Estimation, Long> {

    /**
     * Finds the first estimation matching the given item name (case-sensitive).
     * Currently unused — reserved for future filtering features.
     *
     * @param itemName the exact item name to search for
     * @return the matching Estimation, or null if none found
     */
    Estimation findByItemName(String itemName);
}
