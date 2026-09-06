package bg.latona.santa.selfie.service.downloadPdf.interfaces;

import bg.latona.santa.selfie.domain.ZipEntryData;

import java.util.List;

public interface ZipPDFService {
    byte[] createZipFromFiles(List<ZipEntryData> zipEntryData, String zipDestinationPath);
}