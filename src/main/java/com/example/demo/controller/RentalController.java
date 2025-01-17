package com.example.demo.controller;

import com.example.demo.dto.rental.RentalDto;
import com.example.demo.dto.rental.RentalRequestDto;
import com.example.demo.dto.rental.ReturnDto;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.service.RentalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Rentals management", description = "Endpoints for management rentals")
@RequiredArgsConstructor
@RestController
@RequestMapping("/rentals")
@Validated
public class RentalController {
    private final RentalService rentalService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Rent a car", description = "Save new rental to DB")
    public RentalDto rentCar(Authentication authentication,
                             @RequestBody @Valid RentalRequestDto requestDto) {
        User user = (User) authentication.getPrincipal();
        return rentalService.save(user, requestDto);
    }

    @GetMapping
    @Operation(summary = "Get your rentals", description = "Return page of user's rentals")
    public Page<RentalDto> getRentals(Authentication authentication, Pageable pageable,
                                      @RequestParam(value = "is_active",
                                              required = false) Boolean isActive,
                                      @RequestParam(value = "user_id",
                                              required = false) Long userId) {
        User curentUser = (User) authentication.getPrincipal();

        boolean isAdmin = curentUser.getRoles().stream()
                .anyMatch(role -> role.getName().equals(Role.RoleName.ADMIN));

        return rentalService.findAll(isAdmin ? userId : curentUser.getId(), pageable, isActive);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get specific rental", description = "Return rental based on id")
    public RentalDto getRentalById(Authentication authentication,
                                   @Positive @PathVariable Long id) {
        User user = (User) authentication.getPrincipal();
        return rentalService.findRental(user.getId(), id);
    }

    @PostMapping("/{id}")
    @Operation(summary = "Close the rental", description = "Set actual return date")
    public RentalDto returnCar(Authentication authentication,
                               @RequestBody @Valid ReturnDto returnDto,
                               @PathVariable @Positive Long id) {
        User user = (User) authentication.getPrincipal();
        return rentalService.returnCar(user.getId(), returnDto, id);
    }
}
