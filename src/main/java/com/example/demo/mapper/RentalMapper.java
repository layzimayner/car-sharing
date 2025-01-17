package com.example.demo.mapper;

import com.example.demo.config.MapperConfig;
import com.example.demo.dto.rental.RentalDto;
import com.example.demo.dto.rental.RentalRequestDto;
import com.example.demo.model.Car;
import com.example.demo.model.Rental;
import com.example.demo.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface RentalMapper {
    @Mapping(source = "user", target = "user")
    @Mapping(source = "car", target = "car")
    @Mapping(source = "requestDto.rentalDate", target = "rentalDate")
    @Mapping(source = "requestDto.returnDate", target = "returnDate")
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "id", ignore = true)
    Rental toModel(RentalRequestDto requestDto, User user, Car car);

    @Mapping(target = "rentalId", source = "rental.id")
    @Mapping(target = "userId", source = "rental.user.id")
    RentalDto toDto(Rental rental);
}
