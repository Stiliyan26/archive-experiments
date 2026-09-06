package bg.latona.santa.selfie.interfaces;

import bg.latona.santa.entities.selfie.*;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.List;
import java.util.Map;


@FunctionalInterface
public interface FileProcessor {
    void process(
            int counter,
            List<ElectricityInvoice> electricityInvoices,
            Sheet sheet,
            Map<Integer, List<String>> invalidFileRowEntries,
            Map<String, AgreementType> agreementTypeMap,
            Map<Integer, LoiDocumentType> loiDocumentTypeMap,
            Map<String, LoiMeasurementUnit> loiMeasurementUnitMap
    );
}
