package bg.latona.santa.selfie.service.common.interfaces.entityRelated;

import bg.latona.santa.selfie.domain.RequestBody.FileFilterRequest;
import bg.latona.santa.entities.DBFile;

import java.util.List;

public interface DBFileService {

    List<DBFile> getDBFilesByIdsAndCurrentCompany(List<Long> dbFileIdList);

    List<DBFile> getAllDBFiles();

    DBFile getDBFileById(Long dbFileId);

    List<DBFile> getDbFilesByDownloadPdfFiltersAndDbFileIsNotNull(FileFilterRequest fileFilterRequest);
}
