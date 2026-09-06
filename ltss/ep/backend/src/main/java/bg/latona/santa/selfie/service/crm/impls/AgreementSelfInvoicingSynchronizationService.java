package bg.latona.santa.selfie.service.crm.impls;

import bg.latona.santa.entities.nepal.PowerPlant;
import bg.latona.santa.entities.selfie.AgreementSelfInvoicing;
import bg.latona.santa.entities.selfie.SelfInvoicingLine;
import bg.latona.santa.reports.ReportException;
import bg.latona.santa.selfie.domain.crm.AgreementSelfInvoicingSyncResult;
import bg.latona.santa.selfie.domain.crm.SyncSummary;
import bg.latona.santa.selfie.dtos.Crm.SelfInvoicingLineDTO;
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
public class AgreementSelfInvoicingSynchronizationService {

    private final CrmClient crmClient;
    private final CrmDtoMapping crmDtoMapping;

    private final DbPowerPlantService dbPowerPlantService;
    private final EntityPersistenceService entityPersistenceService;


    @Transactional
    public SyncSummary<AgreementSelfInvoicingSyncResult> synchronizeAll() {

        List<PowerPlant> powerPlants = dbPowerPlantService.getAllPowerPlants();
        List<AgreementSelfInvoicingSyncResult> results = new ArrayList<>();

        int successCount = 0;
        int failureCount = 0;

        for (PowerPlant pp : powerPlants) {
            try {
                AgreementSelfInvoicingSyncResult syncResult = doSynchronize(pp.getAccessPoint());
                results.add(syncResult);

                if (syncResult.isSuccess()) {
                    successCount++;
                } else {
                    failureCount++;
                }
            } catch (Exception ex) {
                failureCount++;
                results.add(new AgreementSelfInvoicingSyncResult(pp.getAccessPoint(), false, ex.getMessage()));
            }
        }

        return new SyncSummary<>(successCount, failureCount, results);
    }


    private AgreementSelfInvoicingSyncResult doSynchronize(String accessPoint) {
        try {
            synchronize(accessPoint);

            return new AgreementSelfInvoicingSyncResult(accessPoint, true, "Agreement self invoicing Synchronized Successfully");
        } catch (Exception e) {
            return new AgreementSelfInvoicingSyncResult(accessPoint, false, "An error occurred while synchronizing the agreement self invoicing: " + e.getMessage());
        }
    }


    @Transactional
    public ResponseEntity<String> synchronize(String mpid) {
        try {
            List<SelfInvoicingLineDTO> selfInvoicingLineCollection = crmClient.fetchSelfInvoicingLineByMpid(mpid);

            updateAgreementSelfInvoicing(selfInvoicingLineCollection, mpid);

            return ResponseEntity.ok("Agreement self invoicing Synchronized Successfully");
        } catch (ReportException e) {

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while synchronizing the agreement self invoicing: " + e.getMessage());
        }
    }


    public void updateAgreementSelfInvoicing(List<SelfInvoicingLineDTO> selfInvoicingLineCollection, String mpid) {

        PowerPlant powerPlant = dbPowerPlantService
                .getPowerPlantByAccessPoint(mpid)
                .orElseThrow(() -> new ReportException("No power plant found with this access point: " + mpid));

        for (SelfInvoicingLineDTO selfInvoicingLineDTO : selfInvoicingLineCollection) {

            AgreementSelfInvoicing newAgreement = crmDtoMapping
                    .toAgreementSelfInvoicing(selfInvoicingLineDTO.getAgreementSelfInvoicing(), powerPlant);

            entityPersistenceService.persistEntity(newAgreement, true, true);

            SelfInvoicingLine newSelfInvoicingLine = crmDtoMapping
                    .toSelfInvoicingLine(selfInvoicingLineDTO, newAgreement);

            entityPersistenceService.persistEntity(newSelfInvoicingLine, true, true);
        }
    }
}
