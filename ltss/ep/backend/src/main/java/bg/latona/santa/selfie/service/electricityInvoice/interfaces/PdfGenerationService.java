package bg.latona.santa.selfie.service.electricityInvoice.interfaces;


import bg.latona.santa.entities.DBFile;
import bg.latona.santa.entities.selfie.ElectricityInvoice;

import java.util.List;
import java.util.Map;


public interface PdfGenerationService {

    Map<String, List<String>> generatePdfDocuments();

    // Fill HTML template and Generate PDF file afterward
    DBFile generatePdfFile(
            ElectricityInvoice electricityInvoice,
            String date,
            String legalPersonIdentifier,
            String itn,
            String[] data,
            Map<String, List<String>> invalidElectricityInvoiceRows
    );
}
