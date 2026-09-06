package bg.latona.santa.selfie.util;

import bg.latona.santa.selfie.enums.ElectricityInvoiceColumnNamesEnumEn;
import bg.latona.santa.entities.DBFile;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.selfie.*;
import bg.latona.santa.reports.ReportException;
import bg.latona.santa.repositories.AgreementTypeRepository;
import bg.latona.santa.repositories.LoiDocumentTypeRepository;
import bg.latona.santa.repositories.LoiMeasurementUnitRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.cglib.core.Local;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static bg.latona.santa.selfie.constant.electricityInvoiceRelated.ElectricityInvoiceConstants.*;
import static bg.latona.santa.selfie.constant.electricityInvoiceRelated.ElectricityInvoiceMessages.*;
import static bg.latona.santa.selfie.constant.electricityInvoiceRelated.ElectricityInvoiceRelatedEntitiesOptionsNames.*;


public class ElectricityInvoiceUtils {

    // Validators

    /********************************************************************/

    public static boolean isFileExtensionInvalid(DBFile dbFile, Map<Integer, List<String>> invalidFileRowEntries) {
        boolean isFileExtensionInvalid = VALID_FILE_TYPES.stream()
                .noneMatch(ext -> dbFile.getName().toLowerCase().endsWith(ext));

        if (isFileExtensionInvalid) {
            List<String> invalidFileExtensionArrayList = new ArrayList<>();
            invalidFileExtensionArrayList.add(FILE_EXTENSION_INVALID_ERROR);

            invalidFileRowEntries.put(0, invalidFileExtensionArrayList);
            return true;
        }

        return false;
    }


    public static boolean areFileColumnsInvalid(Row fileHeaderRow, int fileNumber, Map<Integer, List<String>> invalidFileRowEntries) {
        switch (fileNumber) {
            case 1:
                if (fileHeaderRow.getPhysicalNumberOfCells() != firstFileColumnNames.length) {
                    invalidFileRowEntries.put(0, Arrays.asList(FILE_COLUMNS_INVALID_NUMBER_ERROR));
                    return true;
                }
                for (int i = 0; i < fileHeaderRow.getPhysicalNumberOfCells(); i++)
                    if (!fileHeaderRow.getCell(i).toString().equalsIgnoreCase(firstFileColumnNames[i])) {
                        invalidFileRowEntries.put(0, Arrays.asList(FILE_COLUMNS_INVALID_ERROR));
                        return true;
                    }
                break;

            case 2:
                if (fileHeaderRow.getPhysicalNumberOfCells() != secondFileColumnNames.length) {
                    invalidFileRowEntries.put(0, Arrays.asList(FILE_COLUMNS_INVALID_NUMBER_ERROR));
                    return true;
                }
                for (int i = 0; i < fileHeaderRow.getPhysicalNumberOfCells(); i++)
                    if (!fileHeaderRow.getCell(i).toString().equalsIgnoreCase(secondFileColumnNames[i])) {
                        invalidFileRowEntries.put(0, Arrays.asList(FILE_COLUMNS_INVALID_ERROR));
                        return true;
                    }
                break;

            case 3:
                if (fileHeaderRow.getPhysicalNumberOfCells() != thirdFileColumnNames.length) {
                    invalidFileRowEntries.put(0, Arrays.asList(FILE_COLUMNS_INVALID_NUMBER_ERROR));
                    return true;
                }
                for (int i = 0; i < fileHeaderRow.getPhysicalNumberOfCells(); i++)
                    if (!fileHeaderRow.getCell(i).toString().equalsIgnoreCase(thirdFileColumnNames[i])) {
                        invalidFileRowEntries.put(0, Arrays.asList(FILE_COLUMNS_INVALID_ERROR));
                        return true;
                    }
                break;
        }

        return false;
    }


    public static void validatePeriodRange(ElectricityInvoice electricityInvoice, List<String> currentExcelRowErrorMessages) {
        if (electricityInvoice.getPeriodFrom() == null || electricityInvoice.getPeriodTo() == null)
            return;

        if (!electricityInvoice.getPeriodFrom().isBefore(electricityInvoice.getPeriodTo()) || electricityInvoice.getPeriodFrom().isEqual(electricityInvoice.getPeriodTo())) {
            currentExcelRowErrorMessages.add(INVALID_PERIOD_FROM_AND_TO_ERROR);
        }
    }


    // Prefetch Relational Entities

    /********************************************************************/

