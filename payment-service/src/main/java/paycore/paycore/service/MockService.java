package paycore.paycore.service;

import org.springframework.stereotype.Service;
import paycore.paycore.common.UseCase;
import paycore.paycore.dto.MockServiceRequestDto;

import javax.net.ssl.SSLSession;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Optional;

@Service
public class MockService implements UseCase<MockService.Input, MockService.Output> {
    public record Input(
            MockServiceRequestDto mockServiceRequestDto
    ) {
    }

    public record Output(
            HttpResponse<String> httpResponse
    ) {
    }

    @Override
    public Output execute(Input input) {
        HttpResponse<String> httpResponse = new HttpResponse<>() {
            @Override
            public int statusCode() {
                return 200;
            }

            @Override
            public String body() {
                return "{\"ok\":true}";
            }

            @Override
            public HttpRequest request() {
                return null;
            }

            @Override
            public Optional<HttpResponse<String>> previousResponse() {
                return Optional.empty();
            }

            @Override
            public HttpHeaders headers() {
                return HttpHeaders.of(Map.of(), (a, b) -> true);
            }

            @Override
            public Optional<SSLSession> sslSession() {
                return Optional.empty();
            }

            @Override
            public URI uri() {
                return URI.create("https://mock.service.local");
            }

            @Override
            public HttpClient.Version version() {
                return HttpClient.Version.HTTP_1_1;
            }
        };

        return new Output(httpResponse);
    }
}