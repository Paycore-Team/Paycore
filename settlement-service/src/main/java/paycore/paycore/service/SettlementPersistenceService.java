package paycore.paycore.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import paycore.paycore.application.usecase.FindSettlementUseCase;
import paycore.paycore.entity.SettlementEntity;
import paycore.paycore.repository.SettlementRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SettlementPersistenceService implements FindSettlementUseCase {

    private final SettlementRepository settlementRepository;

    @Override
    public SettlementEntity execute(UUID settlementId) {
        return settlementRepository.findById(settlementId)
                .orElseThrow(() -> new IllegalArgumentException("Settlement not found: " + settlementId));
    }
}