    public static Map<String, AgreementType> getAgreementTypeMap(ManagedCompany company, AgreementTypeRepository agreementTypeRepository) {
        // Prefetching the agreement types
        List<AgreementType> agreementTypes = agreementTypeRepository.findAllByIsValidIsTrue();

        // Creating a map to store the types with specific keys
        Map<String, AgreementType> agreementTypeMap = new HashMap<>();

        agreementTypes.forEach(agreementType -> agreementTypeMap.put(agreementType.getCode(), agreementType));

        return agreementTypeMap;
    }


    public static Map<String, LoiMeasurementUnit> getLoiMeasurementUnitMap(ManagedCompany company, LoiMeasurementUnitRepository loiMeasurementUnitRepository) {
        // Prefetching the LoiMeasurementUnits
        LoiMeasurementUnit loiMeasurementUnitkWh = loiMeasurementUnitRepository
                .findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiMeasurementUnit.kWh, company, false);

        LoiMeasurementUnit loiMeasurementUnitmWh = loiMeasurementUnitRepository
                .findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiMeasurementUnit.MWh, company, false);

        // Creating a map to store the types with specific keys
        Map<String, LoiMeasurementUnit> loiMeasurementUnitMap = new HashMap<>();
        loiMeasurementUnitMap.put(LOI_MEASUREMENT_UNIT_KWH, loiMeasurementUnitkWh);
        loiMeasurementUnitMap.put(LOI_MEASUREMENT_UNIT_MWH, loiMeasurementUnitmWh);

        return loiMeasurementUnitMap;
    }


    public static Map<Integer, LoiDocumentType> getLoiDocumentTypeMap(ManagedCompany company, LoiDocumentTypeRepository loiDocumentTypeRepository) {
        // Prefetching the LoiDocumentTypes
        LoiDocumentType loiDocumentTypeInvoice = loiDocumentTypeRepository
                .findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiDocumentType.INVOICE, company, false);

        LoiDocumentType loiDocumentTypeDebitNote = loiDocumentTypeRepository
                .findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiDocumentType.DEBIT_NOTE, company, false);

        LoiDocumentType loiDocumentTypeCreditNote = loiDocumentTypeRepository
                .findFirstByListOptionItemCodeAndCompanyAndDeleted(LoiDocumentType.CREDIT_NOTE, company, false);

        // Creating a map to store the types with specific keys
        Map<Integer, LoiDocumentType> loiDocumentTypeMap = new HashMap<>();
        loiDocumentTypeMap.put(LOI_DOCUMENT_TYPE_INVOICE, loiDocumentTypeInvoice);
        loiDocumentTypeMap.put(LOI_DOCUMENT_DEBIT_NOTE, loiDocumentTypeDebitNote);
        loiDocumentTypeMap.put(LOI_DOCUMENT_CREDIT_NOTE, loiDocumentTypeCreditNote);

        return loiDocumentTypeMap;
    }


    // Validate, Parse and Set Methods

    /********************************************************************/

    public static void parseAndValidateLocalDateValue(Cell cell, String columnName, List<String> currentExcelRowErrorMessages, ElectricityInvoice electricityInvoice) {
        if (cell == null) {
            currentExcelRowErrorMessages.add(String.format(EMPTY_VALUE_ERROR, columnName));
            return;
        }

        String value = cell.toString();

        if (!value.contains(DATE_SEPARATOR) || value.trim().split(DATE_DOT_SEPARATOR_SPLIT).length != 3) {
            currentExcelRowErrorMessages.add(String.format(INVALID_FORMAT_ERROR, columnName));

        } else if (!value.matches(LOCAL_DATE_REGEX)) {
            currentExcelRowErrorMessages.add(String.format(CONTAINS_INVALID_CHARS_ERROR, columnName));

        } else {
            if (columnName.equals(ElectricityInvoiceColumnNamesEnumEn.TAX_EVENT_DATE.getValue())) {
                electricityInvoice.setTaxEventDate(parseDateFromStringDotFormat(value));

            } else if (columnName.equals(ElectricityInvoiceColumnNamesEnumEn.PERIOD_FROM.getValue())) {
                electricityInvoice.setPeriodFrom(parseDateFromStringDotFormat(value));

            } else if (columnName.equals(ElectricityInvoiceColumnNamesEnumEn.PERIOD_TO.getValue()))
                electricityInvoice.setPeriodTo(parseDateFromStringDotFormat(value));
        }
    }


    public static void parseAndValidateStringValue(Cell cell, String columnName, List<String> currentExcelRowErrorMessages, ElectricityInvoice electricityInvoice) {
        if (cell == null) {
            currentExcelRowErrorMessages.add(String.format(EMPTY_VALUE_ERROR, columnName));
            return;
        }

        String value = cell.toString();

        if (value.trim().isEmpty()) {
            currentExcelRowErrorMessages.add(String.format(CANNOT_BE_EMPTY_OR_SPACES_ERROR, columnName));

        } else {
            if (columnName.equals(ElectricityInvoiceColumnNamesEnumEn.REPORTING_POINT_OWN.getValue())) {
                electricityInvoice.setReportingPointOwn(value.trim());
            }
        }
    }


    public static void parseAndValidateBigDecimalValue(Cell cell, String columnName, List<String> currentExcelRowErrorMessages, ElectricityInvoice electricityInvoice) {
        if (cell == null) {
            currentExcelRowErrorMessages.add(String.format(EMPTY_VALUE_ERROR, columnName));
            return;
        }

        String value = cell.toString();

        if (!value.matches(BIG_DECIMAL_NUMBER_REGEX)) {
            currentExcelRowErrorMessages.add(String.format(INVALID_FORMAT_ERROR, columnName));

        } else if (value.trim().isEmpty()) {
            currentExcelRowErrorMessages.add(String.format(CANNOT_BE_EMPTY_OR_SPACES_ERROR, columnName));

        } else if (getCellValueAsBigDecimal(value).compareTo(BigDecimal.ZERO) < 0) {
            currentExcelRowErrorMessages.add(String.format(CANNOT_BE_LESS_THAN_ZERO_ERROR, columnName));

        } else {
            if (columnName.equals(ElectricityInvoiceColumnNamesEnumEn.TOTAL_QUANTITY.getValue())) {
                if (getCellValueAsBigDecimal(value).compareTo(BigDecimal.ZERO) == 0) {
                    currentExcelRowErrorMessages.add(String.format(CANNOT_BE_ZERO_ERROR, columnName));
                    return;
                }

                electricityInvoice.setTotalQuantity((getCellValueAsBigDecimal(value)));

            } else if (columnName.equals(ElectricityInvoiceColumnNamesEnumEn.IMPORT_VALUE_AND_QUANTITY_PRICE_IN_LEVS.getValue())) {
                electricityInvoice.setPriceInLevs(getCellValueAsBigDecimal(value));

            } else if (columnName.equals(ElectricityInvoiceColumnNamesEnumEn.PRICE_IN_EUROS.getValue())) {
                electricityInvoice.setPriceInEuros(getCellValueAsBigDecimal(value));

            } else if (columnName.equals(ElectricityInvoiceColumnNamesEnumEn.TOTAL_SUM_IN_LEVS.getValue())) {
                electricityInvoice.setTotalSumInLevs(getCellValueAsBigDecimal(value));

            } else if (columnName.equals(ElectricityInvoiceColumnNamesEnumEn.TOTAL_SUM_IN_EUROS.getValue())) {
                electricityInvoice.setTotalSumInEuros(getCellValueAsBigDecimal(value));
            }
        }
    }


    public static void parseAndValidateAgreementTypeValue(Cell cell, String columnName, List<String> currentExcelRowErrorMessages, ElectricityInvoice electricityInvoice, Map<String, AgreementType> agreementTypeMap) {
        if (cell == null) {
            currentExcelRowErrorMessages.add(String.format(EMPTY_VALUE_ERROR, columnName));
            return;
        }

        String value = cell.toString();

        if (value.isEmpty()) {
            currentExcelRowErrorMessages.add(String.format(EMPTY_VALUE_ERROR, columnName));
            return;
        }

        if (!agreementTypeMap.containsKey(value.trim())) {
            currentExcelRowErrorMessages.add(String.format(INVALID_VALUE_ERROR, columnName));

        } else {
            electricityInvoice.setAgreementType(agreementTypeMap.get(value.trim()));
        }
    }


    public static void parseAndValidateLoiDocumentTypeValue(Cell cell, String columnName, List<String> currentExcelRowErrorMessages, ElectricityInvoice electricityInvoice, Map<Integer, LoiDocumentType> loiDocumentTypeMap) {
        if (cell == null) {
            currentExcelRowErrorMessages.add(String.format(EMPTY_VALUE_ERROR, columnName));
            return;
        }

        String value = cell.toString();

        if (value.isEmpty()) {
            currentExcelRowErrorMessages.add(String.format(EMPTY_VALUE_ERROR, columnName));
            return;
        }

        if (!loiDocumentTypeMap.containsKey((int) Double.parseDouble(value.trim()))) {
            currentExcelRowErrorMessages.add(String.format(INVALID_VALUE_ERROR, columnName));

        } else {
            electricityInvoice.setLoiDocumentType(loiDocumentTypeMap.get((int) Double.parseDouble(value.trim())));
        }
    }


    public static void parseAndValidateLoiMeasurementUnitValue(Cell cell, String columnName, List<String> currentExcelRowErrorMessages, ElectricityInvoice electricityInvoice, Map<String, LoiMeasurementUnit> loiMeasurementUnitMap) {
        if (cell == null) {
            currentExcelRowErrorMessages.add(String.format(EMPTY_VALUE_ERROR, columnName));
            return;
        }

        String value = cell.toString();

        if (value.isEmpty()) {
            currentExcelRowErrorMessages.add(String.format(EMPTY_VALUE_ERROR, columnName));
            return;
        }

        if (!loiMeasurementUnitMap.containsKey(value.trim())) {
            currentExcelRowErrorMessages.add(String.format(INVALID_VALUE_ERROR, columnName));

        } else {
            electricityInvoice.setLoiMeasurementUnit(loiMeasurementUnitMap.get(value.trim()));
        }
    }


    public static String formatPopulateElectricityInvoiceErrorMessageKey(String reportingPointOwn, LocalDate periodFrom, LocalDate periodTo, String documentTypeItemName) {
        return String.format(
                "%s-%s-%s-%s",
                reportingPointOwn,
                LocalDate.parse(String.valueOf(periodFrom)).format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                LocalDate.parse(String.valueOf(periodTo)).format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                documentTypeItemName
        );
    }


    // Constraints

    /********************************************************************/

