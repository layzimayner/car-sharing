package com.example.demo.service;

import com.example.demo.dto.rental.RentalDto;
import com.example.demo.dto.rental.RentalRequestDto;
import com.example.demo.dto.rental.ReturnDto;
import com.example.demo.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RentalService {
    RentalDto save(User user, RentalRequestDto requestDto);

    Page<RentalDto> findAll(Long userId, Pageable pageable, Boolean isActive);

    RentalDto findRental(Long userId, Long id);

    RentalDto returnCar(Long userId, ReturnDto returnDto, Long rentalId);
}
