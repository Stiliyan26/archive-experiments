package bg.latona.santa.selfie.service.sap.interfaces;

import bg.latona.santa.selfie.dtos.sapXMLShipment.DocumentDTO;
import bg.latona.santa.entities.selfie.ElectricityInvoice;

public interface SAPXMLService {

    DocumentDTO createDocumentDTO(ElectricityInvoice electricityInvoice);
}