//    public static void checkDuplicateTaxEventDateAndAgreementType(ElectricityInvoiceRepository electricityInvoiceRepository, ElectricityInvoice electricityInvoice, List<String> currentExcelRowErrorMessages) {
//        if (electricityInvoiceRepository
//                .countAllByTaxEventDateAndLoiAgreementType(
//                        electricityInvoice.getTaxEventDate(),
//                        electricityInvoice.getLoiAgreementType()
//                ) > 0) {
//            currentExcelRowErrorMessages.add("A record with the same Tax Event Date and Loi Agreement Type already exists.");
//        }
//    }


    // Helper Methods

    /********************************************************************/

    public static LoiDocumentType parseLoiDocumentTypeFromStringUrl(String value, Map<Integer, LoiDocumentType> loiDocumentType) {
        if (LOI_DOCUMENT_TYPES_EN_NAMES_MAP.containsKey(value))
            return loiDocumentType.get(LOI_DOCUMENT_TYPES_EN_NAMES_MAP.get(value).intValue());

        return null;
    }


    private static LocalDate parseDateFromStringDotFormat(String dateStr) {
        String[] dateParts = dateStr.trim().split(DATE_DOT_SEPARATOR_SPLIT);

        try {
            int day = Integer.parseInt(dateParts[0]);
            int month = Integer.parseInt(dateParts[1]);
            int year = Integer.parseInt(dateParts[2]);

            return LocalDate.of(year, month, day);

        } catch (NumberFormatException e) {
            throw new ReportException(INVALID_DATE_FORMAT_ERROR);
        }
    }


    public static LocalDate parseDateFromStringHyphenFormat(String dateStr) {
        String[] dateParts = dateStr.trim().split(DATE_HYPHEN_SEPARATOR_SPLIT);

        try {
            int year = Integer.parseInt(dateParts[0]);
            int month = Integer.parseInt(dateParts[1]);
            int day = Integer.parseInt(dateParts[2]);

            return LocalDate.of(year, month, day);

        } catch (NumberFormatException e) {
            throw new ReportException(INVALID_DATE_FORMAT_ERROR);
        }
    }


    private static BigDecimal getCellValueAsBigDecimal(String cell) {
        return new BigDecimal(cell.replace(",", "."));
    }
}


