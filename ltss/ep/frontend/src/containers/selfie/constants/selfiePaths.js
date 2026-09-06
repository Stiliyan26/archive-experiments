// Authentication
//---------------------------------------------------------------------------------------------

export const LOGIN = "/login";

// Import Files
//---------------------------------------------------------------------------------------------

export const REPORTS_IMPORT_VALUES_FILE = "/reports/importValuesFile/";

export const REPORTS_IMPORT_QUANTITIES_FILE = "/reports/importQuantitiesFile/";

export const REPORTS_IMPORT_VALUES_AND_QUANTITIES_FILE = "/reports/importValuesAndQuantitiesFile/";

// Electricity Invoice
//---------------------------------------------------------------------------------------------

export const POPULATE_ELECTRICITY_INVOICE = "/reports/populateElectricityInvoice";

export const GENERATE_PDF_DOCUMENTS = "/reports/generatePdfDocuments";

export const DOWNLOAD_PDF_ZIP_BASE = "/reports/downloadPdfZip";

export const DOWNLOAD_PDF_ZIP = (API_URL, periodFromStr, periodToStr, documentTypesStr, agreementTypesStr, accessPointsStr) => `${API_URL + DOWNLOAD_PDF_ZIP_BASE}?fromDate=${encodeURIComponent(periodFromStr)}&toDate=${encodeURIComponent(periodToStr)}&documentTypeCodes=${encodeURIComponent(documentTypesStr)}&agreementTypeCodes=${encodeURIComponent(agreementTypesStr)}&accessPoints=${encodeURIComponent(accessPointsStr)}`;

// Fetches
//---------------------------------------------------------------------------------------------

export const AGREEMENT_TYPES_FETCH = "/reports/builder/1?from=AgreementType&select=AgreementType&having=isTrue(AgreementType.isValid)&page=0&size=20&sort=AgreementType.id%2Cdesc";

export const ACCESS_POINTS_FETCH = "/reports/builder/1?from=PowerPlant&select=PowerPlant.accessPoint,PowerPlant&page=0&size=20&sort=PowerPlant.id%2Cdesc";

export const ACCOUNTING_PERIOD_FETCH = "/reports/builder/1?from=AccountingPeriod&select=AccountingPeriod&having=isTrue(AccountingPeriod.isActive)&page=0&size=20&sort=AccountingPeriod.id%2Cdesc";

export const IMPORT_VALUES_FETCH = `/reports/builder/1?from=ImportValue&select=ImportValue.loiDocumentType,ImportValue.agreementType,ImportValue.electricityInvoice,ImportValue&page=0&size=20&sort=ImportValue.id%2Cdesc`;

export const IMPORT_QUANTITIES_FETCH = `/reports/builder/1?from=ImportQuantity&select=ImportQuantity.agreementType,ImportQuantity.loiMeasurementUnit,ImportQuantity.loiDocumentType,ImportQuantity.electricityInvoice,ImportQuantity&page=0&size=20&sort=ImportQuantity.id%2Cdesc`;

export const IMPORT_VALUES_AND_QUANTITIES_FETCH = `/reports/builder/1?from=ImportValueAndQuantity&select=ImportValueAndQuantity.loiDocumentType,ImportValueAndQuantity.agreementType,ImportValueAndQuantity.loiMeasurementUnit,ImportValueAndQuantity.electricityInvoice,ImportValueAndQuantity&page=0&size=20&sort=ImportValueAndQuantity.id%2Cdesc`;

export const LEGAL_PEOPLE_FETCH = `/reports/builder/1?from=LegalPerson&select=LegalPerson.legalStatus,LegalPerson.legalPersonType,LegalPerson&page=0&size=20&sort=LegalPerson.id%2Cdesc`;

export const LEGAL_PEOPLE_FETCH_2 = `/reports/builder/1?from=LegalPerson&select=LegalPerson.legalStatus,LegalPerson.legalPersonType,LegalPerson&page=0&size=20&sort=LegalPerson.lastModifiedDate%2Cdesc`;

