package bg.latona.santa.selfie.service.sap.impls;

import bg.latona.santa.selfie.domain.RequestBody.FileFilterRequest;
import bg.latona.santa.selfie.domain.ZipEntryData;
import bg.latona.santa.selfie.service.common.interfaces.entityRelated.DbElectricityInvoiceService;
import bg.latona.santa.selfie.service.common.interfaces.entityRelated.DbSecUserService;
import bg.latona.santa.selfie.service.common.interfaces.fileManagment.FileService;
import bg.latona.santa.selfie.service.downloadPdf.interfaces.ZipPDFService;
import bg.latona.santa.selfie.service.sap.interfaces.SAPFileExportService;
import bg.latona.santa.entities.DBFile;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.selfie.ElectricityInvoice;
import bg.latona.santa.entities.selfie.SapXml;
import bg.latona.santa.reports.ReportException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@RequiredArgsConstructor
@Service
public class SAPFileExportServiceImpl implements SAPFileExportService {

    private final FileService fileService;
    private final ZipPDFService zipPDFService;

    private final DbSecUserService dbSecUserService;
    private final DbElectricityInvoiceService dbElectricityInvoiceService;


    public byte[] downloadSapFilesZip(FileFilterRequest fileFilterRequest) {

        List<ElectricityInvoice> electricityInvoices = getElectricityInvoices(fileFilterRequest);

        if (electricityInvoices.isEmpty()) {
            String errorMessage = "No files to process";

            log.warn(errorMessage);
            throw new ReportException(errorMessage);
        }

        List<ZipEntryData> zipEntryData = new ArrayList<>();

        electricityInvoices.forEach(elInvoice -> {
            DBFile dbFile = elInvoice.getDbFile();

            zipEntryData.add(createZipEntryData(
                    dbFile.getName(),
                    dbFile.getContent()
            ));

            SapXml sapXml = elInvoice.getSapXmls().get(0);

            zipEntryData.add(createZipEntryData(
                    sapXml.getDbFile().getName(),
                    sapXml.getDbFile().getContent()
            ));
        });

        String zipPath = fileService.getDownloadsDirectory() + File.separator + "sapFiles.zip";

        return zipPDFService.createZipFromFiles(zipEntryData, zipPath);
    }

    private List<ElectricityInvoice> getElectricityInvoices(FileFilterRequest fileFilterRequest) {
        ManagedCompany company = dbSecUserService.getCurrentUserManagedCompany();

        return dbElectricityInvoiceService
                .getElectricityInvoicesByTaxDateBetweenAccessPointDocTypeAndAgreementTypeAndDbFileIsNotNull(
                        fileFilterRequest.getFromDate(),
                        fileFilterRequest.getToDate(),
                        fileFilterRequest.getDocumentTypeCodes(),
                        fileFilterRequest.getAgreementTypeCodes(),
                        fileFilterRequest.getAccessPoints(),
                        company,
                        false
                );
    }

    private ZipEntryData createZipEntryData(String fileName, byte[] fileContent) {
        return ZipEntryData.builder()
                .name(fileName)
                .content(fileContent)
                .build();
    }
}