// Generate Pdf File from Excel Template filled with data (Using LibreOffice)

    /*
    @Override
    public String generatePdfFile() {
        try {

            // Values of the Double Cells
            double[] doubleValues = {
                    20_000,
                    10_000,
                    4_000,
                    2_000,
                    20_000,
                    12_000
            };

            // Values of the String Cells
            String[] stringValues = {
                    "Идентификационен № по ДДС: DynamicValue",
            };

            // Name of the Generated File
            String FILE_NAME = "dynamicallyGeneratedFile";

            // Path to the file, WITHOUT THE EXTENSION
            String PATH_TO_FILE = GENERATED_FILES_FOLDER_PATH + FILE_NAME;

            // Fill the Excel data
            fillExcelInvoiceTemplate(
                    doubleValues,
                    stringValues,
                    PATH_TO_FILE
            );

            // Convert the Excel to PDF
            convertExcelFileToPdf(
                    PATH_TO_FILE,
                    GENERATED_FILES_FOLDER_PATH
            );


            return PATH_TO_FILE + ".pdf";

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    */


// Abstract Logic for the work with CSV file documents
//---------------------------------------------------------------------------------------------

    /*
    private Map<Integer, List<String>> abstractElectricityInvoiceFileHandler(DBFile dbFile, FileProcessor fileProcessor) {
        Map<Integer, List<String>> invalidFileRowEntries = new LinkedHashMap<>();

        if (dbFile != null) {

            // Validate file extension
            if (isFileExtensionInvalid(dbFile, invalidFileRowEntries))
                return invalidFileRowEntries;

            // Reading the DB file as a Stream
            byte[] content = dbFile.getContent();

            try (
                    ByteArrayInputStream byteStream = new ByteArrayInputStream(content);
                    InputStreamReader isReader = new InputStreamReader(byteStream, StandardCharsets.UTF_8);
                    BufferedReader bufferedReader = new BufferedReader(isReader)
            ) {
                // Fetching the relationships beforehand
                ManagedCompany company = secUserService.getCurrentUserManagedCompany();

                Map<String, LoiAgreementType> loiAgreementTypeMap = getLoiAgreementTypeMap(company, loiAgreementTypeRepository);
                Map<String, LoiDocumentType> loiDocumentTypeMap = getLoiDocumentTypeMap(company, loiDocumentTypeRepository);
                Map<String, LoiMeasurementUnit> loiMeasurementUnitMap = getLoiMeasurementUnitMap(company, loiMeasurementUnitRepository);

                fileProcessor.process(
                        bufferedReader,
                        invalidFileRowEntries,
                        loiAgreementTypeMap,
                        loiDocumentTypeMap,
                        loiMeasurementUnitMap
                );

                return invalidFileRowEntries;

            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            throw new ReportException(DB_FILE_MISSING_MESSAGE);
        }

        return invalidFileRowEntries; // If all rows are with valid data: an empty Map will be returned
    }
    */

