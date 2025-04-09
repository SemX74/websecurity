package edu.pzks.security25.repository;

import edu.pzks.security25.model.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {
    // Custom query methods can be added here if needed
} 