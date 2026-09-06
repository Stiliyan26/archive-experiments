package bg.latona.santa.selfie.service.eInvoice.impls.eInvoiceSoap;

import bg.latona.santa.selfie.exception.eInvoiceSoap.SoapClientException;
import bg.latona.santa.selfie.service.eInvoice.helpers.HttpRequestBuilder;
import bg.latona.santa.selfie.service.eInvoice.helpers.HttpRequestParameters;
import bg.latona.santa.selfie.service.eInvoice.helpers.HttpResponseHandler;
import bg.latona.santa.selfie.service.eInvoice.interfaces.eInvoiceSoap.HttpClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Service
public class HttpClientServiceImpl implements HttpClientService {

    private final OkHttpClient httpClient;
    private final HttpRequestBuilder httpRequestBuilder;
    private final HttpResponseHandler httpResponseHandler;

    public String sendHttpRequest(HttpRequestParameters httpRequestParameters) {
        Request request = httpRequestBuilder.buildRequest(httpRequestParameters);

        log.info("Sending SOAP request to URL: {}", httpRequestParameters.getUrl());

        return makeHttpCall(request);
    }

    private String makeHttpCall(Request request) {
        try (Response response = httpClient.newCall(request).execute()){
            return httpResponseHandler.getResponseBodyToString(response);
        } catch (IOException e) {
            log.error("Error during SOAP request", e);

            throw new SoapClientException("Error during SOAP request", e);
        }
    }
}
