package com.example.demo.mapper;

import com.example.demo.config.MapperConfig;
import com.example.demo.dto.car.CarDto;
import com.example.demo.dto.car.CreateCarDto;
import com.example.demo.model.Car;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class)
public interface CarMapper {
    Car toModel(CreateCarDto carDto);

    CarDto toDto(Car car);

    void updateCar(CreateCarDto carDto, @MappingTarget Car car);
}
