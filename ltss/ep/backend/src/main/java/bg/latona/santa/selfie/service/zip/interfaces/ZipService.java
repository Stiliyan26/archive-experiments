package bg.latona.santa.selfie.service.zip.interfaces;

import bg.latona.santa.selfie.domain.ZipEntryData;

import java.util.List;

public interface ZipService {

    byte[] createZip(byte[] xmlBytes, List<ZipEntryData> attachments);
}
