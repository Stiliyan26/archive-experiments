package bg.latona.santa.selfie.service.downloadPdf.impls;

import bg.latona.santa.selfie.domain.RequestBody.FileFilterRequest;
import bg.latona.santa.selfie.domain.ZipEntryData;
import bg.latona.santa.selfie.service.common.interfaces.entityRelated.DBFileService;
import bg.latona.santa.selfie.service.common.interfaces.fileManagment.FileService;
import bg.latona.santa.selfie.service.downloadPdf.interfaces.PdfService;
import bg.latona.santa.selfie.service.downloadPdf.interfaces.ZipPDFService;
import bg.latona.santa.entities.DBFile;
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
public class PdfServiceImpl implements PdfService {

    private final FileService fileService;
    private final ZipPDFService zipPDFService;
    private final DBFileService dbFileService;


    @Override
    public byte[] downloadAndZipPdfs(FileFilterRequest fileFilterRequest) {

        List<DBFile> dbFiles = dbFileService.getDbFilesByDownloadPdfFiltersAndDbFileIsNotNull(fileFilterRequest);

        log.info("Starting to process {} PDF files", dbFiles.size());

        if (dbFiles.isEmpty()) {
            String errorMessage = "No PDF files to process";

            log.warn(errorMessage);
            throw new ReportException(errorMessage);
        }

        List<ZipEntryData> zipEntryDataList = new ArrayList<>();

        int successfullyProcessedFiles = 0;

        for (DBFile dbFile : dbFiles) {
            try {
                log.info("Processing PDF file: {}", dbFile.getName());

                zipEntryDataList.add(createZipEntryData(
                        dbFile.getName(),
                        dbFile.getContent()
                ));

                successfullyProcessedFiles++;
            } catch (Exception e) {
                log.error("Error processing PDF file {}: {}", dbFile.getName(), e.getMessage(), e);
            }
        }

        log.info("Processed {} out of {} PDF files successfully", successfullyProcessedFiles, zipEntryDataList.size());

        String zipPath = fileService.getDownloadsDirectory() + File.separator + "compiled_pdfs.zip";

        return zipPDFService.createZipFromFiles(zipEntryDataList, zipPath);
    }

    private ZipEntryData createZipEntryData(String fileName, byte[] fileContent) {
        return ZipEntryData.builder()
                .name(fileName)
                .content(fileContent)
                .build();
    }
}