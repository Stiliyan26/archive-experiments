package bg.latona.santa.selfie.service.eInvoice.helpers;

import bg.latona.santa.selfie.exception.eInvoiceSoap.SoapClientException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class HttpResponseHandler {

    public ResponseBody getResponseBody(Response response) {
        if (response.isSuccessful()) {
            log.info("SOAP request successful: {}", response.message());
            assert response.body() != null;

            return response.body();
        } else {
            log.error("SOAP request failed: {} - {}", response.code(), response.message());
            throw new SoapClientException("SOAP request failed with status: " + response.code());
        }
    }

    public String getResponseBodyToString(Response response) throws IOException {
        return getResponseBody(response).string();
    }
}
