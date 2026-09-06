package bg.latona.santa.selfie.service.common.impls.fileManagment;

import bg.latona.santa.selfie.service.common.interfaces.fileManagment.FileService;
import bg.latona.santa.reports.ReportException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@RequiredArgsConstructor
@Service
public class FileServiceImpl implements FileService {

    @Override
    public void writeContentToFile(byte[] fileContent, String outputPath)  {
        try (FileOutputStream fileOutputStream = new FileOutputStream(outputPath)) {
            fileOutputStream.write(fileContent);
            fileOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            throw new ReportException(e.getMessage());
        }
    }

    @Override
    public String getDownloadsDirectory() {
        return System.getProperty("user.home") + File.separator + "Downloads";
    }

    @Override
    public String createDirectory(String folderName) {
        String outputPath = getDownloadsDirectory() + File.separator + folderName;
        Path directoryPath = Paths.get(outputPath);

        try {
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
                log.info("Directory {} successfully created", outputPath);
            }

            return outputPath;
        } catch (IOException e) {
            log.error("Failed to create directory: {}", outputPath, e);
            throw new ReportException("Could not create storage directory: " + e.getMessage());
        }
    }
}