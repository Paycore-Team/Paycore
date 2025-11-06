package paycore.paycore.dto;

import java.math.BigDecimal;

public record OrderRequestDto(Long userId, BigDecimal amount) {}
