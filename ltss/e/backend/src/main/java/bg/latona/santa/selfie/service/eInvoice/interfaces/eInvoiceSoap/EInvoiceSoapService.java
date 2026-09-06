package bg.latona.santa.selfie.service.eInvoice.interfaces.eInvoiceSoap;

import bg.latona.santa.selfie.domain.UploadZip;

public interface EInvoiceSoapService {

    String uploadFile(UploadZip uploadZip);

    String getFileResult(String fileTicket);
}
