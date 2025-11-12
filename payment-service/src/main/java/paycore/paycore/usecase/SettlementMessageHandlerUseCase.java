package paycore.paycore.usecase;

public interface SettlementMessageHandlerUseCase<I, O> {
    O handle(I input);
}
