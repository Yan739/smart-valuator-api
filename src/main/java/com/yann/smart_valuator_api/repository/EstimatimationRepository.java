package com.yann.smart_valuator_api.repository;

import com.yann.smart_valuator_api.entity.Estimation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstimatimationRepository extends JpaRepository<Estimation, Long> {
    Estimation findByItemName(String itemName);
}
