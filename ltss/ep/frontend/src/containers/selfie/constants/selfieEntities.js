const selfieEntities = {
    Titles: {
        AgreementTypes: "AgreementTypes",
        Import: "Import"
    },
    PowerPlant: {
        className: "PowerPlant",
        singleCamelCase: "powerPlant",
        pluralCamelCase: "powerPlants",
        view: "powerPlantView"
    },
    AgreementType: {
        className: "AgreementType",
        singleCamelCase: "agreementType",
        pluralCamelCase: "agreementTypes",
        view: "agreementTypeView"
    },
    AgreementTypeMapping: {
        className: "AgreementTypeMapping",
        singleCamelCase: "agreementTypeMapping",
        pluralCamelCase: "agreementTypeMappings",
        view: "agreementTypeMappingView"
    },
    ElectricityInvoice: {
        className: "ElectricityInvoice",
        singleCamelCase: "electricityInvoice",
        pluralCamelCase: "electricityInvoices",
        view: "electricityInvoiceView",
        reference: {
            pluralCamelCase: "references"
        }
    },
    ImportQuantity: {
        className: "ImportQuantity",
        singleCamelCase: "importQuantity",
        pluralCamelCase: "importQuantities",
        view: "importQuantityView"
    },
    ImportValueAndQuantity: {
        className: "ImportValueAndQuantity",
        singleCamelCase: "importValueAndQuantity",
        pluralCamelCase: "importValueAndQuantities",
        view: "importValueAndQuantityView"
    },
    ImportValue: {
        className: "ImportValue",
        singleCamelCase: "importValue",
        pluralCamelCase: "importValues",
        view: "importValueView"
    },
    ErrorMessages: {
        pluralPascalCase: "ErrorMessages"
    },
    Reference: { // View for the filtering and downloading zipped Electricity Invoices
        className: "Reference",
        pluralCamelCase: "references",
        pluralPascalCase: "References"
    },
    AccountingPeriod: {
        className: "AccountingPeriod",
        pluralCamelCase: "accountingPeriods",
    }
};


export {selfieEntities};