// Custom Logic for each of the CSV file documents
//---------------------------------------------------------------------------------------------

// First File
    /*
    public void importValuesFile(
            BufferedReader bufferedReader,
            Map<Integer, List<String>> invalidFileRowEntries,
            Map<String, LoiAgreementType> loiAgreementTypeMap,
            Map<String, LoiDocumentType> loiDocumentTypeMap,
            Map<String, LoiMeasurementUnit> loiMeasurementUnitMap
    ) throws IOException {

        // List of the error messages
        List<String> currentExcelRowErrorMessages = new ArrayList<>();
        List<ElectricityInvoice> electricityInvoices = new ArrayList<>();

        int counter = -1;
        String line;

        // Iterating through the rows of data
        while ((line = bufferedReader.readLine()) != null) {
            counter++;

            // Validate file columns
            if (counter == 0) {
                if (areFileColumnsInvalid(line, 1)) {
                    List<String> invalidFileColumnsArrayList = new ArrayList<>();
                    invalidFileColumnsArrayList.add(FILE_COLUMNS_INVALID_ERROR);

                    invalidFileRowEntries.put(0, invalidFileColumnsArrayList);
                    return;

                } else
                    continue;
            }

            // Initializing the Entity
            ElectricityInvoice electricityInvoice = new ElectricityInvoice();

            String[] currentRowDataArr = line.split(CSV_SEPARATOR);
            //---------------------------------------------------
            // Reporting Point OWN | String | ТО ЕВН
            parseAndValidateStringValue(
                    currentRowDataArr[0],
                    ElectricityInvoiceColumnNamesEnumEn.REPORTING_POINT_OWN.getValue(),
                    currentExcelRowErrorMessages,
                    electricityInvoice
            );
            //---------------------------------------------------
            // Document Type | Relationship | Вид на документа
            parseAndValidateLoiDocumentTypeValue(
                    currentRowDataArr[1],
                    ElectricityInvoiceColumnNamesEnumEn.DOCUMENT_TYPE.getValue(),
                    currentExcelRowErrorMessages,
                    electricityInvoice,
                    loiDocumentTypeMap
            );
            //---------------------------------------------------
            // Loi Agreement Type | Relationship | Кода на “Вид на услугата”
            parseAndValidateLoiAgreementTypeValue(
                    currentRowDataArr[2],
                    ElectricityInvoiceColumnNamesEnumEn.LOI_AGREEMENT_TYPE.getValue(),
                    currentExcelRowErrorMessages,
                    electricityInvoice,
                    loiAgreementTypeMap
            );
            //---------------------------------------------------
            // Price in Levs | BigDecimal | Стойност в лв. /без ДДС, закръглена до 2ри знак
            parseAndValidateBigDecimalValue(
                    currentRowDataArr[3],
                    ElectricityInvoiceColumnNamesEnumEn.PRICE_IN_LEVS.getValue(),
                    currentExcelRowErrorMessages,
                    electricityInvoice
            );
            //---------------------------------------------------
            // Period From | LocalDate | Период от
            parseAndValidateLocalDateValue(
                    currentRowDataArr[4],
                    ElectricityInvoiceColumnNamesEnumEn.PERIOD_FROM.getValue(),
                    currentExcelRowErrorMessages,
                    electricityInvoice
            );
            //---------------------------------------------------
            // Period To | LocalDate | Период до
            parseAndValidateLocalDateValue(
                    currentRowDataArr[5],
                    ElectricityInvoiceColumnNamesEnumEn.PERIOD_TO.getValue(),
                    currentExcelRowErrorMessages,
                    electricityInvoice
            );
            //-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-
            // Check whether Period From is before Period To
            if (isPeriodRangeInvalid(
                    electricityInvoice.getPeriodFrom(),
                    electricityInvoice.getPeriodTo(),
                    invalidFileRowEntries,
                    counter + 1)
            )
                continue;
            //-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-


            if (currentExcelRowErrorMessages.isEmpty()) { // in case of no errors

                electricityInvoices.add(electricityInvoice);

            } else { // in case of error/s: add it to the Map with invalid rows
                invalidFileRowEntries.put(counter + 1, currentExcelRowErrorMessages);
            }
        }

        if (invalidFileRowEntries.isEmpty()) { // if there are no errors -> save all entries
            electricityInvoices
                    .forEach(electricityInvoice -> {
                                Optional<ElectricityInvoice> previousEntity = electricityInvoiceRepository
                                        .findByReportingPointOwnAndPeriodFromAndPeriodTo(
                                                electricityInvoice.getReportingPointOwn(),
                                                electricityInvoice.getPeriodFrom(),
                                                electricityInvoice.getPeriodTo()
                                        );

                                if (previousEntity.isPresent()) { // case where such entity already exists
                                    previousEntity.get().setLoiDocumentType(electricityInvoice.getLoiDocumentType());
                                    previousEntity.get().setPriceInLevs(electricityInvoice.getPriceInLevs());

                                    electricityInvoiceRepository.save(previousEntity.get());

                                } else // case where the entity does not exist
                                    entityPersistenceService.persistEntity(electricityInvoice, true, false);
                            }
                    );
        }
    }
    */


