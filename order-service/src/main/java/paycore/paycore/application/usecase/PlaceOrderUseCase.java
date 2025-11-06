package paycore.paycore.application.usecase;

import paycore.paycore.common.UseCase;
import paycore.paycore.dto.OrderRequestDto;
import paycore.paycore.entity.OrderEntity;

public interface PlaceOrderUseCase extends UseCase<OrderRequestDto, OrderEntity> {
}
