package bg.latona.santa.selfie.service.sap.interfaces;

import bg.latona.santa.entities.selfie.ElectricityInvoice;


public interface SAPPackagingService {

    void writeSapXmlFile(ElectricityInvoice electricityInvoice);
}