// Second File
    /*
    public void importQuantitiesFile(
            BufferedReader bufferedReader,
            Map<Integer, List<String>> invalidFileRowEntries,
            Map<String, LoiAgreementType> loiAgreementTypeMap,
            Map<String, LoiDocumentType> loiDocumentTypeMap,
            Map<String, LoiMeasurementUnit> loiMeasurementUnitMap
    ) throws IOException {

        // List of the error messages
        List<String> currentExcelRowErrorMessages = new ArrayList<>();
        List<ElectricityInvoice> electricityInvoices = new ArrayList<>();

        String line;
        int counter = -1;

        // Iterating through the rows of data
        while ((line = bufferedReader.readLine()) != null) {
            counter++;

            // Validate file columns
            if (counter == 0) {
                if (areFileColumnsInvalid(line, 2)) {
                    List<String> invalidFileColumnsArrayList = new ArrayList<>();
                    invalidFileColumnsArrayList.add(FILE_COLUMNS_INVALID_ERROR);

                    invalidFileRowEntries.put(0, invalidFileColumnsArrayList);
                    return;

                } else
                    continue;
            }

            // Initializing the Entity
            ElectricityInvoice electricityInvoice = new ElectricityInvoice();

            String[] currentRowDataArr = line.split(CSV_SEPARATOR);
            //---------------------------------------------------
            // Reporting Point OWN | String | ТО ЕВН
            parseAndValidateStringValue(
                    currentRowDataArr[0],
                    ElectricityInvoiceColumnNamesEnumEn.REPORTING_POINT_OWN.getValue(),
                    currentExcelRowErrorMessages,
                    electricityInvoice
            );
            //---------------------------------------------------
            // Loi Agreement Type | Relationship | Кода на “Вид на услугата”
            parseAndValidateLoiAgreementTypeValue(
                    currentRowDataArr[1],
                    ElectricityInvoiceColumnNamesEnumEn.LOI_AGREEMENT_TYPE.getValue(),
                    currentExcelRowErrorMessages,
                    electricityInvoice,
                    loiAgreementTypeMap
            );
            //---------------------------------------------------
            // Total Quantity | BigDecimal | Общо Количество
            parseAndValidateBigDecimalValue(
                    currentRowDataArr[2],
                    ElectricityInvoiceColumnNamesEnumEn.TOTAL_QUANTITY.getValue(),
                    currentExcelRowErrorMessages,
                    electricityInvoice
            );
            //---------------------------------------------------
            // Loi Measurement Unit | Relationship | Мерна единица латински
            parseAndValidateLoiMeasurementUnitValue(
                    currentRowDataArr[3],
                    ElectricityInvoiceColumnNamesEnumEn.LOI_MEASUREMENT_UNIT.getValue(),
                    currentExcelRowErrorMessages,
                    electricityInvoice,
                    loiMeasurementUnitMap
            );
            //---------------------------------------------------
            // Period From | LocalDate | Период от
            parseAndValidateLocalDateValue(
                    currentRowDataArr[4],
                    ElectricityInvoiceColumnNamesEnumEn.PERIOD_FROM.getValue(),
                    currentExcelRowErrorMessages,
                    electricityInvoice
            );
            //---------------------------------------------------
            // Period To | LocalDate | Период до
            parseAndValidateLocalDateValue(
                    currentRowDataArr[5],
                    ElectricityInvoiceColumnNamesEnumEn.PERIOD_TO.getValue(),
                    currentExcelRowErrorMessages,
                    electricityInvoice
            );
            //-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-
            // Check whether Period From is before Period To
            if (isPeriodRangeInvalid(
                    electricityInvoice.getPeriodFrom(),
                    electricityInvoice.getPeriodTo(),
                    invalidFileRowEntries,
                    counter + 1)
            )
                continue;
            //-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-


            if (currentExcelRowErrorMessages.isEmpty()) { // in case of no errors

                electricityInvoices.add(electricityInvoice);

            } else { // in case of error/s: add it to the Map with invalid rows
                invalidFileRowEntries.put(counter + 1, currentExcelRowErrorMessages);
            }
        }

        if (invalidFileRowEntries.isEmpty()) {
            electricityInvoices
                    .forEach(
                            electricityInvoice -> {
                                Optional<ElectricityInvoice> previousEntity = electricityInvoiceRepository
                                        .findByReportingPointOwnAndPeriodFromAndPeriodTo(
                                                electricityInvoice.getReportingPointOwn(),
                                                electricityInvoice.getPeriodFrom(),
                                                electricityInvoice.getPeriodTo()
                                        );

                                if (previousEntity.isPresent()) { // case where such entity already exists
                                    previousEntity.get().setTotalQuantity(electricityInvoice.getTotalQuantity());
                                    previousEntity.get().setLoiMeasurementUnit(electricityInvoice.getLoiMeasurementUnit());

                                    electricityInvoiceRepository.save(previousEntity.get());

                                } else // creating the entity for the first time
                                    entityPersistenceService.persistEntity(electricityInvoice, true, false);
                            });
        }
    }
    */


