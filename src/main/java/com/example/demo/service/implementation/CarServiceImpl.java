package com.example.demo.service.implementation;

import com.example.demo.dto.car.CarDto;
import com.example.demo.dto.car.CreateCarDto;
import com.example.demo.dto.car.UpdateInventoryDto;
import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.mapper.CarMapper;
import com.example.demo.model.Car;
import com.example.demo.repository.CarRepository;
import com.example.demo.service.CarService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CarServiceImpl implements CarService {
    private final CarMapper carMapper;
    private final CarRepository carRepository;

    @Override
    public CarDto save(CreateCarDto createCarDto) {
        Car car = carMapper.toModel(createCarDto);
        return carMapper.toDto(carRepository.save(car));
    }

    @Override
    public Page<CarDto> findAll(Pageable pageable) {
        return carRepository.findAll(pageable)
                .map(carMapper::toDto);
    }

    @Override
    public CarDto update(CreateCarDto createCarDto, Long id) {
        Car car = carRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Can't update car with id "
                        + id + " because it does not exist")
                );
        carMapper.updateCar(createCarDto, car);
        carRepository.save(car);
        return carMapper.toDto(car);
    }

    @Override
    public CarDto updateInventory(UpdateInventoryDto updateDto) {
        Car car = carRepository.findById(updateDto.getCarId()).orElseThrow(() ->
                new EntityNotFoundException("Can't update car with id "
                        + updateDto.getCarId() + " because it does not exist")
        );
        car.setInventory(updateDto.getQuantity());
        carRepository.save(car);
        return carMapper.toDto(car);
    }

    @Override
    public CarDto findById(Long id) {
        Car car = carRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException("Can't find car with id "
                        + id + " because it does not exist")
        );
        return carMapper.toDto(car);
    }

    @Override
    public void delete(Long id) {
        if (!carRepository.existsById(id)) {
            throw new EntityNotFoundException("Can't delete car with id "
                    + id + " because it does not exist");
        }
        carRepository.deleteById(id);
    }
}
