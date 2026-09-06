package bg.latona.santa.selfie.constant.electricityInvoiceRelated;


import bg.latona.santa.selfie.enums.ElectricityInvoiceColumnNamesEnumBg;
import bg.latona.santa.entities.selfie.LoiDocumentType;
import bg.latona.santa.entities.selfie.LoiMeasurementUnit;

import java.math.BigDecimal;
import java.util.*;


public class ElectricityInvoiceConstants {

    public static final String DATE_SEPARATOR = ".";


    public static final double VAT_VALUE = 0.2;


    public static final String CSV_SEPARATOR = ":";


    public static final String HTML_REPLACE_SYMBOL = "&";


    public static final String DATE_DOT_SEPARATOR_SPLIT = "\\.";


    public static final String DATE_HYPHEN_SEPARATOR_SPLIT = "-";


    public static final String EMPTY_HTML_VALUE_PLACEHOLDER = "N/A";


    public static final List<String> VALID_FILE_TYPES = Arrays.asList(".xlsx", ".xls");


    public static final BigDecimal LEV_EURO_RATIO = BigDecimal.valueOf(1.96);


    public static final Map<String, Long> LOI_DOCUMENT_TYPES_EN_NAMES_MAP;

    static {
        Map<String, Long> tempMap = new HashMap<>();
        tempMap.put("invoice", LoiDocumentType.INVOICE);
        tempMap.put("debitNote", LoiDocumentType.DEBIT_NOTE);
        tempMap.put("creditNote", LoiDocumentType.CREDIT_NOTE);
        LOI_DOCUMENT_TYPES_EN_NAMES_MAP = Collections.unmodifiableMap(tempMap); // Make immutable
    }


    public static final Map<Long, String> LOI_DOCUMENT_TYPES_BG_NAMES_MAP;

    static {
        Map<Long, String> tempMap = new HashMap<>();
        tempMap.put(LoiDocumentType.INVOICE, "Фактура");
        tempMap.put(LoiDocumentType.DEBIT_NOTE, "Дебитно Известие");
        tempMap.put(LoiDocumentType.CREDIT_NOTE, "Кредитно Известие");
        LOI_DOCUMENT_TYPES_BG_NAMES_MAP = Collections.unmodifiableMap(tempMap); // Make immutable
    }


    public static final Map<Long, String> LOI_MEASUREMENT_UNITS_BG_NAMES_MAP;

    static {
        Map<Long, String> tempMap = new HashMap<>();
        tempMap.put(LoiMeasurementUnit.kWh, "kWh");
        tempMap.put(LoiMeasurementUnit.MWh, "MWh");
        LOI_MEASUREMENT_UNITS_BG_NAMES_MAP = Collections.unmodifiableMap(tempMap); // Make immutable
    }

    public static final String LOCAL_DATE_REGEX = "\\d{2}\\.\\d{2}\\.\\d{4}"; // Формат „dd.mm.yyyy“


    public static final String BIG_DECIMAL_NUMBER_REGEX = "-?\\d+([\\.,]\\d+)?"; // Трябва да съдържат само числови стойности


    public static final String GENERATED_FILES_FOLDER_PATH = "src\\main\\resources\\templates\\generatedFiles\\";


    public static final String INVOICE_EXCEL_TEMPLATE_FILE_PATH = "src\\main\\resources\\templates\\invoice_template.xlsx";


    public static final String INVOICE_HTML_TEMPLATE_FILE_PATH = "src\\main\\resources\\templates\\invoice_template.html";


    public static final String[] firstFileColumnNames = {
            ElectricityInvoiceColumnNamesEnumBg.REPORTING_POINT_OWN.getValue(),
            ElectricityInvoiceColumnNamesEnumBg.DOCUMENT_TYPE.getValue(),
            ElectricityInvoiceColumnNamesEnumBg.LOI_AGREEMENT_TYPE.getValue(),
            ElectricityInvoiceColumnNamesEnumBg.TOTAL_SUM_IN_LEVS.getValue(),
            ElectricityInvoiceColumnNamesEnumBg.PERIOD_FROM.getValue(),
            ElectricityInvoiceColumnNamesEnumBg.PERIOD_TO.getValue()
    };


    public static final String[] secondFileColumnNames = {
            ElectricityInvoiceColumnNamesEnumBg.REPORTING_POINT_OWN.getValue(),
            ElectricityInvoiceColumnNamesEnumBg.LOI_AGREEMENT_TYPE.getValue(),
            ElectricityInvoiceColumnNamesEnumBg.TOTAL_QUANTITY.getValue(),
            ElectricityInvoiceColumnNamesEnumBg.LOI_MEASUREMENT_UNIT.getValue(),
            ElectricityInvoiceColumnNamesEnumBg.PERIOD_FROM.getValue(),
            ElectricityInvoiceColumnNamesEnumBg.PERIOD_TO.getValue(),
            ElectricityInvoiceColumnNamesEnumBg.DOCUMENT_TYPE.getValue()
    };


    public static final String[] thirdFileColumnNames = {
            ElectricityInvoiceColumnNamesEnumBg.REPORTING_POINT_OWN.getValue(),
            ElectricityInvoiceColumnNamesEnumBg.DOCUMENT_TYPE.getValue(),
            ElectricityInvoiceColumnNamesEnumBg.LOI_AGREEMENT_TYPE.getValue(),
            ElectricityInvoiceColumnNamesEnumBg.TOTAL_SUM_IN_LEVS.getValue(),
            ElectricityInvoiceColumnNamesEnumBg.PERIOD_FROM.getValue(),
            ElectricityInvoiceColumnNamesEnumBg.PERIOD_TO.getValue(),
            ElectricityInvoiceColumnNamesEnumBg.TOTAL_QUANTITY.getValue(),
            ElectricityInvoiceColumnNamesEnumBg.PRICE_IN_LEVS.getValue(),
            ElectricityInvoiceColumnNamesEnumBg.LOI_MEASUREMENT_UNIT.getValue()
    };


    // Unused
    //---------------------------------------------------------------------------------------------

    public static final int[][] INVOICE_TEMPLATE_DOUBLE_CELL_POSITIONS = {
            {25, 10},  // Данъчна основа в лева
            {26, 10},  // Данъчна основа в евро
            {27, 10},   // ДДС в лева
            {28, 10},   // ДДС в евро
            {29, 10},   // Обща стойност в лева
            {30, 10},   // Обща стойност в евро
    };


    public static final int[][] INVOICE_TEMPLATE_STRING_CELL_POSITIONS = {
            {15, 1},  // Идентификационен № по ДДС
    };
}
