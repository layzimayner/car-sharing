package com.example.demo.service;

import com.example.demo.dto.car.CarDto;
import com.example.demo.dto.car.CreateCarDto;
import com.example.demo.dto.car.UpdateInventoryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CarService {
    CarDto save(CreateCarDto createCarDto);

    Page<CarDto> findAll(Pageable pageable);

    CarDto update(CreateCarDto createCarDto, Long id);

    CarDto updateInventory(UpdateInventoryDto updateDto);

    CarDto findById(Long id);

    void delete(Long id);
}
