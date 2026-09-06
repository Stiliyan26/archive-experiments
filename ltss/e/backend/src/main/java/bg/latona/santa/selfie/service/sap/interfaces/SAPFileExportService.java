package bg.latona.santa.selfie.service.sap.interfaces;

import bg.latona.santa.selfie.domain.RequestBody.FileFilterRequest;

public interface SAPFileExportService {

    byte[] downloadSapFilesZip(FileFilterRequest fileFilterRequest);
}
