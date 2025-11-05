package paycore.paycore.service;

import org.springframework.stereotype.Service;
import paycore.paycore.usecase.MockUseCase;
import paycore.paycore.usecase.model.MockServiceRequest;
import paycore.paycore.usecase.model.MockServiceResponse;

import javax.net.ssl.SSLSession;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Optional;

@Service
public class MockService implements MockUseCase {
    /**
     * 주어진 요청에 대해 실제 I/O를 수행하지 않고 미리 정의된 모의 HTTP 응답을 생성하여 래핑된 응답을 반환합니다.
     *
     * 생성된 모의 응답은 HTTP 상태 코드 200, 본문 {"ok":true}, URI "https://mock.service.local" 및 HTTP/1.1 버전을 가집니다.
     *
     * @param input 모의 응답을 생성할 때 참조할 요청 정보
     * @return 생성된 모의 `HttpResponse<String>`을 포함하는 `MockServiceResponse`
     */
    @Override
    public MockServiceResponse execute(MockServiceRequest input) {
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

        return new MockServiceResponse(httpResponse);
    }
}