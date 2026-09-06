package bg.latona.santa.selfie.service.electricityInvoice.impls;

import bg.latona.santa.entities.nepal.PowerPlant;
import bg.latona.santa.selfie.enums.ElectricityInvoiceColumnNamesEnumEn;
import bg.latona.santa.selfie.interfaces.FileProcessor;
import bg.latona.santa.selfie.service.common.interfaces.EntityPersistenceService;
import bg.latona.santa.selfie.service.common.interfaces.entityRelated.DBFileService;
import bg.latona.santa.selfie.service.common.interfaces.entityRelated.DbSecUserService;
import bg.latona.santa.selfie.service.electricityInvoice.interfaces.ElectricityInvoiceService;
import bg.latona.santa.selfie.service.electricityInvoice.interfaces.ImportFilesService;
import bg.latona.santa.selfie.service.electricityInvoice.interfaces.PdfGenerationService;
import bg.latona.santa.entities.DBFile;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.selfie.*;
import bg.latona.santa.reports.ReportException;
import bg.latona.santa.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static bg.latona.santa.selfie.constant.electricityInvoiceRelated.ElectricityInvoiceMessages.*;
import static bg.latona.santa.selfie.util.ElectricityInvoiceUtils.*;


@Slf4j
@RequiredArgsConstructor
@Service
public class ElectricityInvoiceServiceImpl implements ElectricityInvoiceService {
    private final PowerPlantRepository powerPlantRepository;
    private final ElectricityInvoiceRepository electricityInvoiceRepository;

    private final AgreementTypeRepository agreementTypeRepository;
    private final LoiMeasurementUnitRepository loiMeasurementUnitRepository;
    private final LoiDocumentTypeRepository loiDocumentTypeRepository;

    private final DbSecUserService dbSecUserService;
    private final DBFileService dbFileService;

    private final EntityPersistenceService entityPersistenceService;
    private final ImportFilesService importFilesService;
    private final PdfGenerationService pdfGenerationService;
    private final PdfGenerationServiceImpl pdfGenerationServiceImpl;
    private final ImportValueRepository importValueRepository;
    private final ImportQuantityRepository importQuantityRepository;
    private final ImportValueAndQuantityRepository importValueAndQuantityRepository;
    private final AgreementTypeMappingRepository agreementTypeMappingRepository;


    // Public Methods
//---------------------------------------------------------------------------------------------


    // First File
    @Override
    public Map<Integer, List<String>> processValuesFile(Long dbFileId) {
        return abstractElectricityInvoiceFileHandler(dbFileId, this::importValuesFile);
    }


    // Second File
    @Override
    public Map<Integer, List<String>> processQuantitiesFile(Long dbFileId) {
        return abstractElectricityInvoiceFileHandler(dbFileId, this::importQuantitiesFile);
    }


    // Third File
    @Override
    public Map<Integer, List<String>> processValuesAndQuantitiesFile(Long dbFileId) {
        return abstractElectricityInvoiceFileHandler(dbFileId, this::importValuesAndQuantitiesFile);
    }


    // Add the data from the three subEntities to ElectricityInvoice
    @Override
    public Set<Map<String, Set<String>>> populateElectricityInvoice(
            String periodFromStr, String periodToStr, String taxEventDateStr, String documentTypeStr
    ) {
        ManagedCompany company = dbSecUserService.getCurrentUserManagedCompany();

        // Parse the Data coming from the GET request URL
        LocalDate periodFrom = parseDateFromStringHyphenFormat(periodFromStr);
        LocalDate periodTo = parseDateFromStringHyphenFormat(periodToStr);
        LocalDate taxEventDate = parseDateFromStringHyphenFormat(taxEventDateStr);
        Map<Integer, LoiDocumentType> loiDocumentTypeMap = getLoiDocumentTypeMap(company, loiDocumentTypeRepository);
        LoiDocumentType loiDocumentType = parseLoiDocumentTypeFromStringUrl(documentTypeStr, loiDocumentTypeMap);

        Set<AgreementTypeMapping> agreementTypeMappingSet = agreementTypeMappingRepository.findByCompanyAndDeleted(company, false);

        Set<Map<String, Set<String>>> errorsResult = new HashSet<>();

        Set<Map<String, Set<String>>> fetchAndPopulateImportValueEntries = importFilesService.fetchAndPopulateImportValueEntries(periodFrom, periodTo, taxEventDate, loiDocumentType, loiDocumentTypeMap, agreementTypeMappingSet, company);
        Set<Map<String, Set<String>>> fetchAndPopulateImportQuantityEntries = importFilesService.fetchAndPopulateImportQuantityEntries(periodFrom, periodTo, taxEventDate, loiDocumentType, loiDocumentTypeMap, agreementTypeMappingSet, company);
        Set<Map<String, Set<String>>> fetchAndPopulateImportValueAndQuantityEntries = importFilesService.fetchAndPopulateImportValueAndQuantityEntries(periodFrom, periodTo, taxEventDate, loiDocumentType, loiDocumentTypeMap, agreementTypeMappingSet, company);

        errorsResult.addAll(fetchAndPopulateImportValueEntries);
        errorsResult.addAll(fetchAndPopulateImportQuantityEntries);
        errorsResult.addAll(fetchAndPopulateImportValueAndQuantityEntries);

        return errorsResult;
    }


