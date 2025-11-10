package paycore.paycore.application.usecase;

import paycore.paycore.common.UseCase;
import paycore.paycore.dto.SettlementRequestDto;
import paycore.paycore.entity.SettlementEntity;

public interface ProcessSettlementUseCase extends UseCase<SettlementRequestDto, SettlementEntity> {
}

