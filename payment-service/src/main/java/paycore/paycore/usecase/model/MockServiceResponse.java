package paycore.paycore.usecase.model;

import java.net.http.HttpResponse;

public record MockServiceResponse(
        HttpResponse<String> httpResponse
) {
}
