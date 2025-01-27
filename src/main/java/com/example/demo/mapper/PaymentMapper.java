package com.example.demo.mapper;

import com.example.demo.config.MapperConfig;
import com.example.demo.dto.payment.PaymentDto;
import com.example.demo.dto.payment.PaymentRequestDto;
import com.example.demo.model.Payment;
import com.example.demo.model.Rental;
import java.math.BigDecimal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface PaymentMapper {
    @Mapping(target = "paymentId", source = "id")
    PaymentDto toDto(Payment payment);

    @Mapping(target = "rental", source = "rental")
    @Mapping(target = "type", source = "requestDto.type")
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "id", ignore = true)
    Payment toModel(Rental rental, PaymentRequestDto requestDto, BigDecimal total);
}
