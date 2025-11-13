package paycore.paycore.application.usecase;

import paycore.paycore.common.UseCase;
import paycore.paycore.entity.SettlementEntity;

import java.util.UUID;

public interface FindSettlementUseCase extends UseCase<UUID, SettlementEntity> {
}

