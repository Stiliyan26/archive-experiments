package bg.latona.santa.selfie.service.electricityInvoice.impls;


import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.nepal.PowerPlant;
import bg.latona.santa.entities.selfie.*;
import bg.latona.santa.repositories.*;
import bg.latona.santa.selfie.domain.RequestBody.PopulateElectricityInvoiceRequest;
import bg.latona.santa.selfie.service.common.interfaces.EntityPersistenceService;
import bg.latona.santa.selfie.service.electricityInvoice.interfaces.ImportFilesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static bg.latona.santa.selfie.constant.electricityInvoiceRelated.ElectricityInvoiceConstants.*;
import static bg.latona.santa.selfie.constant.electricityInvoiceRelated.ElectricityInvoiceMessages.ERROR_DELETING_FILES;
import static bg.latona.santa.selfie.constant.electricityInvoiceRelated.ElectricityInvoiceMessages.NO_PREVIOUS_INVOICE_ERROR;
import static bg.latona.santa.selfie.constant.electricityInvoiceRelated.ElectricityInvoiceRelatedEntitiesOptionsNames.*;
import static bg.latona.santa.selfie.util.ElectricityInvoiceUtils.formatPopulateElectricityInvoiceErrorMessageKey;


@Slf4j
@RequiredArgsConstructor
@Service
public class ImportFilesServiceImpl implements ImportFilesService {

    private final ElectricityInvoiceRepository electricityInvoiceRepository;

    private final ImportValueRepository importValueRepository;
    private final ImportQuantityRepository importQuantityRepository;
    private final ImportValueAndQuantityRepository importValueAndQuantityRepository;

    private final EntityPersistenceService entityPersistenceService;

    private final LoiAgreementStatusRepository loiAgreementStatusRepository;
    private final PowerPlantRepository powerPlantRepository;

    private final CrmValidator crmValidator;

    // Fetch and Populate Methods
//---------------------------------------------------------------------------------------------

    @Override
    @Transactional
    public Set<Map<String, Set<String>>> fetchAndPopulateImportValueEntries(
            LocalDate periodFrom,
            LocalDate periodTo,
            LocalDate taxEventDate,
            LoiDocumentType loiDocumentType,
            Map<Integer, LoiDocumentType> loiDocumentTypeMap,
            Set<String> agreementTypeCodes,
            Set<AgreementTypeMapping> agreementTypeMappingSet,
            Set<Long> powerPlantIds,
            ManagedCompany company
    ) {
        List<ImportValue> importValues = importValueRepository
                .findAllByElectricityInvoiceAndIsValidAndPeriodFromAndPeriodToAndLoiDocumentTypeAndAgreementType_CodeInAndPowerPlant_IdInAndDeletedAndCompany
                        (
                                null,
                                true,
                                periodFrom,
                                periodTo,
                                loiDocumentType,
                                agreementTypeCodes,
                                powerPlantIds,
                                false,
                                company
                        );

        return importValues
                .stream()
                .map(importValue ->
                        fillOrCreateElectricityInvoiceFromImportValueEntity(importValue, taxEventDate, loiDocumentTypeMap, agreementTypeMappingSet, company))
                .collect(Collectors.toSet());
    }


    @Override
    @Transactional
    public Set<Map<String, Set<String>>> fetchAndPopulateImportQuantityEntries(
            LocalDate periodFrom,
            LocalDate periodTo,
            LocalDate taxEventDate,
            LoiDocumentType loiDocumentType,
            Map<Integer, LoiDocumentType> loiDocumentTypeMap,
            Set<String> agreementTypeCodes,
            Set<AgreementTypeMapping> agreementTypeMappingSet,
            Set<Long> powerPlantIds,
            ManagedCompany company
    ) {
        List<ImportQuantity> importQuantities = importQuantityRepository
                .findAllByElectricityInvoiceAndIsValidAndPeriodFromAndPeriodToAndLoiDocumentTypeAndAgreementType_CodeInAndPowerPlant_IdInAndDeletedAndCompany(
                        null,
                        true,
                        periodFrom,
                        periodTo,
                        loiDocumentType,
                        agreementTypeCodes,
                        powerPlantIds,
                        false,
                        company
                );

        return importQuantities.stream()
                .map(importQuantity ->
                        fillOrCreateElectricityInvoiceFromImportQuantityEntity(importQuantity, taxEventDate, loiDocumentTypeMap, agreementTypeMappingSet, company))
                .collect(Collectors.toSet());
    }


