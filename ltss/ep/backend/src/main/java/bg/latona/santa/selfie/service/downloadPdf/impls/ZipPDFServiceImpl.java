package bg.latona.santa.selfie.service.downloadPdf.impls;

import bg.latona.santa.selfie.domain.ZipEntryData;
import bg.latona.santa.selfie.service.common.interfaces.fileManagment.FileService;
import bg.latona.santa.selfie.service.downloadPdf.interfaces.ZipPDFService;
import bg.latona.santa.selfie.util.ZipUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;
import java.util.zip.ZipOutputStream;

@Slf4j
@RequiredArgsConstructor
@Service
public class ZipPDFServiceImpl implements ZipPDFService {

    private final FileService fileService;

    @Override
    public byte[] createZipFromFiles(List<ZipEntryData> zipEntryDataList, String zipDestinationPath) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            for (ZipEntryData zipEntryData : zipEntryDataList) {

                if (zipEntryData.getContent().length == 0) {
                    log.error("File is empty, skipping: {}", zipEntryData.getName());
                    continue;
                }

                ZipUtils.addToZip(zos, zipEntryData.getName(), zipEntryData.getContent());
            }

            ZipUtils.closeZipOutputStream(zos);
            // Save the ZIP file to the specified path
            fileService.writeContentToFile(baos.toByteArray(), zipDestinationPath);

            log.info("ZIP file created and saved successfully at: {}", zipDestinationPath);
            return baos.toByteArray();

        } catch (IOException e) {
            log.error("Error creating ZIP file: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create ZIP file.", e);
        }
    }
}