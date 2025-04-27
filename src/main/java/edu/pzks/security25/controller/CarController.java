package edu.pzks.security25.controller;

import edu.pzks.security25.model.Car;
import edu.pzks.security25.service.CarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cars")
public class CarController {

    private final CarService carService;

    @Autowired
    public CarController(CarService carService) {
        this.carService = carService;
    }

    // CREATE - Add a new car
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Car> createCar(@RequestBody Car car) {
        Car savedCar = carService.saveCar(car);
        return new ResponseEntity<>(savedCar, HttpStatus.CREATED);
    }

    // READ - Get all cars
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<Car>> getAllCars() {
        List<Car> cars = carService.getAllCars();
        return new ResponseEntity<>(cars, HttpStatus.OK);
    }

    // READ - Get a car by id
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Car> getCarById(@PathVariable Long id) {
        Optional<Car> car = carService.getCarById(id);
        return car.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // UPDATE - Update an existing car
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Car> updateCar(@PathVariable Long id, @RequestBody Car carDetails) {
        Optional<Car> optionalCar = carService.getCarById(id);
        
        if (optionalCar.isPresent()) {
            Car car = optionalCar.get();
            car.setMake(carDetails.getMake());
            car.setModel(carDetails.getModel());
            car.setYear(carDetails.getYear());
            car.setColor(carDetails.getColor());
            car.setPrice(carDetails.getPrice());
            
            Car updatedCar = carService.saveCar(car);
            return new ResponseEntity<>(updatedCar, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // DELETE - Delete a car
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCar(@PathVariable Long id) {
        Optional<Car> car = carService.getCarById(id);
        
        if (car.isPresent()) {
            carService.deleteCar(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    // SUPERADMIN ONLY - System stats and administration
    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCars", carService.getAllCars().size());
        stats.put("systemStatus", "healthy");
        stats.put("adminAccess", true);
        stats.put("timestamp", System.currentTimeMillis());
        stats.put("message", "This endpoint is only accessible by SUPERADMIN role");
        
        return new ResponseEntity<>(stats, HttpStatus.OK);
    }
    
    // NEW ENDPOINT 7: Search cars by make
    @GetMapping("/search/make/{make}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<Car>> getCarsByMake(@PathVariable String make) {
        List<Car> cars = carService.getAllCars().stream()
                .filter(car -> car.getMake().equalsIgnoreCase(make))
                .collect(Collectors.toList());
        return new ResponseEntity<>(cars, HttpStatus.OK);
    }
    
    // NEW ENDPOINT 8: Get cars by price range
    @GetMapping("/search/price")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<Car>> getCarsByPriceRange(
            @RequestParam Double min, 
            @RequestParam Double max) {
        List<Car> cars = carService.getAllCars().stream()
                .filter(car -> car.getPrice() >= min && car.getPrice() <= max)
                .collect(Collectors.toList());
        return new ResponseEntity<>(cars, HttpStatus.OK);
    }
    
    // NEW ENDPOINT 9: Bulk delete cars (ADMIN only)
    @DeleteMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> bulkDeleteCars(@RequestBody List<Long> ids) {
        int deletedCount = 0;
        for (Long id : ids) {
            if (carService.getCarById(id).isPresent()) {
                carService.deleteCar(id);
                deletedCount++;
            }
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("deletedCount", deletedCount);
        response.put("message", "Bulk delete operation completed");
        
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
    // NEW ENDPOINT 10: Reset car price (ADMIN only)
    @PatchMapping("/{id}/reset-price")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Car> resetCarPrice(@PathVariable Long id, @RequestParam Double newPrice) {
        Optional<Car> optionalCar = carService.getCarById(id);
        
        if (optionalCar.isPresent()) {
            Car car = optionalCar.get();
            car.setPrice(newPrice);
            
            Car updatedCar = carService.saveCar(car);
            return new ResponseEntity<>(updatedCar, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
} 