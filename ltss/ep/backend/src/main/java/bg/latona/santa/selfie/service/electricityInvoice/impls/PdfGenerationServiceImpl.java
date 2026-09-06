package bg.latona.santa.selfie.service.electricityInvoice.impls;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.selfie.service.common.impls.entityRelated.DbLegalPersonServiceImpl;
import bg.latona.santa.selfie.service.common.interfaces.EntityPersistenceService;
import bg.latona.santa.selfie.service.common.interfaces.entityRelated.DbSecUserService;
import bg.latona.santa.selfie.service.eInvoice.interfaces.eInvoiceUploadService.EInvoiceUploadService;
import bg.latona.santa.selfie.service.electricityInvoice.interfaces.PdfGenerationService;
import bg.latona.santa.selfie.util.DateFormatUtils;
import bg.latona.santa.entities.DBFile;
import bg.latona.santa.entities.selfie.AgreementSelfInvoicing;
import bg.latona.santa.entities.selfie.DocumentRange;
import bg.latona.santa.entities.selfie.ElectricityInvoice;
import bg.latona.santa.entities.nepal.PowerPlant;
import bg.latona.santa.entities.person.LegalPerson;
import bg.latona.santa.reports.ReportException;
import bg.latona.santa.repositories.ElectricityInvoiceRepository;
import bg.latona.santa.repositories.PowerPlantRepository;
import liquibase.pro.packaged.S;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;

import static bg.latona.santa.selfie.constant.electricityInvoiceRelated.ElectricityInvoiceConstants.*;
import static bg.latona.santa.selfie.constant.electricityInvoiceRelated.ElectricityInvoiceMessages.*;
import static bg.latona.santa.selfie.util.CurrencyFormatUtils.convertNumbersToBgnCurrency;
import static bg.latona.santa.selfie.util.ElectricityInvoiceUtils.formatPopulateElectricityInvoiceErrorMessageKey;
import static bg.latona.santa.selfie.util.HtmlUtils.fillHtmlTemplate;
import static bg.latona.santa.selfie.util.PdfUtils.convertHtmlFileToPdfByteArray;


@RequiredArgsConstructor
@Slf4j
@Service
public class PdfGenerationServiceImpl implements PdfGenerationService {

    private final PowerPlantRepository powerPlantRepository;

    private final EntityPersistenceService entityPersistenceService;
    private final ElectricityInvoiceRepository electricityInvoiceRepository;
    private final EInvoiceUploadService eInvoiceUploadService;
    private final DbLegalPersonServiceImpl dbLegalPersonService;
    private final DbSecUserService dbSecUserService;


