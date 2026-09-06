package bg.latona.santa.selfie.service.crm.impls;

import bg.latona.santa.entities.nepal.PowerPlant;
import bg.latona.santa.entities.person.LegalPerson;
import bg.latona.santa.reports.ReportException;
import bg.latona.santa.selfie.domain.crm.PowerPlantSyncResult;
import bg.latona.santa.selfie.domain.crm.SyncSummary;
import bg.latona.santa.selfie.dtos.Crm.CustomerDTO;
import bg.latona.santa.selfie.dtos.Crm.VeiDTO;
import bg.latona.santa.selfie.service.common.interfaces.EntityPersistenceService;
import bg.latona.santa.selfie.service.common.interfaces.entityRelated.DbPowerPlantService;
import bg.latona.santa.selfie.service.crm.interfaces.CrmClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class PowerPlantSynchronizationService {

    private final CrmClient crmClient;
    private final CrmDtoMapping crmDtoMapping;
    private final LegalPersonSynchronizationService legalPersonSynchronizationService;
    private final DbPowerPlantService dbPowerPlantService;
    private final EntityPersistenceService entityPersistenceService;

    @Transactional
    public SyncSummary<PowerPlantSyncResult> synchronizeAll() {
        List<PowerPlant> powerPlants = dbPowerPlantService.getAllPowerPlants();

        log.info("Starting synchronization for {} power plants", powerPlants.size());

        List<PowerPlantSyncResult> results = new ArrayList<>();


        int successCount = 0;
        int failureCount = 0;

        for (PowerPlant pp : powerPlants) {
            String accessPoint = pp.getAccessPoint();
            log.info("Starting synchronization for power plant with access point: {}", accessPoint);

            try {
                PowerPlantSyncResult syncResult = doSynchronize(accessPoint);
                results.add(syncResult);

                if (syncResult.isSuccess()) {
                    successCount++;

                    log.info("Successfully synchronized power plant with access point: {}", accessPoint);
                } else {
                    failureCount++;

                    log.warn("Failed synchronization for power plant with access point: {}. Reason: {}",
                            accessPoint, syncResult.getMessage());
                }
            } catch (Exception ex) {
                failureCount++;

                log.error("Exception occurred while synchronizing power plant with access point: {}. Exception: {}",
                        accessPoint, ex.getMessage(), ex);

                results.add(new PowerPlantSyncResult(accessPoint, false, ex.getMessage()));
            }
        }
        log.info("Completed power plant synchronization: {} successes, {} failures", successCount, failureCount);
        return new SyncSummary<>(successCount, failureCount, results);
    }

    private PowerPlantSyncResult doSynchronize(String accessPoint) {
        log.debug("Attempting synchronization for power plant with access point: {}", accessPoint);
        try {
            ResponseEntity<String> response = synchronize(accessPoint);

            log.debug("Received response from CRM for power plant {}: {}", accessPoint, response);

            return response.getStatusCode().is2xxSuccessful()
                    ? new PowerPlantSyncResult(accessPoint, true, response.getBody())
                    : new PowerPlantSyncResult(accessPoint, false, response.getBody());
        } catch (Exception e) {

            log.error("Error during synchronization for power plant with access point: {}. Exception: {}",
                    accessPoint, e.getMessage(), e);

            return new PowerPlantSyncResult(accessPoint, false,
                    "An error occurred while synchronizing the power plant: " + e.getMessage());
        }
    }

    public ResponseEntity<String> synchronize(String mpid) {
        log.info("Fetching VeiDTO for power plant with mpid: {}", mpid);
        try {
            VeiDTO veiDTO = crmClient.fetchVeiDtoByMpid(mpid);

            log.debug("Received VeiDTO for power plant with mpid {}: {}", mpid, veiDTO);

            updatePowerPlant(veiDTO, mpid);

            log.info("Power plant with mpid {} updated successfully", mpid);

            return ResponseEntity.ok("Power Plant Synchronized Successfully");
        } catch (ReportException e) {
            log.error("ReportException while synchronizing power plant with mpid {}: {}", mpid, e.getMessage(), e);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while synchronizing the power plant: " + e.getMessage());
        }
    }

    public void updatePowerPlant(VeiDTO veiDTO, String mpid) {
        log.info("Updating power plant with mpid: {}", mpid);

        PowerPlant powerPlant = dbPowerPlantService.getPowerPlantByAccessPoint(mpid)
                .orElseGet(() -> {
                    log.debug("No existing power plant found for mpid {}. Creating new instance.", mpid);

                    PowerPlant newPowerPlant = new PowerPlant();

                    newPowerPlant.setAccessPoint(mpid);

                    return newPowerPlant;
                });

        CustomerDTO customerDTO = veiDTO.getAccount();
        log.debug("Retrieved CustomerDTO for power plant mpid {}: {}", mpid, customerDTO);

        LegalPerson legalPerson = legalPersonSynchronizationService
                .updateLegalPerson(customerDTO, customerDTO.getAccountNumber());

        log.debug("LegalPerson updated for account: {}", customerDTO.getAccountNumber());

        crmDtoMapping.toPowerPlant(powerPlant, veiDTO, legalPerson);

        log.debug("Mapping to power plant complete for mpid: {}", mpid);

        try {
            entityPersistenceService.persistEntity(powerPlant, true, true);
            log.info("Persisted power plant with mpid: {}", mpid);
        } catch (Exception ex) {
            log.error("Error persisting power plant with mpid {}: {}", mpid, ex.getMessage(), ex);
            throw ex;
        }
    }
}
