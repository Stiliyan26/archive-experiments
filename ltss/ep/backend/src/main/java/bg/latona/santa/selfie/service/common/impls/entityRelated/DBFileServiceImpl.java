package bg.latona.santa.selfie.service.common.impls.entityRelated;

import bg.latona.santa.selfie.domain.RequestBody.FileFilterRequest;
import bg.latona.santa.selfie.service.common.interfaces.entityRelated.DBFileService;
import bg.latona.santa.selfie.service.common.interfaces.entityRelated.DbElectricityInvoiceService;
import bg.latona.santa.selfie.service.common.interfaces.entityRelated.DbSecUserService;
import bg.latona.santa.entities.DBFile;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.selfie.ElectricityInvoice;
import bg.latona.santa.repositories.DBFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class DBFileServiceImpl implements DBFileService {

    private final DBFileRepository dbFileRepository;

    private final DbSecUserService dbSecUserService;
    private final DbElectricityInvoiceService dbElectricityInvoiceService;

    @Override
    public List<DBFile> getDBFilesByIdsAndCurrentCompany(List<Long> dbFileIdList) {
        ManagedCompany company = dbSecUserService.getCurrentUserManagedCompany();
        log.info("Fetching DB files for company: {} with file IDs: {}", company.getName(), dbFileIdList);

        List<DBFile> dbFileList = dbFileRepository.findByIdInAndCompanyAndDeleted(dbFileIdList, company, false);

        log.info("Retrieved {} DB files for company: {}", dbFileList.size(), company.getName());
        return dbFileList;
    }

    @Override
    public List<DBFile> getAllDBFiles() {
        ManagedCompany company = dbSecUserService.getCurrentUserManagedCompany();
        dbFileRepository.findAllByCompanyAndDeleted(company, false);

        return dbFileRepository.findAllByCompanyAndDeleted(company, false);
    }

    @Override
    public DBFile getDBFileById(Long dbFileId) {
        return dbFileRepository.findFirstById(dbFileId);
    }

    @Override
    public List<DBFile> getDbFilesByDownloadPdfFiltersAndDbFileIsNotNull(FileFilterRequest fileFilterRequest) {
        ManagedCompany company = dbSecUserService.getCurrentUserManagedCompany();

        List<ElectricityInvoice> electricityInvoices = getElectricityInvoicesByTaxDateBetweenAccessPointDocTypeAndDbFileIsNotNull(
                fileFilterRequest, company
        );

        List<DBFile> dbFileList = electricityInvoices.stream()
                .map(ElectricityInvoice::getDbFile)
                .collect(Collectors.toList());

        log.info("Retrieved {} DB files for company: {}", dbFileList.size(), company.getName());

        return dbFileList;
    }

    private List<ElectricityInvoice> getElectricityInvoicesByTaxDateBetweenAccessPointDocTypeAndDbFileIsNotNull(
            FileFilterRequest fileFilterRequest,
            ManagedCompany company
    ) {
        log.info("Fetching DB files for company: {} with taxEventDate between {} and {}", company.getName(), fileFilterRequest.getFromDate(), fileFilterRequest.getToDate());

        return dbElectricityInvoiceService.getElectricityInvoicesByTaxDateBetweenAccessPointDocTypeAndAgreementTypeAndDbFileIsNotNull(
                fileFilterRequest.getFromDate(),
                fileFilterRequest.getToDate(),
                fileFilterRequest.getDocumentTypeCodes(),
                fileFilterRequest.getAgreementTypeCodes(),
                fileFilterRequest.getAccessPoints(),
                company,
                false
        );
    }
}
