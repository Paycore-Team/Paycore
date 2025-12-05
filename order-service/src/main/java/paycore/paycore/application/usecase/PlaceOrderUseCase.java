package paycore.paycore.application.usecase;

import paycore.paycore.common.UseCase;
import paycore.paycore.domain.OrderStatus;
import paycore.paycore.dto.OrderRequestDto;

public interface PlaceOrderUseCase extends UseCase<OrderRequestDto, OrderStatus> {
}
