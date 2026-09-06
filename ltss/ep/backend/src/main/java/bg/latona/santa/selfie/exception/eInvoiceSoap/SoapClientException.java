package bg.latona.santa.selfie.exception.eInvoiceSoap;

public class SoapClientException extends RuntimeException {

    public SoapClientException(String message) {
        super(message);
    }

    public SoapClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
