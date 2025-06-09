package com.example.demo.service.implementation;

import com.example.demo.dto.rental.RentalDto;
import com.example.demo.dto.rental.RentalRequestDto;
import com.example.demo.dto.rental.ReturnDto;
import com.example.demo.exception.EntityNotFoundException;
import com.example.demo.exception.RentalProcessingException;
import com.example.demo.mapper.RentalMapper;
import com.example.demo.model.Car;
import com.example.demo.model.Rental;
import com.example.demo.model.User;
import com.example.demo.repository.CarRepository;
import com.example.demo.repository.RentalRepository;
import com.example.demo.service.NotificationService;
import com.example.demo.service.RentalService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RentalServiceImpl implements RentalService {
    private final RentalRepository rentalRepository;
    private final RentalMapper rentalMapper;
    private final CarRepository carRepository;
    private final NotificationService notificationService;
    @Value("${telegram.chat.id.member}")
    private String telegramChatIdMember;

    @Override
    @Transactional
    public RentalDto save(User user, RentalRequestDto requestDto) {
        Car car = carRepository.findById(requestDto.getCarId()).orElseThrow(() ->
                new EntityNotFoundException("Can't find car with id "
                        + requestDto.getCarId() + " because it does not exist")
        );
        if (car.getInventory() < 1) {
            throw new RentalProcessingException("Sorry, there are not enough free"
                    + " cars, choose another model");
        }
        car.setInventory(car.getInventory() - 1);
        carRepository.save(car);

        Rental rental = rentalMapper.toModel(requestDto, user, car);
        rentalRepository.save(rental);

        notificationService.sendRentalNotification(rental);

        return rentalMapper.toDto(rental);
    }

    @Override
    public Page<RentalDto> findAll(Long userId, Pageable pageable, Boolean isActive) {
        if (isActive) {
            return rentalRepository.findAllActive(pageable, userId)
                    .map(rentalMapper::toDto);
        }
        return rentalRepository.findAll(pageable, userId)
                .map(rentalMapper::toDto);
    }

    @Override
    public RentalDto findRental(Long userId, Long id) {
        Rental rental = rentalRepository.findByIdAndUserId(id, userId).orElseThrow(() ->
                new EntityNotFoundException("Rental not found or does not belong to the user")
        );
        return rentalMapper.toDto(rental);
    }

    @Override
    @Transactional
    public RentalDto returnCar(Long userId, ReturnDto returnDto, Long rentalId) {
        Rental rental = rentalRepository.findByIdAndUserId(rentalId, userId).orElseThrow(() ->
                new EntityNotFoundException("Rental not found or does not belong to the user")
        );
        if (rental.getActualReturnDate() != null) {
            throw new RentalProcessingException("The actual date is already set");
        }

        Car car = rental.getCar();
        car.setInventory(car.getInventory() + 1);
        carRepository.save(car);

        rental.setActualReturnDate(returnDto.actualReturnDate());
        rentalRepository.save(rental);

        return rentalMapper.toDto(rental);
    }
}
