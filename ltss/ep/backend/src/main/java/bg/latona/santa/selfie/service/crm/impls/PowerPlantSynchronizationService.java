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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


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
        List<PowerPlantSyncResult> results = new ArrayList<>();

        int successCount = 0;
        int failureCount = 0;

        for (PowerPlant pp : powerPlants) {
            try {
                PowerPlantSyncResult syncResult = doSynchronize(pp.getAccessPoint());
                results.add(syncResult);

                if (syncResult.isSuccess()) {
                    successCount++;
                } else {
                    failureCount++;
                }
            } catch (Exception ex) {
                failureCount++;
                results.add(new PowerPlantSyncResult(pp.getAccessPoint(), false, ex.getMessage()));
            }
        }

        return new SyncSummary<>(successCount, failureCount, results);
    }


    private PowerPlantSyncResult doSynchronize(String accessPoint) {
        try {
            synchronize(accessPoint);

            return new PowerPlantSyncResult(accessPoint, true, "Power Plant Synchronized Successfully");
        } catch (Exception e) {
            return new PowerPlantSyncResult(accessPoint, false, "An error occurred while synchronizing the power plant: " + e.getMessage());
        }
    }


    @Transactional
    public ResponseEntity<String> synchronize(String mpid) {
        try {
            VeiDTO veiDTO = crmClient.fetchVeiDtoByMpid(mpid);

            updatePowerPlant(veiDTO, mpid);

            return ResponseEntity.ok("Power Plant Synchronized Successfully");
        } catch (ReportException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while synchronizing the power plant: " + e.getMessage());
        }
    }


    public PowerPlant updatePowerPlant(VeiDTO veiDTO, String mpid) {
        PowerPlant powerPlant = dbPowerPlantService.getPowerPlantByAccessPoint(mpid)
                .orElseGet(() -> {
                    PowerPlant newPowerPlant = new PowerPlant();
                    newPowerPlant.setAccessPoint(mpid);

                    return newPowerPlant;
                });

        //TODO: check if there is no account set
        CustomerDTO customerDTO = veiDTO.getAccount();

        LegalPerson legalPerson = legalPersonSynchronizationService
                .updateLegalPerson(customerDTO, customerDTO.getAccountNumber());

        crmDtoMapping.toPowerPlant(powerPlant, veiDTO, legalPerson);

        entityPersistenceService.persistEntity(powerPlant, true, true);

        return powerPlant;
    }
}
