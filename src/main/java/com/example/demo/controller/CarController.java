package com.example.demo.controller;

import com.example.demo.dto.car.CarDto;
import com.example.demo.dto.car.CreateCarDto;
import com.example.demo.dto.car.UpdateInventoryDto;
import com.example.demo.service.CarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Cars management", description = "Endpoints for management cars")
@RequiredArgsConstructor
@RestController
@RequestMapping("/cars")
@Validated
public class CarController {
    private final CarService carService;

    @PostMapping
    @Operation(summary = "Create car", description = "Add new car to DB")
    @PreAuthorize("hasAuthority('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public CarDto createCar(@Valid @RequestBody CreateCarDto createCarDto) {
        return carService.save(createCarDto);
    }

    @GetMapping
    @Operation(summary = "Get all cars", description = "Get a page of all available cars")
    public Page<CarDto> getAllCars(Pageable pageable) {
        return carService.findAll(pageable);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update car", description = "Update car by id")
    @PreAuthorize("hasAuthority('ADMIN')")
    public CarDto updateCarById(@PathVariable @Positive Long id,
                                @RequestBody @Valid CreateCarDto createCarDto) {
        return carService.update(createCarDto, id);
    }

    @PutMapping
    @Operation(summary = "Update inventory of car", description = "Update inventory of car by id")
    @PreAuthorize("hasAuthority('ADMIN')")
    public CarDto updateInventory(@Valid @RequestBody UpdateInventoryDto updateDto) {
        return carService.updateInventory(updateDto);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get car", description = "Get car by id")
    public CarDto getCarById(@PathVariable @Positive Long id) {
        return carService.findById(id);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete car", description = "Delete car by id")
    @PreAuthorize("hasAuthority('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCarById(@PathVariable @Positive Long id) {
        carService.delete(id);
    }
}
