package bg.latona.santa.selfie.domain;


import bg.latona.santa.selfie.dtos.eInvoiceXMLShipment.InvoiceDataDTO;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UploadFileRequest {

    private InvoiceDataDTO invoiceData;

    private List<ZipEntryData> attachments = new ArrayList<>();
}
