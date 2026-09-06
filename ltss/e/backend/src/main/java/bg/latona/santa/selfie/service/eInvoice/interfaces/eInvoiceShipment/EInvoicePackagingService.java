package bg.latona.santa.selfie.service.eInvoice.interfaces.eInvoiceShipment;

import bg.latona.santa.selfie.domain.ZipEntryData;
import bg.latona.santa.selfie.domain.UploadZip;
import bg.latona.santa.selfie.dtos.eInvoiceXMLShipment.InvoiceDataDTO;

import java.util.List;

public interface EInvoicePackagingService {

    UploadZip createShipmentZip(InvoiceDataDTO invoiceData, List<ZipEntryData> attachments) throws Exception;
}
