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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Service
public class LegalPersonSynchronizationService {

    private final CrmClient crmClient;
    private final CrmDtoMapping crmDtoMapping;
    private final DbLegalPersonService dbLegalPersonService;
    private final EntityPersistenceService entityPersistenceService;

    public SyncSummary<LegalPersonSyncResult> synchronizeAll() {
        Set<LegalPerson> legalPeople = dbLegalPersonService.getAllLegalPeople();
        log.info("Starting synchronization for {} legal persons", legalPeople.size());

        List<LegalPersonSyncResult> results = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;

        for (LegalPerson lp : legalPeople) {
            String eik = lp.getEik();
            log.info("Starting synchronization for legal person with EIK: {}", eik);

            try {
                LegalPersonSyncResult syncResult = doSynchronize(eik);
                results.add(syncResult);

                if (syncResult.isSuccess()) {
                    successCount++;
                    log.info("Successfully synchronized legal person with EIK: {}", eik);
                } else {
                    failureCount++;
                    log.warn("Failed synchronization for legal person with EIK: {}. Reason: {}", eik, syncResult.getMessage());
                }

            } catch (Exception ex) {
                failureCount++;
                log.error("Exception occurred during synchronization for legal person with EIK: {}. Exception: {}", eik, ex.getMessage(), ex);
                results.add(new LegalPersonSyncResult(eik, false, ex.getMessage()));
            }
        }

        log.info("Completed legal persons synchronization: {} successes, {} failures", successCount, failureCount);
        return new SyncSummary<>(successCount, failureCount, results);
    }

    private LegalPersonSyncResult doSynchronize(String eik) {
        log.debug("Attempting to synchronize legal person with EIK: {}", eik);
        try {
            ResponseEntity<String> response = synchronize(eik);
            log.debug("Received CRM response for legal person EIK {}: Status - {}, Body - {}",
                    eik, response.getStatusCode(), response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                return new LegalPersonSyncResult(eik, true, response.getBody());
            } else {
                return new LegalPersonSyncResult(eik, false, response.getBody());
            }
        } catch (Exception e) {
            log.error("Error synchronizing legal person with EIK: {}. Exception: {}", eik, e.getMessage(), e);
            return new LegalPersonSyncResult(eik, false, "An error occurred while synchronizing the legal person: " + e.getMessage());
        }
    }

    public ResponseEntity<String> synchronize(String eik) {
        log.info("Fetching CustomerDTO from CRM for legal person with EIK: {}", eik);
        try {
            CustomerDTO customerDto = crmClient.fetchCustomerByEik(eik);
            log.debug("Fetched CustomerDTO for legal person EIK {}: {}", eik, customerDto);

            updateLegalPerson(customerDto, eik);
            log.info("Legal person with EIK {} updated successfully", eik);

            return ResponseEntity.ok("Legal Person Synchronized Successfully");
        } catch (ReportException e) {
            log.error("ReportException while synchronizing legal person with EIK {}: {}", eik, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while synchronizing the legal person: " + e.getMessage());
        }
    }

    public LegalPerson updateLegalPerson(CustomerDTO customerDto, String eik) {
        log.info("Updating legal person record for EIK: {}", eik);

        LegalPerson legalPerson = dbLegalPersonService.getLegalPersonByEik(eik)
                .orElseGet(() -> {
                    log.debug("No existing legal person found for EIK {}. Creating a new record.", eik);
                    LegalPerson newPerson = new LegalPerson();
                    newPerson.setEik(eik);
                    return newPerson;
                });

        // Map CRM DTO to legal person entity
        crmDtoMapping.toLegalPerson(legalPerson, customerDto);
        log.debug("Mapped legal person details for EIK {}: {}", eik, legalPerson);

        try {
            entityPersistenceService.persistEntity(legalPerson, true, true);
            log.info("Persisted legal person record for EIK: {}", eik);
        } catch (Exception ex) {
            log.error("Error persisting legal person record for EIK {}: {}", eik, ex.getMessage(), ex);
            throw ex;
        }

        return legalPerson;
    }
}
