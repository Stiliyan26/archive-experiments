package bg.latona.santa.selfie.service.zip.impls;
import bg.latona.santa.selfie.domain.ZipEntryData;
import bg.latona.santa.selfie.service.zip.interfaces.ZipService;
import bg.latona.santa.selfie.util.ZipUtils;
import bg.latona.santa.reports.ReportException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipOutputStream;


@Slf4j
@Service
public class ZipServiceImpl implements ZipService {
    private static final String XML_NAME_CONST = "billerInvoice.xml";
    private static final String ATTACHMENTS_DIR_CONST = "attachments/";
	@Value("${config.content}")
	private  String configFile;
    @Override
    public byte[] createZip(byte[] xmlBytes, List<ZipEntryData> attachments) {
        validateArguments(xmlBytes, attachments);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            addXmlToZip(zos, xmlBytes);

			addConfigFileToZip(zos);

            addAttachmentsToZip(zos, attachments);

            ZipUtils.closeZipOutputStream(zos);

			return baos.toByteArray();
        } catch (IOException e) {
            throw new ReportException("Failed to create ZIP file.");
        }
    }
    private void validateArguments(byte[] xmlBytes, List<ZipEntryData> attachments) {
        if (xmlBytes == null || attachments == null) {
            throw new ReportException("XML bytes or attachments cannot be null");
        }

        if (attachments.size() < 1) {
            throw new ReportException("Attachments cannot be empty");
        }
    }
    private void addXmlToZip(ZipOutputStream zos, byte[] xmlBytes) throws IOException {
        ZipUtils.addToZip(zos, XML_NAME_CONST, xmlBytes);
    }

	private void addConfigFileToZip(ZipOutputStream zos) throws IOException {

		// Get the additional file path from config
		String additionalFilePath = configFile;
		Path path = Paths.get(additionalFilePath);
		if (!Files.exists(path) || Files.isDirectory(path)) {
			throw new IOException("File at path " + additionalFilePath + " does not exist or is a directory.");
		}

		// Read and add the additional file
		byte[] additionalFileBytes = Files.readAllBytes(path);
		String additionalFileName = path.getFileName().toString();

		ZipUtils.addToZip(zos, additionalFileName, additionalFileBytes);
	}

    private void addAttachmentsToZip(ZipOutputStream zos, List<ZipEntryData> attachments) {
        attachments.forEach(attachment -> {
            try {
                String fileName = ATTACHMENTS_DIR_CONST + attachment.getName();

                ZipUtils.addToZip(zos, fileName, attachment.getContent());
            } catch (IOException e) {
                throw new ReportException("Error adding file to ZIP: " + attachment.getName());
            }
        });
    }
}