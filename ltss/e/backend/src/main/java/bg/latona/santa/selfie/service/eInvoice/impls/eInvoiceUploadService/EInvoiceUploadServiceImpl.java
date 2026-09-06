package bg.latona.santa.selfie.service.eInvoice.impls.eInvoiceUploadService;

import bg.latona.santa.entities.DBFile;
import bg.latona.santa.entities.nepal.PowerPlant;
import bg.latona.santa.entities.person.LegalPerson;
import bg.latona.santa.entities.selfie.ElectricityInvoice;
import bg.latona.santa.reports.ReportException;
import bg.latona.santa.selfie.domain.UploadFileRequest;
import bg.latona.santa.selfie.domain.UploadZip;
import bg.latona.santa.selfie.domain.ZipEntryData;
import bg.latona.santa.selfie.dtos.eInvoiceXMLShipment.AttachedFileDTO;
import bg.latona.santa.selfie.dtos.eInvoiceXMLShipment.InvoiceDataDTO;
import bg.latona.santa.selfie.service.common.interfaces.EntityPersistenceService;
import bg.latona.santa.selfie.service.common.interfaces.entityRelated.DbElectricityInvoiceService;
import bg.latona.santa.selfie.service.common.interfaces.entityRelated.DbLegalPersonService;
import bg.latona.santa.selfie.service.eInvoice.interfaces.eInvoiceShipment.EInvoicePackagingService;
import bg.latona.santa.selfie.service.eInvoice.interfaces.eInvoiceShipment.EInvoiceShipmentXmlService;
import bg.latona.santa.selfie.service.eInvoice.interfaces.eInvoiceSoap.EInvoiceSoapService;
import bg.latona.santa.selfie.service.eInvoice.interfaces.eInvoiceUploadService.EInvoiceUploadService;
import bg.latona.santa.selfie.service.sap.interfaces.SAPPackagingService;
import bg.latona.santa.selfie.util.SHA1Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class EInvoiceUploadServiceImpl implements EInvoiceUploadService {

    private final EInvoiceShipmentXmlService eInvoiceShipmentXmlService;
    private final EInvoicePackagingService eInvoicePackagingService;
    private final EInvoiceSoapService eInvoiceSoapService;

    private final SAPPackagingService sapPackagingService;

    private final DbLegalPersonService dbLegalPersonService;
    private final DbElectricityInvoiceService dbElectricityInvoiceService;
    private final EntityPersistenceService entityPersistenceService;


    @Transactional
    public String uploadZipFileEInvoice(Set<String> uniqueAccessPoints) {
        Set<LegalPerson> legalPersonList = dbLegalPersonService.getAllDistinctLegalPersonsByAccessPoints(
                uniqueAccessPoints
        );

        return processUpload(legalPersonList, uniqueAccessPoints);
    }


    private String processUpload(
            Set<LegalPerson> legalPersonList,
            Set<String> uniqueAccessPoints
    ) {
        List<String> responses = new ArrayList<>();
        List<ElectricityInvoice> allElectricityInvoices = new ArrayList<>();

        legalPersonList.forEach(legalPerson -> {
            try {
                Set<ElectricityInvoice> electricityInvoices = getAllElectricityInvoicesForLegalPerson(
                        legalPerson.getPowerPlants(),
                        uniqueAccessPoints
                );

                allElectricityInvoices.addAll(electricityInvoices);

                Set<DBFile> dbFileList = getDbFileList(electricityInvoices);

                UploadFileRequest uploadFileRequest = createUploadFileRequest(dbFileList, legalPerson);
                String response = uploadZipToEInvoice(uploadFileRequest);

                responses.add(response);

            } catch (Exception e) {
                log.error("Reporting exception for legal person with ID {}: {}", legalPerson.getId(), e.getMessage(), e);
            }
        });

        if (responses.size() > 0) {
            markInvoicesAsSent(allElectricityInvoices);
            generateAllSAPXMLs(allElectricityInvoices);
        }

        return responses.toString();
    }


    public void markInvoicesAsSent(List<ElectricityInvoice> electricityInvoices) {
        try {
            electricityInvoices.forEach(invoice -> {
                invoice.setSent(true);
                entityPersistenceService.persistEntity(invoice, false, false);
            });

            log.info("Successfully marked {} invoices as sent", electricityInvoices.size());
        } catch (Exception e) {
            log.error("Error marking invoices as sent: {}", e.getMessage(), e);
            throw new ReportException("Failed to mark invoices as sent");
        }
    }


    private UploadFileRequest createUploadFileRequest(Set<DBFile> dbFileList, LegalPerson legalPerson) {
        InvoiceDataDTO invoiceData = eInvoiceShipmentXmlService.createInvoiceData(legalPerson);

        List<ZipEntryData> attachments = new ArrayList<>();
        List<AttachedFileDTO> attachedFilesTagDetails = invoiceData.getPresentationDetails().getAttachedFiles();

        for (DBFile dbFile : dbFileList) {
            attachments.add(new ZipEntryData(dbFile.getName(), dbFile.getContent()));
            String sha1Hash = SHA1Utils.calculateSHA1(dbFile.getContent());
            attachedFilesTagDetails.add(new AttachedFileDTO(dbFile.getName(), true, sha1Hash));
        }

        return new UploadFileRequest(invoiceData, attachments);
    }


    private String uploadZipToEInvoice(UploadFileRequest uploadFileRequest) throws Exception {
        UploadZip uploadZip = eInvoicePackagingService.createShipmentZip(
                uploadFileRequest.getInvoiceData(),
                uploadFileRequest.getAttachments()
        );

        log.info("Passing zip file bytes to uploadFile method");
        return eInvoiceSoapService.uploadFile(uploadZip);
    }


    private void generateAllSAPXMLs(List<ElectricityInvoice> electricityInvoices) {
        electricityInvoices.forEach(invoice -> {
            try {
                sapPackagingService.writeSapXmlFile(invoice);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Error generating SAP XML file for electricity invoice accessPoint {}: {}",
                        invoice.getReportingPointOwn(), e.getMessage(), e);

                throw new ReportException("Error generating SAP XML file for electricity invoice accessPoint");
            }
        });
    }


    private Set<ElectricityInvoice> getAllElectricityInvoicesForLegalPerson(
            List<PowerPlant> powerPlants,
            Set<String> uniqueAccessPoints
    ) {
         Set<String> filteredAccessPoints = powerPlants
                .stream()
                .map(PowerPlant::getAccessPoint)
                .filter(uniqueAccessPoints::contains)
                .collect(Collectors.toSet());

        if (filteredAccessPoints.isEmpty()) {
            return Collections.emptySet();
        }

        return dbElectricityInvoiceService
                .getAllByReportingPointOwnAndNotSentAndDbFileIsNotNull(
                        filteredAccessPoints
                );
    }


    private Set<DBFile> getDbFileList(Set<ElectricityInvoice> electricityInvoices) {
        return electricityInvoices
                .stream()
                .map(ElectricityInvoice::getDbFile)
                .collect(Collectors.toSet());
    }
}