    @Override
    public Map<String, List<String>> generatePdfDocuments() {
        Map<String, List<String>> invalidElectricityInvoiceRows = new HashMap<>();

        ManagedCompany company = dbSecUserService.getCurrentUserManagedCompany();

        List<ElectricityInvoice> electricityInvoices = electricityInvoiceRepository
                .findAllByIsValidIsTrueAndHasDbFileFalseAndCompanyAndDeleted(company, false); // Fetch all Electricity invoices for the process

        LegalPerson recipient = dbLegalPersonService.getLoggedLegalPerson(true);

        Set<String> uniqueAccessPoints = new HashSet<>();

        electricityInvoices
                .forEach(electricityInvoice -> {
                    Optional<PowerPlant> optionalPowerPlant = powerPlantRepository
                            .findByAccessPointAndCompanyAndDeleted(
                                    electricityInvoice.getReportingPointOwn(),
                                    company,
                                    false
                            );

                    String invoiceDocumentNumber = generateDocumentNumber(optionalPowerPlant, electricityInvoice, invalidElectricityInvoiceRows);

                    if (invoiceDocumentNumber == null) // Power plant or Document range missing or unavailable
                        return;

                    electricityInvoice.setInvoiceNumber(invoiceDocumentNumber);

                    // Create an Array of the values for the template
                    String[] templateData = fillHtmlTemplateData(electricityInvoice, optionalPowerPlant.get(), recipient);

                    LegalPerson legalPerson = optionalPowerPlant.get().getOwner();

                    if (legalPerson == null) {
                        invalidElectricityInvoiceRows.put(
                                formatPopulateElectricityInvoiceErrorMessageKey(
                                        electricityInvoice.getReportingPointOwn(),
                                        electricityInvoice.getPeriodFrom(),
                                        electricityInvoice.getPeriodTo(),
                                        electricityInvoice.getLoiDocumentType().getListOptionItemName()
                                ),
                                Arrays.asList(MISSING_LEGAL_PERSON)
                        );
                        return;
                    }

                    String legalPersonIdentifier = getLegalPersonIdentifier(legalPerson);

                    if (legalPersonIdentifier == null) {
                        invalidElectricityInvoiceRows.put(
                                formatPopulateElectricityInvoiceErrorMessageKey(
                                        electricityInvoice.getReportingPointOwn(),
                                        electricityInvoice.getPeriodFrom(),
                                        electricityInvoice.getPeriodTo(),
                                        electricityInvoice.getLoiDocumentType().getListOptionItemName()
                                ),
                                Arrays.asList(MISSING_LEGAL_PERSON_IDENTIFIER)
                        );
                        return;
                    }

                    String powerPlantIdentifier = getPowerPlantIdentifier(optionalPowerPlant.get());

                    if (powerPlantIdentifier == null) {
                        invalidElectricityInvoiceRows.put(
                                formatPopulateElectricityInvoiceErrorMessageKey(
                                        electricityInvoice.getReportingPointOwn(),
                                        electricityInvoice.getPeriodFrom(),
                                        electricityInvoice.getPeriodTo(),
                                        electricityInvoice.getLoiDocumentType().getListOptionItemName()
                                ),
                                Arrays.asList(MISSING_POWER_PLANT_IDENTIFIER)
                        );
                        return;
                    }

                    // Generate PDF file
                    DBFile dbFile = generatePdfFile(
                            electricityInvoice,
                            DateFormatUtils.formatDateToYYYYMMDD(electricityInvoice.getCreatedDate()),
                            legalPersonIdentifier,
                            powerPlantIdentifier,
                            templateData,
                            invalidElectricityInvoiceRows
                    );

                    // TODO: Check whether there is a key in the map with the current row?

                    electricityInvoice.setDbFile(dbFile);
                    electricityInvoice.setHasDbFile(true);

                    uniqueAccessPoints.add(electricityInvoice.getReportingPointOwn());

                    // Save the entity
                    entityPersistenceService
                            .persistEntity(electricityInvoice, true, true);
                });

        String result = eInvoiceUploadService.uploadZipFileEInvoice(uniqueAccessPoints);

        System.out.println("Result After Uploading ZIP files: " + result);

        return invalidElectricityInvoiceRows;
    }


    // Fill HTML template and Generate PDF file afterward
    @Override
    public DBFile generatePdfFile(
            ElectricityInvoice electricityInvoice, String date, String legalPersonIdentifier, String powerPlantIdentifier, String[] data, Map<String, List<String>> invalidElectricityInvoiceRows
    ) {
        String fileName = generatePdfFileName(
                electricityInvoice.getInvoiceNumber(),
                date,
                legalPersonIdentifier,
                powerPlantIdentifier,
                electricityInvoice.getReportingPointOwn()
        );

        try {
            // Fill the HTML template with data, creating a new HTML file
            InputStream filledHtmlInputStream = fillHtmlTemplate(INVOICE_HTML_TEMPLATE_FILE_PATH, data);

            // Convert the filled HTML template file to PDF file
            byte[] pdfFileContent = convertHtmlFileToPdfByteArray(filledHtmlInputStream);

            DBFile dbFile = new DBFile();
            dbFile.setName(fileName.replace(".xml", ".pdf"));
            dbFile.setContentType("application/pdf");
            dbFile.setContent(pdfFileContent);

            return (DBFile) entityPersistenceService.persistEntity(dbFile, false, true);

        } catch (IOException e) {
            invalidElectricityInvoiceRows.put(
                    formatPopulateElectricityInvoiceErrorMessageKey(
                            electricityInvoice.getReportingPointOwn(),
                            electricityInvoice.getPeriodFrom(),
                            electricityInvoice.getPeriodTo(),
                            electricityInvoice.getLoiDocumentType().getListOptionItemName()
                    ),
                    Arrays.asList(ERROR_CREATING_PDF_FILE)
            );

            throw new ReportException(ERROR_CREATING_PDF_FILE);
        }
    }