    @Override
    @Transactional
    public Set<Map<String, Set<String>>> fetchAndPopulateImportValueAndQuantityEntries(
            LocalDate periodFrom,
            LocalDate periodTo,
            LocalDate taxEventDate,
            LoiDocumentType loiDocumentType,
            Map<Integer, LoiDocumentType> loiDocumentTypeMap,
            Set<String> agreementTypeCodes,
            Set<AgreementTypeMapping> agreementTypeMappingSet,
            Set<Long> powerPlantIds,
            ManagedCompany company
    ) {
        List<ImportValueAndQuantity> importValueAndQuantities = importValueAndQuantityRepository
                .findAllByElectricityInvoiceAndIsValidAndPeriodFromAndPeriodToAndLoiDocumentTypeAndAgreementType_CodeInAndPowerPlant_IdInAndDeletedAndCompany(
                        null,
                        true,
                        periodFrom,
                        periodTo,
                        loiDocumentType,
                        agreementTypeCodes,
                        powerPlantIds,
                        false,
                        company
                );

        return importValueAndQuantities.stream()
                .map(importValueAndQuantity ->
                        createElectricityInvoiceFromImportValueAndQuantityEntity(importValueAndQuantity, taxEventDate, loiDocumentTypeMap, agreementTypeMappingSet, company))
                .collect(Collectors.toSet());
    }


    @Override
    public void deleteGeneratedFiles() {
        Path folder = Paths.get(GENERATED_FILES_FOLDER_PATH);

        try {
            Files.walkFileTree(folder, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);

                    return FileVisitResult.CONTINUE;
                }
            });

        } catch (IOException e) {
            System.err.println(ERROR_DELETING_FILES + e.getMessage());
        }
    }


    // ForEach methods
