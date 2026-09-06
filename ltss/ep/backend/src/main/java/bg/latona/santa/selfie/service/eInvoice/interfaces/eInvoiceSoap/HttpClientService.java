package bg.latona.santa.selfie.service.eInvoice.interfaces.eInvoiceSoap;

import bg.latona.santa.selfie.service.eInvoice.helpers.HttpRequestParameters;

public interface HttpClientService {

    String sendHttpRequest(HttpRequestParameters soapRequestParameters);
}
