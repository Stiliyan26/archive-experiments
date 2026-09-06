package bg.latona.santa.selfie.service.crm.impls;

import bg.latona.santa.entities.person.LegalPerson;
import bg.latona.santa.reports.ReportException;
import bg.latona.santa.selfie.domain.crm.LegalPersonSyncResult;
import bg.latona.santa.selfie.domain.crm.SyncSummary;
import bg.latona.santa.selfie.dtos.Crm.CustomerDTO;
import bg.latona.santa.selfie.service.common.interfaces.EntityPersistenceService;
import bg.latona.santa.selfie.service.common.interfaces.entityRelated.DbLegalPersonService;
import bg.latona.santa.selfie.service.crm.interfaces.CrmClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class LegalPersonSynchronizationService {

    private final CrmClient crmClient;
    private final CrmDtoMapping crmDtoMapping;

    private final DbLegalPersonService dbLegalPersonService;
    private final EntityPersistenceService entityPersistenceService;

    @Transactional
    public SyncSummary<LegalPersonSyncResult> synchronizeAll() {

        Set<LegalPerson> legalPeople = dbLegalPersonService.getAllLegalPeople();

        List<LegalPersonSyncResult> results = new ArrayList<>();

        int successCount = 0;
        int failureCount = 0;

        for (LegalPerson lp : legalPeople) {
            try {
                LegalPersonSyncResult syncResult = doSynchronize(lp.getEik());
                results.add(syncResult);

                if (syncResult.isSuccess()) {
                    successCount++;
                } else {
                    failureCount++;
                }

            } catch (Exception ex) {
                failureCount++;
                results.add(new LegalPersonSyncResult(lp.getEik(), false, ex.getMessage()));
            }
        }

        return new SyncSummary<>(successCount, failureCount, results);
    }

    private LegalPersonSyncResult doSynchronize(String eik) {
        try {
            synchronize(eik);

            return new LegalPersonSyncResult(eik, true, "Legal Person Synchronized Successfully");
        } catch (Exception e) {
            return new LegalPersonSyncResult(eik, false, "An error occurred while synchronizing the legal person: " + e.getMessage());
        }
    }


    @Transactional
    public ResponseEntity<String> synchronize(String eik)  {
        try {
            CustomerDTO customerDto = crmClient.fetchCustomerByEik(eik);

            updateLegalPerson(customerDto, eik);

            return ResponseEntity.ok("Legal Person Synchronized Successfully");
        } catch (ReportException e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while synchronizing the legal person: " + e.getMessage());
        }
    }


    public LegalPerson updateLegalPerson(CustomerDTO customerDto, String eik) {

        LegalPerson legalPerson = dbLegalPersonService
                .getLegalPersonByEik(eik)
                .orElseGet(() -> {
                    LegalPerson newPerson = new LegalPerson();
                    newPerson.setEik(eik);

                    return newPerson;
                });

        crmDtoMapping.toLegalPerson(legalPerson, customerDto);

        entityPersistenceService.persistEntity(legalPerson, true, true);

        return legalPerson;
    }
}
