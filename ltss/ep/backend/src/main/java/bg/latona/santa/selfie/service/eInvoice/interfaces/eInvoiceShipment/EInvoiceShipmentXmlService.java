package bg.latona.santa.selfie.service.eInvoice.interfaces.eInvoiceShipment;

import bg.latona.santa.selfie.dtos.eInvoiceXMLShipment.InvoiceDataDTO;
import bg.latona.santa.entities.person.LegalPerson;

public interface EInvoiceShipmentXmlService {

    InvoiceDataDTO createInvoiceData(LegalPerson legalPerson);
}
