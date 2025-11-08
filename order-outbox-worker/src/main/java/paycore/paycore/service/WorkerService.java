package paycore.paycore.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import paycore.paycore.usecase.WorkerUseCase;
import paycore.paycore.usecase.model.WorkerServiceRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkerService implements WorkerUseCase {
    @Override
    public Void execute(WorkerServiceRequest input) {

        return null;
    }
}