    @Override
    public Map<String, List<String>> generatePdfDocuments() {
        return pdfGenerationService.generatePdfDocuments();
    }


    @Override
    public void createUpdatedElectricityInvoice(Long electricityInvoiceId, BigDecimal totalQuantity, BigDecimal priceInLevs, Long loiDocumentTypeId) {

        ManagedCompany managedCompany = dbSecUserService.getCurrentUserManagedCompany();

        ElectricityInvoice electricityInvoice = electricityInvoiceRepository
                .findFirstByIdAndCompanyAndDeleted(electricityInvoiceId, managedCompany, false);

		PowerPlant powerPlant = powerPlantRepository.findFirstByAccessPointAndCompanyAndDeleted(electricityInvoice.getReportingPointOwn(), managedCompany, false);
		boolean isVatIncluded = powerPlant.getAgreementsSelfInvoicing() != null
				&& powerPlant.getAgreementsSelfInvoicing().get(powerPlant.getAgreementsSelfInvoicing().size() - 1).getIsVatIncluded() != null
				? powerPlant.getAgreementsSelfInvoicing().get(powerPlant.getAgreementsSelfInvoicing().size() - 1).getIsVatIncluded()
				: false;

		BigDecimal totalSumInLevs = priceInLevs.divide(totalQuantity, 2, BigDecimal.ROUND_HALF_UP);
		BigDecimal totalSumInEuro = totalSumInLevs.divide(BigDecimal.valueOf(1.96), 2, BigDecimal.ROUND_HALF_UP);
		BigDecimal priceInEuro = priceInLevs.divide(BigDecimal.valueOf(1.96), 2, BigDecimal.ROUND_HALF_UP);

		BigDecimal vatInLevs = isVatIncluded ?  totalSumInLevs.multiply(BigDecimal.valueOf(0.2)) : BigDecimal.ZERO;
		BigDecimal vatInEuro = isVatIncluded ?  totalSumInEuro.multiply(BigDecimal.valueOf(0.2)) : BigDecimal.ZERO;


        ElectricityInvoice newElectricityInvoice = new ElectricityInvoice();

        newElectricityInvoice.setTaxEventDate(LocalDate.now());
        newElectricityInvoice.setReportingPointOwn(electricityInvoice.getReportingPointOwn());
        newElectricityInvoice.setPeriodFrom(electricityInvoice.getPeriodFrom());
        newElectricityInvoice.setPeriodTo(electricityInvoice.getPeriodTo());

        newElectricityInvoice.setTotalQuantity(totalQuantity);
        newElectricityInvoice.setPriceInLevs(priceInLevs);

        newElectricityInvoice.setPriceInEuros(priceInEuro);
        newElectricityInvoice.setVatInLevs(vatInLevs);
        newElectricityInvoice.setTotalSumInLevs(totalSumInLevs);
        newElectricityInvoice.setVatInEuros(vatInEuro);
        newElectricityInvoice.setTotalSumInEuros(totalSumInEuro);
        newElectricityInvoice.setIsValid(electricityInvoice.getIsValid());

        newElectricityInvoice.setAgreementType(electricityInvoice.getAgreementType());
        newElectricityInvoice.setLoiMeasurementUnit(electricityInvoice.getLoiMeasurementUnit());

        LoiDocumentType loiDocumentType = loiDocumentTypeRepository
                .findFirstByListOptionItemCodeAndCompanyAndDeleted(loiDocumentTypeId, managedCompany, false);
        newElectricityInvoice.setLoiDocumentType(loiDocumentType);

        newElectricityInvoice.setElectricityInvoice(electricityInvoice);

        entityPersistenceService.persistEntity(newElectricityInvoice, true, true);
        generatePdfDocuments();
    }


