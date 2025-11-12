package paycore.paycore.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import paycore.paycore.application.usecase.FindOrderUseCase;
import paycore.paycore.entity.OrderEntity;
import paycore.paycore.repository.OrderRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderPersistenceService implements FindOrderUseCase {

    private final OrderRepository orderRepository;

    @Override
    public OrderEntity execute(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
    }
}
