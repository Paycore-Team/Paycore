package paycore.paycore.common;

public interface UseCase<I, O> {
    O execute(I input);

    class Exception extends RuntimeException {
        public Exception(String message) {
            super(message);
        }
    }
}