//---------------------------------------------------------------------------------------------

    private Map<String, Set<String>> fillOrCreateElectricityInvoiceFromImportValueEntity(
            ImportValue importValue, LocalDate taxEventDate, Map<Integer, LoiDocumentType> loiDocumentTypeMap, Set<AgreementTypeMapping> agreementTypeMappingSet, ManagedCompany company
    ) {
        ElectricityInvoice electricityInvoice = null;
        Map<String, Set<String>> currentImportValueErrorMessages = new HashMap<>();

        Optional<ElectricityInvoice> optionalElectricityInvoice = electricityInvoiceRepository
                .findByReportingPointOwnAndPeriodFromAndPeriodToAndLoiDocumentTypeAndDeletedAndCompany(
                        importValue.getReportingPointOwn(),
                        importValue.getPeriodFrom(),
                        importValue.getPeriodTo(),
                        importValue.getLoiDocumentType(),
                        false,
                        company
                );

        boolean isVatIncluded = false;

        PowerPlant powerPlant = powerPlantRepository
                .findFirstByAccessPointAndCompanyAndDeleted(importValue.getReportingPointOwn(), company, false);
        boolean powerPlantHasAgreementsSelfInvoicing = false;

        if (powerPlant != null) {

            powerPlantHasAgreementsSelfInvoicing = powerPlant.getAgreementsSelfInvoicing().size() != 0 && powerPlant.getAgreementsSelfInvoicing()
                    .stream()
                    .anyMatch(agreement -> !agreement.isDeleted());

            isVatIncluded = powerPlantHasAgreementsSelfInvoicing
                    && powerPlant.getAgreementsSelfInvoicing().get(powerPlant.getAgreementsSelfInvoicing().size() - 1).getIsVatIncluded() != null
                    ? powerPlant.getAgreementsSelfInvoicing().get(powerPlant.getAgreementsSelfInvoicing().size() - 1).getIsVatIncluded()
                    : powerPlant.getOwner().getVatNumber() != null && !powerPlant.getOwner().getVatNumber().isEmpty();
        }

        if (optionalElectricityInvoice.isPresent()) { // Previous Entity exists -> Fill out the fields
            // Set the new Values
            optionalElectricityInvoice.get().setTaxEventDate(taxEventDate);
            optionalElectricityInvoice.get().setPriceInLevs(importValue.getPriceInLevs());
            optionalElectricityInvoice.get().setPriceInEuros(importValue.getPriceInLevs().divide(LEV_EURO_RATIO, 2, RoundingMode.HALF_UP));
            BigDecimal totalSumInLevs = optionalElectricityInvoice.get().getTotalQuantity().divide(importValue.getPriceInLevs(), 2, RoundingMode.HALF_UP);
            optionalElectricityInvoice.get().setTotalSumInLevs(totalSumInLevs);
            optionalElectricityInvoice.get().setTotalSumInEuros(totalSumInLevs.divide(LEV_EURO_RATIO, 2, RoundingMode.HALF_UP));
            BigDecimal vatInLevs = isVatIncluded ? totalSumInLevs.multiply(BigDecimal.valueOf(VAT_VALUE)) : BigDecimal.ZERO;
            optionalElectricityInvoice.get().setVatInLevs(vatInLevs);
            optionalElectricityInvoice.get().setVatInEuros(vatInLevs.divide(LEV_EURO_RATIO, 2, RoundingMode.HALF_UP));
            optionalElectricityInvoice.get().setIsValid(true);

            if ( // In case of loiDocumentType == Debit note || Credit note -> Check for an existing invoice
                    importValue.getLoiDocumentType() == loiDocumentTypeMap.get(LOI_DOCUMENT_DEBIT_NOTE) || importValue.getLoiDocumentType() == loiDocumentTypeMap.get(LOI_DOCUMENT_CREDIT_NOTE)
            ) {
                if (previousInvoiceDoesNotExist( // If there isn't an existing previous invoice -> Skip the entry and return error
                        importValue.getReportingPointOwn(),
                        importValue.getPeriodFrom(),
                        importValue.getPeriodTo(),
                        loiDocumentTypeMap,
                        optionalElectricityInvoice.get(),
                        company)
                ) {
                    currentImportValueErrorMessages.put(
                            formatPopulateElectricityInvoiceErrorMessageKey(
                                    importValue.getReportingPointOwn(),
                                    importValue.getPeriodFrom(),
                                    importValue.getPeriodTo(),
                                    importValue.getLoiDocumentType().getListOptionItemName()
                            ),
                            Collections.singleton(NO_PREVIOUS_INVOICE_ERROR)
                    );

                    return currentImportValueErrorMessages;
                }
            }

            Set<String> errors = crmValidator.validateElectricityInvoice(optionalElectricityInvoice.get(), agreementTypeMappingSet, company);

            if (!errors.isEmpty() /*&& !powerPlantHasAgreementsSelfInvoicing*/) { // There are Errors for this Import Value Entry
                errors
                        .forEach(errorMessage ->
                                currentImportValueErrorMessages.put(
                                        formatPopulateElectricityInvoiceErrorMessageKey(
                                                importValue.getReportingPointOwn(),
                                                importValue.getPeriodFrom(),
                                                importValue.getPeriodTo(),
                                                importValue.getLoiDocumentType().getListOptionItemName()
                                        ),
                                        Collections.singleton(errorMessage)
                                )
                        );

            } else { // Save the changes
                electricityInvoice = (ElectricityInvoice) entityPersistenceService
                        .persistEntity(optionalElectricityInvoice.get(), true, true);
                importValue.setElectricityInvoice(electricityInvoice);
            }

        } else {
            // Create the new Entity
            electricityInvoice = ElectricityInvoice
                    .builder()
                    .taxEventDate(taxEventDate)
                    .reportingPointOwn(importValue.getReportingPointOwn())
                    .loiDocumentType(importValue.getLoiDocumentType())
                    .agreementType(importValue.getAgreementType())
                    .priceInLevs(importValue.getPriceInLevs())
                    .priceInEuros(importValue.getPriceInLevs().divide(LEV_EURO_RATIO, 2, RoundingMode.HALF_UP))
                    .periodFrom(importValue.getPeriodFrom())
                    .periodTo(importValue.getPeriodTo())
                    .isValid(false)
                    .build();

            if ( // In case of loiDocumentType == Debit note || Credit note -> Check for an existing invoice
                    importValue.getLoiDocumentType() == loiDocumentTypeMap.get(LOI_DOCUMENT_DEBIT_NOTE) || importValue.getLoiDocumentType() == loiDocumentTypeMap.get(LOI_DOCUMENT_CREDIT_NOTE)
            ) {
                if (previousInvoiceDoesNotExist( // If there isn't an existing previous invoice -> Skip the entry and return error
                        importValue.getReportingPointOwn(),
                        importValue.getPeriodFrom(),
                        importValue.getPeriodTo(),
                        loiDocumentTypeMap,
                        electricityInvoice,
                        company)
                ) {
                    currentImportValueErrorMessages.put(
                            formatPopulateElectricityInvoiceErrorMessageKey(
                                    importValue.getReportingPointOwn(),
                                    importValue.getPeriodFrom(),
                                    importValue.getPeriodTo(),
                                    importValue.getLoiDocumentType().getListOptionItemName()
                            ),
                            Collections.singleton(NO_PREVIOUS_INVOICE_ERROR)
                    );

                    return currentImportValueErrorMessages;
                }
            }

            Set<String> errors = crmValidator.validateElectricityInvoice(electricityInvoice, agreementTypeMappingSet, company);

            if (!errors.isEmpty()) { // There are Errors for this Import Value Entry
                errors
                        .forEach(errorMessage ->
                                currentImportValueErrorMessages.put(
                                        formatPopulateElectricityInvoiceErrorMessageKey(
                                                importValue.getReportingPointOwn(),
                                                importValue.getPeriodFrom(),
                                                importValue.getPeriodTo(),
                                                importValue.getLoiDocumentType().getListOptionItemName()
                                        ),
                                        Collections.singleton(errorMessage)
                                )
                        );

            } else { // Save the new Entity
                electricityInvoice = (ElectricityInvoice) entityPersistenceService
                        .persistEntity(electricityInvoice, true, true);
                importValue.setElectricityInvoice(electricityInvoice);
            }
        }

        if (electricityInvoice == null)
            return currentImportValueErrorMessages;


        // Save the relationship
        entityPersistenceService.persistEntity(importValue, false, true);

        return currentImportValueErrorMessages;
    }


    private Map<String, Set<String>> fillOrCreateElectricityInvoiceFromImportQuantityEntity(
            ImportQuantity importQuantity, LocalDate taxEventDate, Map<Integer, LoiDocumentType> loiDocumentTypeMap, Set<AgreementTypeMapping> agreementTypeMappingSet, ManagedCompany company
    ) {
        ElectricityInvoice electricityInvoice = null;
        Map<String, Set<String>> currentImportQuantityErrorMessages = new HashMap<>();

        Optional<ElectricityInvoice> optionalElectricityInvoice = electricityInvoiceRepository
                .findByReportingPointOwnAndPeriodFromAndPeriodToAndLoiDocumentTypeAndDeletedAndCompany(
                        importQuantity.getReportingPointOwn(),
                        importQuantity.getPeriodFrom(),
                        importQuantity.getPeriodTo(),
                        importQuantity.getLoiDocumentType(),
                        false,
                        company
                );

        boolean isVatIncluded = false;

        PowerPlant powerPlant = powerPlantRepository
                .findFirstByAccessPointAndCompanyAndDeleted(importQuantity.getReportingPointOwn(), company, false);
        boolean powerPlantHasAgreementsSelfInvoicing = false;

        if (powerPlant != null) {

            powerPlantHasAgreementsSelfInvoicing = powerPlant.getAgreementsSelfInvoicing().size() != 0 && powerPlant.getAgreementsSelfInvoicing()
                    .stream()
                    .anyMatch(agreement -> !agreement.isDeleted());

            isVatIncluded = powerPlantHasAgreementsSelfInvoicing
                    && powerPlant.getAgreementsSelfInvoicing().get(powerPlant.getAgreementsSelfInvoicing().size() - 1).getIsVatIncluded() != null
                    ? powerPlant.getAgreementsSelfInvoicing().get(powerPlant.getAgreementsSelfInvoicing().size() - 1).getIsVatIncluded()
                    : powerPlant.getOwner().getVatNumber() != null && !powerPlant.getOwner().getVatNumber().isEmpty();
        }

        if (optionalElectricityInvoice.isPresent()) { // Check for a previous Entity
            // Set the new Values
            optionalElectricityInvoice.get().setTaxEventDate(taxEventDate);
            optionalElectricityInvoice.get().setTotalQuantity(importQuantity.getTotalQuantity());
            optionalElectricityInvoice.get().setLoiMeasurementUnit(importQuantity.getLoiMeasurementUnit());
            BigDecimal totalSumInLevs = importQuantity.getTotalQuantity().divide(optionalElectricityInvoice.get().getPriceInLevs(), 2, RoundingMode.HALF_UP);
            optionalElectricityInvoice.get().setTotalSumInLevs(totalSumInLevs);
            optionalElectricityInvoice.get().setTotalSumInEuros(totalSumInLevs.divide(LEV_EURO_RATIO, 2, RoundingMode.HALF_UP));
            BigDecimal vatInLevs = isVatIncluded ? totalSumInLevs.divide(BigDecimal.valueOf(VAT_VALUE)) : BigDecimal.ZERO;
            optionalElectricityInvoice.get().setVatInLevs(vatInLevs);
            optionalElectricityInvoice.get().setVatInEuros(vatInLevs.divide(LEV_EURO_RATIO, 2, RoundingMode.HALF_UP));
            optionalElectricityInvoice.get().setIsValid(true);

            if ( // In case of loiDocumentType == Debit note || Credit note -> Check for an existing invoice
                    importQuantity.getLoiDocumentType() == loiDocumentTypeMap.get(LOI_DOCUMENT_DEBIT_NOTE) || importQuantity.getLoiDocumentType() == loiDocumentTypeMap.get(LOI_DOCUMENT_CREDIT_NOTE)
            ) {
                if (previousInvoiceDoesNotExist( // If there isn't an existing previous invoice -> Skip the entry and return error
                        importQuantity.getReportingPointOwn(),
                        importQuantity.getPeriodFrom(),
                        importQuantity.getPeriodTo(),
                        loiDocumentTypeMap,
                        optionalElectricityInvoice.get(),
                        company)
                ) {
                    currentImportQuantityErrorMessages.put(
                            formatPopulateElectricityInvoiceErrorMessageKey(
                                    importQuantity.getReportingPointOwn(),
                                    importQuantity.getPeriodFrom(),
                                    importQuantity.getPeriodTo(),
                                    importQuantity.getLoiDocumentType().getListOptionItemName()
                            ),
                            Collections.singleton(NO_PREVIOUS_INVOICE_ERROR)
                    );

                    return currentImportQuantityErrorMessages;
                }
            }

            Set<String> errors = crmValidator.validateElectricityInvoice(optionalElectricityInvoice.get(), agreementTypeMappingSet, company);

            if (!errors.isEmpty() /*&& !powerPlantHasAgreementsSelfInvoicing*/) { // There are Errors for this Import Quantity Entry
                errors
                        .forEach(errorMessage ->
                                currentImportQuantityErrorMessages.put(
                                        formatPopulateElectricityInvoiceErrorMessageKey(
                                                importQuantity.getReportingPointOwn(),
                                                importQuantity.getPeriodFrom(),
                                                importQuantity.getPeriodTo(),
                                                importQuantity.getLoiDocumentType().getListOptionItemName()
                                        ),
                                        Collections.singleton(errorMessage)
                                )
                        );

            } else { // Save the changes
                electricityInvoice = (ElectricityInvoice) entityPersistenceService
                        .persistEntity(optionalElectricityInvoice.get(), true, true);
                importQuantity.setElectricityInvoice(electricityInvoice);
            }

        } else {
            // Create the new Entity
            electricityInvoice = ElectricityInvoice
                    .builder()
                    .taxEventDate(taxEventDate)
                    .reportingPointOwn(importQuantity.getReportingPointOwn())
                    .agreementType(importQuantity.getAgreementType())
                    .loiDocumentType(importQuantity.getLoiDocumentType())
                    .totalQuantity(importQuantity.getTotalQuantity())
                    .loiMeasurementUnit(importQuantity.getLoiMeasurementUnit())
                    .periodFrom(importQuantity.getPeriodFrom())
                    .periodTo(importQuantity.getPeriodTo())
                    .isValid(false)
                    .build();

            if ( // In case of loiDocumentType == Debit note || Credit note -> Check for an existing invoice
                    importQuantity.getLoiDocumentType() == loiDocumentTypeMap.get(LOI_DOCUMENT_DEBIT_NOTE) || importQuantity.getLoiDocumentType() == loiDocumentTypeMap.get(LOI_DOCUMENT_CREDIT_NOTE)
            ) {
                if (previousInvoiceDoesNotExist( // If there isn't an existing previous invoice -> Skip the entry and return error
                        importQuantity.getReportingPointOwn(),
                        importQuantity.getPeriodFrom(),
                        importQuantity.getPeriodTo(),
                        loiDocumentTypeMap,
                        electricityInvoice,
                        company)
                ) {
                    currentImportQuantityErrorMessages.put(
                            formatPopulateElectricityInvoiceErrorMessageKey(
                                    importQuantity.getReportingPointOwn(),
                                    importQuantity.getPeriodFrom(),
                                    importQuantity.getPeriodTo(),
                                    importQuantity.getLoiDocumentType().getListOptionItemName()
                            ),
                            Collections.singleton(NO_PREVIOUS_INVOICE_ERROR)
                    );

                    return currentImportQuantityErrorMessages;
                }
            }

            Set<String> errors = crmValidator.validateElectricityInvoice(electricityInvoice, agreementTypeMappingSet, company);

            if (!errors.isEmpty()) { // There are Errors for this Import Quantity Entry
                errors
                        .forEach(errorMessage ->
                                currentImportQuantityErrorMessages.put(
                                        formatPopulateElectricityInvoiceErrorMessageKey(
                                                importQuantity.getReportingPointOwn(),
                                                importQuantity.getPeriodFrom(),
                                                importQuantity.getPeriodTo(),
                                                importQuantity.getLoiDocumentType().getListOptionItemName()
                                        ),
                                        Collections.singleton(errorMessage)
                                )
                        );

            } else { // Save the new Entity
                electricityInvoice = (ElectricityInvoice) entityPersistenceService
                        .persistEntity(electricityInvoice, true, true);
                importQuantity.setElectricityInvoice(electricityInvoice);
            }
        }

        if (electricityInvoice == null)
            return currentImportQuantityErrorMessages;

        // Save the relationship
        entityPersistenceService.persistEntity(importQuantity, false, true);

        return currentImportQuantityErrorMessages;
    }


    private Map<String, Set<String>> createElectricityInvoiceFromImportValueAndQuantityEntity(
            ImportValueAndQuantity importValueAndQuantity, LocalDate taxEventDate, Map<Integer, LoiDocumentType> loiDocumentTypeMap, Set<AgreementTypeMapping> agreementTypeMappingSet, ManagedCompany company
    ) {
        boolean isVatIncluded = false;

        PowerPlant powerPlant = powerPlantRepository
                .findFirstByAccessPointAndCompanyAndDeleted(importValueAndQuantity.getReportingPointOwn(), company, false);
        boolean powerPlantHasAgreementsSelfInvoicing = false;

        Map<String, Set<String>> currentImportValueAndQuantityErrorMessages = new HashMap<>();
        if (powerPlant != null) {

            powerPlantHasAgreementsSelfInvoicing = powerPlant.getAgreementsSelfInvoicing().size() != 0 && powerPlant.getAgreementsSelfInvoicing()
                    .stream()
                    .anyMatch(agreement -> !agreement.isDeleted());

            isVatIncluded = powerPlantHasAgreementsSelfInvoicing
                    && powerPlant.getAgreementsSelfInvoicing().get(powerPlant.getAgreementsSelfInvoicing().size() - 1).getIsVatIncluded() != null
                    ? powerPlant.getAgreementsSelfInvoicing().get(powerPlant.getAgreementsSelfInvoicing().size() - 1).getIsVatIncluded()
                    : powerPlant.getOwner().getVatNumber() != null && !powerPlant.getOwner().getVatNumber().isEmpty();

        }

        // Create the entity
        BigDecimal vatInLevs = isVatIncluded ? importValueAndQuantity.getTotalSumInLevs().multiply(BigDecimal.valueOf(VAT_VALUE)) : BigDecimal.ZERO;
        ElectricityInvoice electricityInvoice = ElectricityInvoice
                .builder()
                .taxEventDate(taxEventDate)
                .reportingPointOwn(importValueAndQuantity.getReportingPointOwn())
                .loiDocumentType(importValueAndQuantity.getLoiDocumentType())
                .agreementType(importValueAndQuantity.getAgreementType())
                .loiMeasurementUnit(importValueAndQuantity.getLoiMeasurementUnit())
                .totalSumInLevs(importValueAndQuantity.getTotalSumInLevs())
                .totalSumInEuros(importValueAndQuantity.getTotalSumInLevs().divide(LEV_EURO_RATIO, 2, RoundingMode.HALF_UP))
                .vatInLevs(vatInLevs)
                .vatInEuros(vatInLevs.divide(LEV_EURO_RATIO, 2, RoundingMode.HALF_UP))
                .periodFrom(importValueAndQuantity.getPeriodFrom())
                .periodTo(importValueAndQuantity.getPeriodTo())
                .totalQuantity(importValueAndQuantity.getTotalQuantity())
                .priceInLevs(importValueAndQuantity.getPriceInLevs())
                .priceInEuros(importValueAndQuantity.getPriceInLevs().divide(LEV_EURO_RATIO, 2, RoundingMode.HALF_UP))
                .isValid(true)
                .build();


        if ( // In case of loiDocumentType == Debit note || Credit note -> Check for an existing invoice
                importValueAndQuantity.getLoiDocumentType() == loiDocumentTypeMap.get(LOI_DOCUMENT_DEBIT_NOTE) || importValueAndQuantity.getLoiDocumentType() == loiDocumentTypeMap.get(LOI_DOCUMENT_CREDIT_NOTE)
        ) {
            if (previousInvoiceDoesNotExist( // If there isn't an existing invoice -> Skip the entry and return error
                    importValueAndQuantity.getReportingPointOwn(),
                    importValueAndQuantity.getPeriodFrom(),
                    importValueAndQuantity.getPeriodTo(),
                    loiDocumentTypeMap,
                    electricityInvoice,
                    company)
            ) {
                currentImportValueAndQuantityErrorMessages.put(
                        formatPopulateElectricityInvoiceErrorMessageKey(
                                importValueAndQuantity.getReportingPointOwn(),
                                importValueAndQuantity.getPeriodFrom(),
                                importValueAndQuantity.getPeriodTo(),
                                importValueAndQuantity.getLoiDocumentType().getListOptionItemName()
                        ),
                        Collections.singleton(NO_PREVIOUS_INVOICE_ERROR)
                );

                return currentImportValueAndQuantityErrorMessages;
            }
        }

        Set<String> errors = crmValidator.validateElectricityInvoice(electricityInvoice, agreementTypeMappingSet, company);

        if (!errors.isEmpty() /*&& !powerPlantHasAgreementsSelfInvoicing*/) { // There are Errors for this Import Value and Quantity Entry
            errors
                    .forEach(errorMessage ->
                            currentImportValueAndQuantityErrorMessages.put(
                                    formatPopulateElectricityInvoiceErrorMessageKey(
                                            importValueAndQuantity.getReportingPointOwn(),
                                            importValueAndQuantity.getPeriodFrom(),
                                            importValueAndQuantity.getPeriodTo(),
                                            importValueAndQuantity.getLoiDocumentType().getListOptionItemName()
                                    ),
                                    Collections.singleton(errorMessage)
                            )
                    );

        } else { // Save the entity
            electricityInvoice = (ElectricityInvoice) entityPersistenceService
                    .persistEntity(electricityInvoice, true, true);
            importValueAndQuantity.setElectricityInvoice(electricityInvoice);
        }

        if (electricityInvoice == null)
            return currentImportValueAndQuantityErrorMessages;


        // Save the relationship
        entityPersistenceService.persistEntity(importValueAndQuantity, false, true);

        return currentImportValueAndQuantityErrorMessages;
    }


    // Helper methods
//---------------------------------------------------------------------------------------------

    private boolean previousInvoiceDoesNotExist(String reportingPointOwn, LocalDate periodFrom, LocalDate periodTo, Map<Integer, LoiDocumentType> loiDocumentTypeMap, ElectricityInvoice electricityInvoice, ManagedCompany company) {
        Optional<ElectricityInvoice> optionalElectricityInvoiceDocumentTypeInvoice = electricityInvoiceRepository
                .findFirstByReportingPointOwnAndPeriodFromAndPeriodToAndLoiDocumentTypeAndHasDbFileIsTrueAndDeletedAndCompany(
                        reportingPointOwn,
                        periodFrom,
                        periodTo,
                        loiDocumentTypeMap.get(LOI_DOCUMENT_TYPE_INVOICE),
                        false,
                        company
                );

        if (!optionalElectricityInvoiceDocumentTypeInvoice.isPresent()) {
            return true;
        }

        // set relationship with the invoice
        electricityInvoice.setElectricityInvoice(optionalElectricityInvoiceDocumentTypeInvoice.get());

        return false;
    }
}