    // Abstract Logic for the work with Excel file documents
//---------------------------------------------------------------------------------------------

    private Map<Integer, List<String>> abstractElectricityInvoiceFileHandler(Long dbFileId, FileProcessor fileProcessor) {
        DBFile dbFile = dbFileService.getDBFileById(dbFileId);

        if (dbFile == null) // if the File is null
            throw new ReportException(DB_FILE_MISSING_MESSAGE);


        Map<Integer, List<String>> invalidFileRowEntries = new LinkedHashMap<>();
        List<ElectricityInvoice> electricityInvoices = new ArrayList<>();


        // Validate file extension
        if (isFileExtensionInvalid(dbFile, invalidFileRowEntries))
            return invalidFileRowEntries;


        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(dbFile.getContent()))) {
            Sheet sheet = workbook.getSheetAt(0);

            int counter = -1;


            // Fetching the relationships beforehand
            ManagedCompany company = dbSecUserService.getCurrentUserManagedCompany();

            Map<String, AgreementType> agreementTypeMap = getAgreementTypeMap(company, agreementTypeRepository);
            Map<Integer, LoiDocumentType> loiDocumentTypeMap = getLoiDocumentTypeMap(company, loiDocumentTypeRepository);
            Map<String, LoiMeasurementUnit> loiMeasurementUnitMap = getLoiMeasurementUnitMap(company, loiMeasurementUnitRepository);


            fileProcessor.process(
                    counter,
                    electricityInvoices,
                    sheet,
                    invalidFileRowEntries,
                    agreementTypeMap,
                    loiDocumentTypeMap,
                    loiMeasurementUnitMap
            ); // callback function for each of the different Excel files

        } catch (Exception e) {
            invalidFileRowEntries.put(0, Arrays.asList(VALIDATIONS_DID_NOT_WORK_CORRECTLY));
            e.printStackTrace();
        }

        return invalidFileRowEntries; // If all rows are with valid data: an empty Map will be returned
    }


    // Custom Logic for each of the Excel file documents
