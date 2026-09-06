package bg.latona.santa.selfie.service.common.interfaces.fileManagment;

import java.io.IOException;

public interface FileService {

    void writeContentToFile(byte[] fileContent, String outputPath) throws IOException;

    String getDownloadsDirectory();

    String createDirectory(String folderName);
}