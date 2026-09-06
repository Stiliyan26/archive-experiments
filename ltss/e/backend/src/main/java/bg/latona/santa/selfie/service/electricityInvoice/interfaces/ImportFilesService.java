package bg.latona.santa.selfie.service.electricityInvoice.interfaces;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.selfie.AgreementTypeMapping;
import bg.latona.santa.entities.selfie.LoiDocumentType;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;


public interface ImportFilesService {

	Set<Map<String, Set<String>>> fetchAndPopulateImportValueEntries(LocalDate periodFrom, LocalDate periodTo, LocalDate taxEventDate, LoiDocumentType loiDocumentType, Map<Integer, LoiDocumentType> loiDocumentTypeMap, Set<String> agreementTypeCodes, Set<AgreementTypeMapping> agreementTypeMappingSet, Set<Long> powerPlantIds, ManagedCompany company);

	Set<Map<String, Set<String>>> fetchAndPopulateImportQuantityEntries(LocalDate periodFrom, LocalDate periodTo, LocalDate taxEventDate, LoiDocumentType loiDocumentType, Map<Integer, LoiDocumentType> loiDocumentTypeMap, Set<String> agreementTypeCodes, Set<AgreementTypeMapping> agreementTypeMappingSet, Set<Long> powerPlantIds, ManagedCompany company);

	Set<Map<String, Set<String>>> fetchAndPopulateImportValueAndQuantityEntries(LocalDate periodFrom, LocalDate periodTo, LocalDate taxEventDate, LoiDocumentType loiDocumentType, Map<Integer, LoiDocumentType> loiDocumentTypeMap, Set<String> agreementTypeCodes, Set<AgreementTypeMapping> agreementTypeMappingSet, Set<Long> powerPlantIds, ManagedCompany company);

    void deleteGeneratedFiles();
}