// Third File
    /*
    public void importFinalSupplierFile(
            BufferedReader bufferedReader,
            Map<Integer, List<String>> invalidFileRowEntries,
            Map<String, LoiAgreementType> loiAgreementTypeMap,
            Map<String, LoiDocumentType> loiDocumentTypeMap,
            Map<String, LoiMeasurementUnit> loiMeasurementUnitMap
    ) throws IOException {

        // List of the error messages
        List<String> currentExcelRowErrorMessages = new ArrayList<>();
        List<ElectricityInvoice> electricityInvoices = new ArrayList<>();

        String line;
        int counter = -1;

        // Iterating through the rows of data
        while ((line = bufferedReader.readLine()) != null) {
            counter++;

            // Validate file columns
            if (counter == 0) {
                if (areFileColumnsInvalid(line, 3)) {
                    List<String> invalidFileColumnsArrayList = new ArrayList<>();
                    invalidFileColumnsArrayList.add(FILE_COLUMNS_INVALID_ERROR);

                    invalidFileRowEntries.put(0, invalidFileColumnsArrayList);
                    return;

                } else
                    continue;
            }

            // Initializing the Entity
            ElectricityInvoice electricityInvoice = new ElectricityInvoice();

            String[] currentRowDataArr = line.split(CSV_SEPARATOR);
            //---------------------------------------------------
            // Reporting Point OWN | String | ТО ЕВН
            parseAndValidateStringValue(
                    currentRowDataArr[0],
                    ElectricityInvoiceColumnNamesEnumEn.REPORTING_POINT_OWN.getValue(),
                    currentExcelRowErrorMessages,
                    electricityInvoice
            );
            //---------------------------------------------------
            // Document Type | Relationship | Вид на документа
            parseAndValidateLoiDocumentTypeValue(
                    currentRowDataArr[1],
                    ElectricityInvoiceColumnNamesEnumEn.DOCUMENT_TYPE.getValue(),
                    currentExcelRowErrorMessages,
                    electricityInvoice,
                    loiDocumentTypeMap
            );
            //---------------------------------------------------
            // Loi Agreement Type | Relationship | Кода на “Вид на услугата”
            parseAndValidateLoiAgreementTypeValue(
                    currentRowDataArr[2],
                    ElectricityInvoiceColumnNamesEnumEn.LOI_AGREEMENT_TYPE.getValue(),
                    currentExcelRowErrorMessages,
                    electricityInvoice,
                    loiAgreementTypeMap
            );
            //---------------------------------------------------
            // Price in Levs | BigDecimal | Стойност в лв. /без ДДС, закръглена до 2ри знак/
            parseAndValidateBigDecimalValue(
                    currentRowDataArr[3],
                    ElectricityInvoiceColumnNamesEnumEn.PRICE_IN_LEVS.getValue(),
                    currentExcelRowErrorMessages,
                    electricityInvoice
            );
            //---------------------------------------------------
            // Period From | LocalDate | Период от
            parseAndValidateLocalDateValue(
                    currentRowDataArr[4],
                    ElectricityInvoiceColumnNamesEnumEn.PERIOD_FROM.getValue(),
                    currentExcelRowErrorMessages,
                    electricityInvoice
            );
            //---------------------------------------------------
            // Period To | LocalDate | Период до
            parseAndValidateLocalDateValue(
                    currentRowDataArr[5],
                    ElectricityInvoiceColumnNamesEnumEn.PERIOD_TO.getValue(),
                    currentExcelRowErrorMessages,
                    electricityInvoice
            );
            //---------------------------------------------------
            // Total Quantity | BigDecimal | Общо Количество
            parseAndValidateBigDecimalValue(
                    currentRowDataArr[6],
                    ElectricityInvoiceColumnNamesEnumEn.TOTAL_QUANTITY.getValue(),
                    currentExcelRowErrorMessages,
                    electricityInvoice
            );
            //---------------------------------------------------
            // Total Sum in Levs | BigDecimal | Единична цена в лв. на kWh, закръглена до 2ри знак
            parseAndValidateBigDecimalValue(
                    currentRowDataArr[7],
                    ElectricityInvoiceColumnNamesEnumEn.TOTAL_SUM_IN_LEVS.getValue(),
                    currentExcelRowErrorMessages,
                    electricityInvoice
            );
            //-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-
            // Check whether Period From is before Period To
            if (isPeriodRangeInvalid(
                    electricityInvoice.getPeriodFrom(),
                    electricityInvoice.getPeriodTo(),
                    invalidFileRowEntries,
                    counter + 1)
            )
                continue;
            //-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-|-


            if (currentExcelRowErrorMessages.isEmpty()) {

                electricityInvoices.add(electricityInvoice);

            } else { // in case of error/s: add it to the Map with invalid rows
                invalidFileRowEntries.put(counter + 1, currentExcelRowErrorMessages);
            }
        }

        electricityInvoices.forEach(
                electricityInvoice -> entityPersistenceService
                        .persistEntity(electricityInvoice, true, false)
        );
    }
    */