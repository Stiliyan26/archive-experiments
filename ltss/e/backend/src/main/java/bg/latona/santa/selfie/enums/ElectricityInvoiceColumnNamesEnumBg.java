package bg.latona.santa.selfie.enums;


import lombok.Getter;


@Getter
public enum ElectricityInvoiceColumnNamesEnumBg {

    TAX_EVENT_DATE("Дата на данъчно събитие"), // not used in the import of files

    DOCUMENT_TYPE("Вид на документа"),

    REPORTING_POINT_OWN("ТО ЕВН"),

    LOI_AGREEMENT_TYPE("Код \"Вид услуга\""),

    PERIOD_FROM("Период от"),

    PERIOD_TO("Период до"),

    TOTAL_QUANTITY("Общо количество"),

    LOI_MEASUREMENT_UNIT("Мерна единица"),

    PRICE_IN_LEVS("Единична цена в лв."),

    PRICE_IN_EUROS("Единична цена в евро"), // not used in the import of files

    TOTAL_SUM_IN_LEVS("Стойност в лв."),

    TOTAL_SUM_IN_EUROS("Стойност в евро"); // not used in the import of files


    private final String value;


    ElectricityInvoiceColumnNamesEnumBg(String value) {
        this.value = value;
    }
}
