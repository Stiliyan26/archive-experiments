package bg.latona.santa.selfie.enums;


import lombok.Getter;


@Getter
public enum ElectricityInvoiceColumnNamesEnumEn {

    TAX_EVENT_DATE("TaxEventDate"), // not used in the import of files

    DOCUMENT_TYPE("DocumentType"),

    REPORTING_POINT_OWN("ReportingPointOwn"),

    AGREEMENT_TYPE("AgreementType"),

    PERIOD_FROM("PeriodFrom"),

    PERIOD_TO("PeriodTo"),

    TOTAL_QUANTITY("TotalQuantity"),

    LOI_MEASUREMENT_UNIT("MeasurementUnit"),

    IMPORT_VALUE_AND_QUANTITY_PRICE_IN_LEVS("SinglePriceInLevs"),

    PRICE_IN_EUROS("SinglePriceInEuros"), // not used in the import of files

    TOTAL_SUM_IN_LEVS("TotalSumInLevs"),

    TOTAL_SUM_IN_EUROS("TotalSumInEuros"); // not used in the import of files


    private final String value;


    ElectricityInvoiceColumnNamesEnumEn(String value) {
        this.value = value;
    }
}