export const ELECTRICITY_INVOICES_FETCH = "/reports/builder/1?from=ElectricityInvoice&select=ElectricityInvoice.loiDocumentType,ElectricityInvoice.agreementType,ElectricityInvoice.loiMeasurementUnit,ElectricityInvoice&page=0&size=20&sort=ElectricityInvoice.id%2Cdesc";

export const ELECTRICITY_INVOICES_FETCH_2 = (fromDate, toDate) => `/reports/builder/1?from=ElectricityInvoice&select=ElectricityInvoice.loiDocumentType,ElectricityInvoice.agreementType,ElectricityInvoice.loiMeasurementUnit,ElectricityInvoice&ElectricityInvoice.taxEventDate=${fromDate}&ElectricityInvoice.taxEventDate=${toDate}&having=isTrue(ElectricityInvoice.hasDbFile)&page=0&size=20&sort=ElectricityInvoice.id%2Cdesc`;

export const POWER_PLANTS_FETCH = `/reports/builder/1?from=PowerPlant&select=PowerPlant.type,PowerPlant.grid,PowerPlant.powerPlantProfile,PowerPlant.owner,PowerPlant.contractStatus,PowerPlant.loiContractQuantity,PowerPlant.loiContractPrice,PowerPlant.loiContractFee,PowerPlant.loiProtocolCountPerMonth,PowerPlant.loiProtocolLineCount,PowerPlant.agreementType,PowerPlant&page=0&size=20&sort=PowerPlant.lastModifiedDate%2Cdesc`;

export const POWER_PLANTS_TYPES_FETCH = `/reports/builder/1?from=LoiTypeOfPowerPlant&select=LoiTypeOfPowerPlant&page=0&size=20&sort=LoiTypeOfPowerPlant.id%2Cdesc`;

// Date Format: 2024-09-01 | 2024-09-30
export const FETCH_IMPORT_VALUES_BY_FILTER_PERIOD = (periodFromStr, periodToStr) => `/reports/builder/1?from=ImportValue&select=ImportValue,ImportValue.powerPlant&where=and(and(>=(ImportValue.periodFrom%3BlocalDateLiteral(${periodFromStr}));<=(ImportValue.periodTo%3BlocalDateLiteral(${periodToStr})));isNull(ImportValue.electricityInvoice))&page=0&size=20&sort=ImportValue.id%2Cdesc`;

// Date Format: 2024-09-01 | 2024-09-30
export const FETCH_IMPORT_QUANTITIES_BY_FILTER_PERIOD = (periodFromStr, periodToStr) => `/reports/builder/1?from=ImportQuantity&select=ImportQuantity,ImportQuantity.powerPlant&where=and(and(>=(ImportQuantity.periodFrom%3BlocalDateLiteral(${periodFromStr}));<=(ImportQuantity.periodTo%3BlocalDateLiteral(${periodToStr})));isNull(ImportQuantity.electricityInvoice))&page=0&size=20&sort=ImportQuantity.id%2Cdesc`;

// Date Format: 2024-09-01 | 2024-09-30
export const FETCH_IMPORT_VALUES_QUANTITIES_BY_FILTER_PERIOD = (periodFromStr, periodToStr) => `/reports/builder/1?from=ImportValueAndQuantity&select=ImportValueAndQuantity,ImportValueAndQuantity.powerPlant&where=and(and(>=(ImportValueAndQuantity.periodFrom%3BlocalDateLiteral(${periodFromStr}));<=(ImportValueAndQuantity.periodTo%3BlocalDateLiteral(${periodToStr})));isNull(ImportValueAndQuantity.electricityInvoice))&page=0&size=20&sort=ImportValueAndQuantity.id%2Cdesc`;

// Corrections
//---------------------------------------------------------------------------------------------

export const CREATE_NEW_ELECTRICITY_INVOICE_NOTE = (API_URL, electricityInvoiceId, totalQuantity, priceInLevs, loiDocumentTypeId) => `${API_URL}/reports/createUpdatedElectricityInvoice/${electricityInvoiceId}/${totalQuantity}/${priceInLevs}/${loiDocumentTypeId}`;