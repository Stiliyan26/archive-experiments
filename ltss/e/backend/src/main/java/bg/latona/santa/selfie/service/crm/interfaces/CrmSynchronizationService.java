package bg.latona.santa.selfie.service.crm.interfaces;

import bg.latona.santa.selfie.domain.crm.AgreementSelfInvoicingSyncResult;
import bg.latona.santa.selfie.domain.crm.LegalPersonSyncResult;
import bg.latona.santa.selfie.domain.crm.PowerPlantSyncResult;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface CrmSynchronizationService {

    ResponseEntity<String> synchronizeLegalPerson(String eik);


    ResponseEntity<String> synchronizePowerPlant(String mpid);


    ResponseEntity<String> synchronizeAgreementSelfInvoicing(String mpid);


    ResponseEntity<List<LegalPersonSyncResult>> synchronizeAllLegalPeople();


    ResponseEntity<List<PowerPlantSyncResult>> synchronizeAllPowerPlants();


    ResponseEntity<List<AgreementSelfInvoicingSyncResult>> synchronizeAllAgreementsSelfInvoicing();
}
