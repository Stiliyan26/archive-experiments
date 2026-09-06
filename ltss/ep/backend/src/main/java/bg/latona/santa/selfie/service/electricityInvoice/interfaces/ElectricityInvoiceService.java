package bg.latona.santa.selfie.service.electricityInvoice.interfaces;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;


public interface ElectricityInvoiceService {

    Map<Integer, List<String>> processValuesFile(Long dbFileId);

    Map<Integer, List<String>> processQuantitiesFile(Long dbFileId);

    Map<Integer, List<String>> processValuesAndQuantitiesFile(Long dbFileId);

	Set<Map<String, Set<String>>> populateElectricityInvoice(String periodFromStr, String periodToStr, String taxEventDateStr, String documentType);

    Map<String, List<String>> generatePdfDocuments();

	void createUpdatedElectricityInvoice(Long electricityInvoiceId, BigDecimal totalQuantity, BigDecimal priceInLevs, Long loiDocumentTypeId);
}
