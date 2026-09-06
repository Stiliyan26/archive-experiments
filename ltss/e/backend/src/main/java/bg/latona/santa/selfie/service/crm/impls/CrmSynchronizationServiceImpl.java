package bg.latona.santa.selfie.service.crm.impls;

import bg.latona.santa.selfie.domain.crm.AgreementSelfInvoicingSyncResult;
import bg.latona.santa.selfie.domain.crm.LegalPersonSyncResult;
import bg.latona.santa.selfie.domain.crm.PowerPlantSyncResult;
import bg.latona.santa.selfie.domain.crm.SyncSummary;
import bg.latona.santa.selfie.service.crm.interfaces.CrmSynchronizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class CrmSynchronizationServiceImpl implements CrmSynchronizationService {

    private final LegalPersonSynchronizationService legalPersonSynchronizationService;
    private final PowerPlantSynchronizationService powerPlantSynchronizationService;
    private final AgreementSelfInvoicingSynchronizationService agreementSelfInvoicingSynchronizationService;


    @Override
    public ResponseEntity<String> synchronizeLegalPerson(String eik) {
        return legalPersonSynchronizationService.synchronize(eik);
    }


    @Override
    public ResponseEntity<String> synchronizePowerPlant(String mpid) {
        return powerPlantSynchronizationService.synchronize(mpid);
    }


    @Override
    public ResponseEntity<String> synchronizeAgreementSelfInvoicing(String mpid) {
        return agreementSelfInvoicingSynchronizationService.synchronize(mpid);
    }


    @Override
    public ResponseEntity<List<LegalPersonSyncResult>> synchronizeAllLegalPeople() {

        SyncSummary<LegalPersonSyncResult> summary = legalPersonSynchronizationService.synchronizeAll();

        HttpStatus overallStatus = summary.determineStatus();

        return ResponseEntity
                .status(overallStatus)
                .body(summary.getResults());
    }


    @Override
    public ResponseEntity<List<PowerPlantSyncResult>> synchronizeAllPowerPlants() {
        SyncSummary<PowerPlantSyncResult> summary = powerPlantSynchronizationService.synchronizeAll();

        HttpStatus overallStatus = summary.determineStatus();

        return ResponseEntity
                .status(overallStatus)
                .body(summary.getResults());
    }


    @Override
    public ResponseEntity<List<AgreementSelfInvoicingSyncResult>> synchronizeAllAgreementsSelfInvoicing() {
        SyncSummary<AgreementSelfInvoicingSyncResult> summary = agreementSelfInvoicingSynchronizationService.synchronizeAll();

        HttpStatus overallStatus = summary.determineStatus();

        return ResponseEntity
                .status(overallStatus)
                .body(summary.getResults());
    }
}
