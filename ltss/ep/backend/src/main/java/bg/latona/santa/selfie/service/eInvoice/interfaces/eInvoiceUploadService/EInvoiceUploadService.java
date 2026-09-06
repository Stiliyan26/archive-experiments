package bg.latona.santa.selfie.service.eInvoice.interfaces.eInvoiceUploadService;

import java.util.Set;

public interface EInvoiceUploadService {

    String uploadZipFileEInvoice(Set<String> uniqueAccessPoints);
}
