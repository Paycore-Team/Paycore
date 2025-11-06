package paycore.paycore.application.usecase;

import paycore.paycore.common.UseCase;
import paycore.paycore.entity.OrderEntity;

import java.util.UUID;

public interface FindOrderUseCase extends UseCase<UUID, OrderEntity> {
}
