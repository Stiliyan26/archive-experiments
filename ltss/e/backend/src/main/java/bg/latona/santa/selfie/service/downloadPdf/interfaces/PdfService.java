package bg.latona.santa.selfie.service.downloadPdf.interfaces;

import bg.latona.santa.selfie.domain.RequestBody.FileFilterRequest;


public interface PdfService {
    byte[] downloadAndZipPdfs(FileFilterRequest fileFilterRequest);
}