    private String generatePdfFileName(String invoiceNumber, String date, String legalPersonIdentifier, String itn, String accessPoint) {
        return String.format("BG_%s_%s_%s_%s_%s.xml",
                invoiceNumber, date, legalPersonIdentifier, itn, accessPoint
        );
    }


    private String getPowerPlantIdentifier(PowerPlant powerPlant) {
        return powerPlant.getIdentificationNumber() != null
                ? powerPlant.getIdentificationNumber()
                : (powerPlant.getProducerEic() != null ? powerPlant.getProducerEic() : null);
    }


    private String getLegalPersonIdentifier(LegalPerson legalPerson) {
        if (legalPerson == null)
            return null;

        return legalPerson.getEik() != null
                ? legalPerson.getEik()
                : (legalPerson.getEgn() != null ? legalPerson.getEgn() : null);
    }


    private String[] fillHtmlTemplateData(ElectricityInvoice electricityInvoice, PowerPlant powerPlant, LegalPerson recipient) {
        List<AgreementSelfInvoicing> agreements = powerPlant.getAgreementsSelfInvoicing();

        if( agreements.size() < 1 ) {  
                throw new ReportException("Няма agreement");
        }
        AgreementSelfInvoicing agreementSelfInvoicing = agreements
                .get(powerPlant.getAgreementsSelfInvoicing().size() - 1);

        boolean isVatIncluded = (agreementSelfInvoicing != null && agreementSelfInvoicing.getIsVatIncluded() != null) ? agreementSelfInvoicing.getIsVatIncluded() : false;

        String recipientName = (recipient != null && !recipient.getContacts().isEmpty() &&
                !recipient.getContacts().get(recipient.getContacts().size() - 1).getName().isEmpty() ?
                recipient.getContacts().get(recipient.getContacts().size() - 1).getName() : EMPTY_HTML_VALUE_PLACEHOLDER);

        String eikRecipient = (recipient != null && recipient.getEik() != null) ? recipient.getEik() : EMPTY_HTML_VALUE_PLACEHOLDER;

        String vatNumberRecipient = (recipient != null && recipient.getVatNumber() != null) ? recipient.getVatNumber() : EMPTY_HTML_VALUE_PLACEHOLDER;

        String documentTypeName = LOI_DOCUMENT_TYPES_BG_NAMES_MAP.get(electricityInvoice.getLoiDocumentType().getListOptionItemCode());

        String currentDateName = LocalDate.now().toString();

        LegalPerson owner = powerPlant.getOwner();

        String ownerIban = (owner != null && !owner.getBankAccounts().isEmpty()) ?
                owner.getBankAccounts().get(owner.getBankAccounts().size() - 1).getIban() : EMPTY_HTML_VALUE_PLACEHOLDER;

        String eikOrEgn = (owner != null && owner.getEik() != null) ?
                owner.getEik() : (owner != null && owner.getEgn() != null) ? owner.getEgn() : EMPTY_HTML_VALUE_PLACEHOLDER;

        String eikWithVat = "";
        if (isVatIncluded) {
            eikWithVat = (owner != null && owner.getVatNumber() != null) ? owner.getVatNumber() : EMPTY_HTML_VALUE_PLACEHOLDER;
        }

        String legalPersonName = (owner != null && owner.getName() != null) ? owner.getName() : EMPTY_HTML_VALUE_PLACEHOLDER;

        String legalPersonAddress = (owner != null && owner.getAddress() != null) ? owner.getAddress() : EMPTY_HTML_VALUE_PLACEHOLDER;

        String measurementUnitName = LOI_MEASUREMENT_UNITS_BG_NAMES_MAP.get(electricityInvoice.getLoiMeasurementUnit().getListOptionItemCode());

        String priceInLevsName = electricityInvoice.getPriceInLevs().toString();

        String priceInEurosName = electricityInvoice.getPriceInEuros().toString();

        String vatAmountInLevsName = electricityInvoice.getVatInLevs().toString();

        String vatAmountInEurosName = electricityInvoice.getVatInEuros().toString();

        String totalAmountInLevsWithVatName = electricityInvoice.getTotalSumInLevs().add(electricityInvoice.getVatInLevs()).toString();

        String totalAmountInEurosWithVatName = electricityInvoice.getTotalSumInEuros().add(electricityInvoice.getVatInEuros()).toString();

        String paymentAmountInLevsName = convertNumbersToBgnCurrency(electricityInvoice.getTotalSumInLevs().intValue());

        String paymentAmountInEurosName = convertNumbersToBgnCurrency(electricityInvoice.getTotalSumInEuros().intValue());

        String relationshipToInvoice =
                (electricityInvoice.getLoiDocumentType().getListOptionItemCode() == 2 ||
                        electricityInvoice.getLoiDocumentType().getListOptionItemCode() == 3) ? // Credit or Debit note
                        String.format(
                                "Към Фактура № %s / Дата: %s <br>",
                                electricityInvoice.getElectricityInvoice().getInvoiceNumber(),
                                electricityInvoice.getTaxEventDate())
                        : "";


        return new String[]{
                // ----------------- Оригинал -----------------
                String.format("%s - САМОФАКТУРИРАНЕ", documentTypeName)

                // Доставчик
                , documentTypeName
                , electricityInvoice.getInvoiceNumber() // Фактура Номер
                , currentDateName // Current Date
                , electricityInvoice.getTaxEventDate().toString() // Tax Event Date
                , relationshipToInvoice
                , legalPersonName // Legal Person Name
                , legalPersonAddress // Legal Person Address
                , eikOrEgn // Идентификационен №
                , isVatIncluded ? String.format("Идентификационен № по ДДС: %s<br>", eikWithVat) : ""// Идентификационен № по ДДС // TODO: if it has it дали контрагента е регистриран по ДДС
                , ownerIban // Банкова сметка

                // Получател
                , eikRecipient // Идентификационен Номер
                , vatNumberRecipient // Идентификационен № по ДДС

                // Таблица
                , powerPlant.getName()
                , electricityInvoice.getPeriodFrom().toString() // Период от
                , electricityInvoice.getPeriodTo().toString() // Период до
                , measurementUnitName // Loi Measurement Unit
                , electricityInvoice.getTotalQuantity().toString() // Quantity
                , priceInEurosName // Price in Euros
                , priceInLevsName // Price in Levs
                , electricityInvoice.getTotalSumInEuros().toString() // Total Sum in Euros
                , electricityInvoice.getTotalSumInLevs().toString() // Total Sum in Levs

                // Totals Table
                , electricityInvoice.getTotalSumInLevs().toString() // Данъчна основа в лева
                , electricityInvoice.getTotalSumInEuros().toString() // Данъчна основа в евро
                , isVatIncluded ? vatAmountInLevsName : "0" // ДДС в лева
                , isVatIncluded ? vatAmountInEurosName : "0" // ДДС в евро
                , isVatIncluded ? totalAmountInLevsWithVatName : electricityInvoice.getTotalSumInLevs().toString() // Обща стойност в лева
                , isVatIncluded ? totalAmountInEurosWithVatName : electricityInvoice.getTotalSumInEuros().toString() // Обща стойност в евро

                // Billed Sums
                , paymentAmountInLevsName // Сума за плащане в лева
                , paymentAmountInEurosName // Сума за плащане в евро
                , String.format("(%s)", recipientName) // Recipient Contact Name

                // ----------------- Копие -----------------
                , String.format("%s - САМОФАКТУРИРАНЕ", documentTypeName)

                // Доставчик
                , documentTypeName
                , electricityInvoice.getInvoiceNumber() // Фактура Номер
                , currentDateName // Current Date
                , electricityInvoice.getTaxEventDate().toString() // Tax Event Date
                , relationshipToInvoice
                , legalPersonName // Legal Person Name
                , legalPersonAddress // Legal Person Address
                , eikOrEgn // Идентификационен №
                , isVatIncluded ? String.format("Идентификационен № по ДДС: %s<br>", eikWithVat) : "" // Идентификационен № по ДДС - Legal Person // TODO: if it has it дали контрагента е регистриран по ДДС
                , ownerIban // Банкова сметка

                // Получател
                , eikRecipient // Идентификационен Номер
                , vatNumberRecipient // Идентификационен № по ДДС

                // Таблица
                , powerPlant.getName()
                , electricityInvoice.getPeriodFrom().toString() // Период от
                , electricityInvoice.getPeriodTo().toString() // Период до
                , measurementUnitName // Loi Measurement Unit
                , electricityInvoice.getTotalQuantity().toString() // Quantity
                , priceInEurosName // Price in Euros
                , priceInLevsName // Price in Levs
                , electricityInvoice.getTotalSumInEuros().toString() // Total Sum in Euros
                , electricityInvoice.getTotalSumInLevs().toString() // Total Sum in Levs

                // Totals Table
                , electricityInvoice.getTotalSumInLevs().toString() // Данъчна основа в лева
                , electricityInvoice.getTotalSumInEuros().toString() // Данъчна основа в евро
                , isVatIncluded ? vatAmountInLevsName : "0" // ДДС в лева
                , isVatIncluded ? vatAmountInEurosName : "0" // ДДС в евро
                , isVatIncluded ? totalAmountInLevsWithVatName : electricityInvoice.getTotalSumInLevs().toString() // Обща стойност в лева
                , isVatIncluded ? totalAmountInEurosWithVatName : electricityInvoice.getTotalSumInEuros().toString() // Обща стойност в евро

                // Billed Sums
                , paymentAmountInLevsName // Сума за плащане в лева
                , paymentAmountInEurosName // Сума за плащане в евро
                , String.format("(%s)", recipientName) // Recipient Contact Name
        };
    }