//---------------------------------------------------------------------------------------------

    // First File
    public void importValuesFile(
            int counter,
            List<ElectricityInvoice> electricityInvoices,
            Sheet sheet,
            Map<Integer, List<String>> invalidFileRowEntries,
            Map<String, AgreementType> agreementTypeMap,
            Map<Integer, LoiDocumentType> loiDocumentTypeMap,
            Map<String, LoiMeasurementUnit> loiMeasurementUnitMap
    ) {
        for (Row row : sheet) {
            counter++;

            // Validate file columns
            if (counter == 0) {
                if (areFileColumnsInvalid(row, 1, invalidFileRowEntries))
                    return;

            } else {
                List<String> currentExcelRowErrorMessages = new ArrayList<>();

                ElectricityInvoice electricityInvoice = new ElectricityInvoice();

                //---------------------------------------------------
                // Reporting Point OWN | String | ТО ЕВН | Meter Point
                parseAndValidateStringValue(
                        row.getCell(0),
                        ElectricityInvoiceColumnNamesEnumEn.REPORTING_POINT_OWN.getValue(),
                        currentExcelRowErrorMessages,
                        electricityInvoice
                );
                //---------------------------------------------------
                // Document Type | Relationship | Вид на документа | Invoice Type
                parseAndValidateLoiDocumentTypeValue(
                        row.getCell(1),
                        ElectricityInvoiceColumnNamesEnumEn.DOCUMENT_TYPE.getValue(),
                        currentExcelRowErrorMessages,
                        electricityInvoice,
                        loiDocumentTypeMap
                );
                //---------------------------------------------------
                // Agreement Type | Relationship | Код на “Вид на услугата” | Service Type
                parseAndValidateAgreementTypeValue(
                        row.getCell(2),
                        ElectricityInvoiceColumnNamesEnumEn.AGREEMENT_TYPE.getValue(),
                        currentExcelRowErrorMessages,
                        electricityInvoice,
                        agreementTypeMap
                );
                //---------------------------------------------------
                // Total Sum in Levs | BigDecimal | TotalAmount
                parseAndValidateBigDecimalValue( // setting it to TotalSum to avoid the name conflict, then setting at line 257
                        row.getCell(3),
                        ElectricityInvoiceColumnNamesEnumEn.TOTAL_SUM_IN_LEVS.getValue(),
                        currentExcelRowErrorMessages,
                        electricityInvoice
                );
                //---------------------------------------------------
                // Period From | LocalDate | Период от
                parseAndValidateLocalDateValue(
                        row.getCell(4),
                        ElectricityInvoiceColumnNamesEnumEn.PERIOD_FROM.getValue(),
                        currentExcelRowErrorMessages,
                        electricityInvoice
                );
                //---------------------------------------------------
                // Period To | LocalDate | Период до
                parseAndValidateLocalDateValue(
                        row.getCell(5),
                        ElectricityInvoiceColumnNamesEnumEn.PERIOD_TO.getValue(),
                        currentExcelRowErrorMessages,
                        electricityInvoice
                );
                //---------------------------------------------------
                // Checking whether the Current row is Empty
                if (
                        electricityInvoice.getPeriodFrom() == null &&
                                electricityInvoice.getPeriodTo() == null &&
                                electricityInvoice.getReportingPointOwn() == null &&
                                electricityInvoice.getTotalQuantity() == null
                )
                    break; // TODO: That means that the following rows won't be read
                //---------------------------------------------------
                // Check whether Period From is before Period To
                validatePeriodRange(electricityInvoice, currentExcelRowErrorMessages);
                //---------------------------------------------------
                // Check if the row already exists
                importValueRepository
                        .findAllByReportingPointOwnAndLoiDocumentTypeAndAgreementTypeAndPeriodFromAndPeriodToAndDeletedAndCompany(
                                electricityInvoice.getReportingPointOwn(),
                                electricityInvoice.getLoiDocumentType(),
                                electricityInvoice.getAgreementType(),
                                electricityInvoice.getPeriodFrom(),
                                electricityInvoice.getPeriodTo(),
                                false,
                                dbSecUserService.getCurrentUserManagedCompany()
                        )
                        .ifPresent(previousImportValues -> {
                            if (previousImportValues.stream().anyMatch(ImportValue::getIsValid))
                                currentExcelRowErrorMessages.add(String.format(DUPLICATE_ROW, "ImportValues"));
                        });
                //---------------------------------------------------


                if (currentExcelRowErrorMessages.isEmpty()) { // in case of no errors
                    electricityInvoices.add(electricityInvoice);

                } else { // in case of error/s: add it to the Map with invalid rows
                    invalidFileRowEntries.put(counter + 1, currentExcelRowErrorMessages);
                }
            }
        }


        if (invalidFileRowEntries.isEmpty()) { // if there are no errors -> save all entries
            electricityInvoices
                    .forEach(electricityInvoice ->
                            {
                                ImportValue importValue = ImportValue
                                        .builder()
                                        .reportingPointOwn(electricityInvoice.getReportingPointOwn())
                                        .loiDocumentType(electricityInvoice.getLoiDocumentType())
                                        .agreementType(electricityInvoice.getAgreementType())
                                        .priceInLevs(electricityInvoice.getTotalSumInLevs())
                                        .periodFrom(electricityInvoice.getPeriodFrom())
                                        .periodTo(electricityInvoice.getPeriodTo())
                                        .isValid(true)
                                        .build();

                                entityPersistenceService
                                        .persistEntity(importValue, false, true);
                            }
                    );
        }
    }


    // Second File
    public void importQuantitiesFile(
            int counter,
            List<ElectricityInvoice> electricityInvoices,
            Sheet sheet,
            Map<Integer, List<String>> invalidFileRowEntries,
            Map<String, AgreementType> agreementTypeMap,
            Map<Integer, LoiDocumentType> loiDocumentTypeMap,
            Map<String, LoiMeasurementUnit> loiMeasurementUnitMap
    ) {
        for (Row row : sheet) {
            counter++;

            // Validate file columns
            if (counter == 0) {
                if (areFileColumnsInvalid(row, 2, invalidFileRowEntries))
                    return;

            } else {
                List<String> currentExcelRowErrorMessages = new ArrayList<>();

                ElectricityInvoice electricityInvoice = new ElectricityInvoice();

                //---------------------------------------------------
                // Reporting Point OWN | String | ТО ЕВН | Meter Point
                parseAndValidateStringValue(
                        row.getCell(0),
                        ElectricityInvoiceColumnNamesEnumEn.REPORTING_POINT_OWN.getValue(),
                        currentExcelRowErrorMessages,
                        electricityInvoice
                );
                //---------------------------------------------------
                // Agreement Type | Relationship | Код на “Вид на услугата” | Service Type
                parseAndValidateAgreementTypeValue(
                        row.getCell(1),
                        ElectricityInvoiceColumnNamesEnumEn.AGREEMENT_TYPE.getValue(),
                        currentExcelRowErrorMessages,
                        electricityInvoice,
                        agreementTypeMap
                );
                //---------------------------------------------------
                // Total Quantity | BigDecimal | Общо Количество | Quantity
                parseAndValidateBigDecimalValue(
                        row.getCell(2),
                        ElectricityInvoiceColumnNamesEnumEn.TOTAL_QUANTITY.getValue(),
                        currentExcelRowErrorMessages,
                        electricityInvoice
                );
                //---------------------------------------------------
                // Loi Measurement Unit | Relationship | Мерна единица латински
                parseAndValidateLoiMeasurementUnitValue(
                        row.getCell(3),
                        ElectricityInvoiceColumnNamesEnumEn.LOI_MEASUREMENT_UNIT.getValue(),
                        currentExcelRowErrorMessages,
                        electricityInvoice,
                        loiMeasurementUnitMap
                );
                //---------------------------------------------------
                // Period From | LocalDate | Период от
                parseAndValidateLocalDateValue(
                        row.getCell(4),
                        ElectricityInvoiceColumnNamesEnumEn.PERIOD_FROM.getValue(),
                        currentExcelRowErrorMessages,
                        electricityInvoice
                );
                //---------------------------------------------------
                // Period To | LocalDate | Период до
                parseAndValidateLocalDateValue(
                        row.getCell(5),
                        ElectricityInvoiceColumnNamesEnumEn.PERIOD_TO.getValue(),
                        currentExcelRowErrorMessages,
                        electricityInvoice
                );
                //---------------------------------------------------
                // Document Type | Relationship | Вид на документа | Invoice Type
                parseAndValidateLoiDocumentTypeValue(
                        row.getCell(6),
                        ElectricityInvoiceColumnNamesEnumEn.DOCUMENT_TYPE.getValue(),
                        currentExcelRowErrorMessages,
                        electricityInvoice,
                        loiDocumentTypeMap
                );
                //---------------------------------------------------
                // Checking whether the Current row is Empty
                if (
                        electricityInvoice.getPeriodFrom() == null &&
                                electricityInvoice.getPeriodTo() == null &&
                                electricityInvoice.getReportingPointOwn() == null &&
                                electricityInvoice.getTotalQuantity() == null
                )
                    break; // TODO: That means that the following rows won't be read
                //---------------------------------------------------
                // Check whether Period From is before Period To
                validatePeriodRange(electricityInvoice, currentExcelRowErrorMessages);
                //---------------------------------------------------
                // Check if the row already exists
                importQuantityRepository
                        .findAllByReportingPointOwnAndLoiDocumentTypeAndAgreementTypeAndPeriodFromAndPeriodToAndDeletedAndCompany(
                                electricityInvoice.getReportingPointOwn(),
                                electricityInvoice.getLoiDocumentType(),
                                electricityInvoice.getAgreementType(),
                                electricityInvoice.getPeriodFrom(),
                                electricityInvoice.getPeriodTo(),
                                false,
                                dbSecUserService.getCurrentUserManagedCompany()
                        )
                        .ifPresent(previousImportQuantities -> {
                            if (previousImportQuantities.stream().anyMatch(ImportQuantity::getIsValid))
                                currentExcelRowErrorMessages.add(String.format(DUPLICATE_ROW, "ImportQuantities"));
                        });
                //---------------------------------------------------


                if (currentExcelRowErrorMessages.isEmpty()) { // in case of no errors
                    electricityInvoices.add(electricityInvoice);

                } else { // in case of error/s: add it to the Map with invalid rows
                    invalidFileRowEntries.put(counter + 1, currentExcelRowErrorMessages);
                }
            }
        }


        if (invalidFileRowEntries.isEmpty()) { // if there are no errors -> save all entries
            electricityInvoices
                    .forEach(electricityInvoice -> {
                                ImportQuantity importQuantity = ImportQuantity
                                        .builder()
                                        .reportingPointOwn(electricityInvoice.getReportingPointOwn())
                                        .agreementType(electricityInvoice.getAgreementType())
                                        .totalQuantity(electricityInvoice.getTotalQuantity())
                                        .loiMeasurementUnit(electricityInvoice.getLoiMeasurementUnit())
                                        .periodFrom(electricityInvoice.getPeriodFrom())
                                        .periodTo(electricityInvoice.getPeriodTo())
                                        .loiDocumentType(electricityInvoice.getLoiDocumentType())
                                        .isValid(true)
                                        .build();

                                entityPersistenceService
                                        .persistEntity(importQuantity, false, true);
                            }
                    );
        }
    }


    // Third File
    public void importValuesAndQuantitiesFile(
            int counter,
            List<ElectricityInvoice> electricityInvoices,
            Sheet sheet,
            Map<Integer, List<String>> invalidFileRowEntries,
            Map<String, AgreementType> agreementTypeMap,
            Map<Integer, LoiDocumentType> loiDocumentTypeMap,
            Map<String, LoiMeasurementUnit> loiMeasurementUnitMap
    ) {
        for (Row row : sheet) {
            counter++;

            // Validate file columns
            if (counter == 0) {
                if (areFileColumnsInvalid(row, 3, invalidFileRowEntries))
                    return;

            } else {
                List<String> currentExcelRowErrorMessages = new ArrayList<>();

                ElectricityInvoice electricityInvoice = new ElectricityInvoice();

                //---------------------------------------------------
                // Reporting Point OWN | String | ТО ЕВН | Meter Point
                parseAndValidateStringValue(
                        row.getCell(0),
                        ElectricityInvoiceColumnNamesEnumEn.REPORTING_POINT_OWN.getValue(),
                        currentExcelRowErrorMessages,
                        electricityInvoice
                );
                //---------------------------------------------------
                // Document Type | Relationship | Вид на документа | Invoice Type
                parseAndValidateLoiDocumentTypeValue(
                        row.getCell(1),
                        ElectricityInvoiceColumnNamesEnumEn.DOCUMENT_TYPE.getValue(),
                        currentExcelRowErrorMessages,
                        electricityInvoice,
                        loiDocumentTypeMap
                );
                //---------------------------------------------------
                // Agreement Type | Relationship | Код на “Вид на услугата” | Service Type
                parseAndValidateAgreementTypeValue(
                        row.getCell(2),
                        ElectricityInvoiceColumnNamesEnumEn.AGREEMENT_TYPE.getValue(),
                        currentExcelRowErrorMessages,
                        electricityInvoice,
                        agreementTypeMap
                );
                //---------------------------------------------------
                // Total Sum in Levs | BigDecimal | Общо Количество | Total Amount
                parseAndValidateBigDecimalValue(
                        row.getCell(3),
                        ElectricityInvoiceColumnNamesEnumEn.TOTAL_SUM_IN_LEVS.getValue(),
                        currentExcelRowErrorMessages,
                        electricityInvoice
                );
                //---------------------------------------------------
                // Period From | LocalDate | Период от
                parseAndValidateLocalDateValue(
                        row.getCell(4),
                        ElectricityInvoiceColumnNamesEnumEn.PERIOD_FROM.getValue(),
                        currentExcelRowErrorMessages,
                        electricityInvoice
                );
                //---------------------------------------------------
                // Period To | LocalDate | Период до
                parseAndValidateLocalDateValue(
                        row.getCell(5),
                        ElectricityInvoiceColumnNamesEnumEn.PERIOD_TO.getValue(),
                        currentExcelRowErrorMessages,
                        electricityInvoice
                );
                //---------------------------------------------------
                // Total Quantity | BigDecimal | Quantity
                parseAndValidateBigDecimalValue(
                        row.getCell(6),
                        ElectricityInvoiceColumnNamesEnumEn.TOTAL_QUANTITY.getValue(),
                        currentExcelRowErrorMessages,
                        electricityInvoice
                );
                //---------------------------------------------------
                // Price in Levs | BigDecimal | Single Price
                parseAndValidateBigDecimalValue(
                        row.getCell(7),
                        ElectricityInvoiceColumnNamesEnumEn.IMPORT_VALUE_AND_QUANTITY_PRICE_IN_LEVS.getValue(),
                        currentExcelRowErrorMessages,
                        electricityInvoice
                );
                // ---------------------------------------------------
                // Loi Measurement Unit | Relationship | Мерна единица латински
                parseAndValidateLoiMeasurementUnitValue(
                        row.getCell(8),
                        ElectricityInvoiceColumnNamesEnumEn.LOI_MEASUREMENT_UNIT.getValue(),
                        currentExcelRowErrorMessages,
                        electricityInvoice,
                        loiMeasurementUnitMap
                );
                //---------------------------------------------------
                // Checking whether the Current row is Empty
                if (
                        electricityInvoice.getPeriodFrom() == null &&
                                electricityInvoice.getPeriodTo() == null &&
                                electricityInvoice.getReportingPointOwn() == null &&
                                electricityInvoice.getTotalQuantity() == null
                )
                    break; // TODO: That means that the following rows won't be read
                //---------------------------------------------------
                // Check whether Period From is before Period To
                validatePeriodRange(electricityInvoice, currentExcelRowErrorMessages);
                //---------------------------------------------------
                // Check if the row already exists
                importValueAndQuantityRepository
                        .findAllByReportingPointOwnAndLoiDocumentTypeAndAgreementTypeAndPeriodFromAndPeriodToAndDeletedAndCompany(
                                electricityInvoice.getReportingPointOwn(),
                                electricityInvoice.getLoiDocumentType(),
                                electricityInvoice.getAgreementType(),
                                electricityInvoice.getPeriodFrom(),
                                electricityInvoice.getPeriodTo(),
                                false,
                                dbSecUserService.getCurrentUserManagedCompany()
                        )
                        .ifPresent(previousImportValuesAndQuantities -> {
                            if (previousImportValuesAndQuantities.stream().anyMatch(ImportValueAndQuantity::getIsValid))
                                currentExcelRowErrorMessages.add(String.format(DUPLICATE_ROW, "ImportValuesAndQuantities"));
                        });
                //---------------------------------------------------


                if (currentExcelRowErrorMessages.isEmpty()) { // in case of no errors
                    electricityInvoices.add(electricityInvoice);

                } else { // in case of error/s: add it to the Map with invalid rows
                    invalidFileRowEntries.put(counter + 1, currentExcelRowErrorMessages);
                }
            }
        }


        if (invalidFileRowEntries.isEmpty()) { // if there are no errors -> save all entries
            electricityInvoices
                    .forEach(
                            electricityInvoice -> {
                                ImportValueAndQuantity importValueAndQuantity = ImportValueAndQuantity
                                        .builder()
                                        .reportingPointOwn(electricityInvoice.getReportingPointOwn())
                                        .loiDocumentType(electricityInvoice.getLoiDocumentType())
                                        .agreementType(electricityInvoice.getAgreementType())
                                        .loiMeasurementUnit(electricityInvoice.getLoiMeasurementUnit())
                                        .totalSumInLevs(electricityInvoice.getTotalSumInLevs())
                                        .periodFrom(electricityInvoice.getPeriodFrom())
                                        .periodTo(electricityInvoice.getPeriodTo())
                                        .totalQuantity(electricityInvoice.getTotalQuantity())
                                        .priceInLevs(electricityInvoice.getPriceInLevs())
                                        .isValid(true)
                                        .build();

                                entityPersistenceService
                                        .persistEntity(importValueAndQuantity, false, true);
                            }
                    );
        }
    }
}
