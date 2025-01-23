package com.example.demo.mapper;

import com.example.demo.config.MapperConfig;
import com.example.demo.dto.payment.PaymentDto;
import com.example.demo.dto.payment.PaymentRequestDto;
import com.example.demo.model.Payment;
import com.example.demo.model.Rental;
import com.stripe.model.checkout.Session;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(config = MapperConfig.class)
public interface PaymentMapper {
    PaymentDto toDto(Payment payment);

    @Mapping(target = "id", source = "rental.id")
    @Mapping(target = "rental", source = "rental")
    @Mapping(target = "type", source = "requestDto.type")
    @Mapping(target = "status", ignore = true)
    Payment toModel(Rental rental, PaymentRequestDto requestDto, BigDecimal total);
}