    public String generateDocumentNumber(
            Optional<PowerPlant> optionalPowerPlant, ElectricityInvoice electricityInvoice, Map<String, List<String>> invalidElectricityInvoiceRows
    ) {
        if (!optionalPowerPlant.isPresent()) { // If there is no such Power plant with this Access point
            invalidElectricityInvoiceRows.put(
                    formatPopulateElectricityInvoiceErrorMessageKey(
                            electricityInvoice.getReportingPointOwn(),
                            electricityInvoice.getPeriodFrom(),
                            electricityInvoice.getPeriodTo(),
                            electricityInvoice.getLoiDocumentType().getListOptionItemName()),
                    Arrays.asList(MISSING_POWER_PLANT)
            );
            return null;
        }

        // Fetch the Document type
        Optional<DocumentRange> optionalDocumentRange = optionalPowerPlant
                .get()
                .getOwner()
                .getDocumentRanges()
                .stream()
                .filter(DocumentRange::getIsActive) // Filter only the active ranges
                .sorted(Comparator.comparing(DocumentRange::getCreatedDate)) // Order by date ASC
                .findFirst(); // Get the first element

        if (!optionalDocumentRange.isPresent()) { // If there isn't an available document number
            invalidElectricityInvoiceRows.put(
                    formatPopulateElectricityInvoiceErrorMessageKey(
                            electricityInvoice.getReportingPointOwn(),
                            electricityInvoice.getPeriodFrom(),
                            electricityInvoice.getPeriodTo(),
                            electricityInvoice.getLoiDocumentType().getListOptionItemName()),
                    Arrays.asList(MISSING_VALID_DOCUMENT_RANGE)
            );
            return null;
        }

        optionalDocumentRange.get().setCurrent(optionalDocumentRange.get().getCurrent() + 1); // Set the number for the current document

        DocumentRange documentRange = (DocumentRange) entityPersistenceService
                .persistEntity(optionalDocumentRange.get(), true, true);

        return documentRange.getCurrent().toString();
    }
}
