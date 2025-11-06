package paycore.paycore.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import paycore.paycore.application.usecase.FindOrderUseCase;
import paycore.paycore.application.usecase.PlaceOrderUseCase;
import paycore.paycore.dto.OrderRequestDto;
import paycore.paycore.entity.OrderEntity;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final PlaceOrderUseCase placeOrderUseCase;
    private final FindOrderUseCase findOrderUseCase;

    @PostMapping
    public ResponseEntity<OrderEntity> createOrder(@RequestBody OrderRequestDto dto) {
        return ResponseEntity.ok(placeOrderUseCase.execute(dto));
    }


    @GetMapping("/{orderId}")
    public ResponseEntity<OrderEntity> getOrder(@PathVariable UUID orderId) {
        OrderEntity order = findOrderUseCase.execute(orderId);
        return ResponseEntity.ok(order);
    }
}
