package bg.latona.santa.selfie.controller;

import bg.latona.santa.selfie.domain.RequestBody.LegalPersonSynchronizeRequest;
import bg.latona.santa.selfie.domain.RequestBody.SynchronizeRequest;
import bg.latona.santa.selfie.domain.crm.AgreementSelfInvoicingSyncResult;
import bg.latona.santa.selfie.domain.crm.LegalPersonSyncResult;
import bg.latona.santa.selfie.domain.crm.PowerPlantSyncResult;
import bg.latona.santa.selfie.dtos.Crm.CustomerDTO;
import bg.latona.santa.selfie.dtos.Crm.SelfInvoicingLineDTO;
import bg.latona.santa.selfie.dtos.Crm.VeiDTO;
import bg.latona.santa.selfie.service.crm.interfaces.CrmMockEndpointService;
import bg.latona.santa.selfie.service.crm.interfaces.CrmSynchronizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@CrossOrigin
@RestController
@RequestMapping(value = "/api/reports")
public class CRMController {

    private static final String X_AUTH_KEY = "X-Auth-Key";

    @Value("${crm.x-auth-key}")
    private String AUTH_KEY_VALUE;


    private final CrmMockEndpointService crmMockEndpointService;
    private final CrmSynchronizationService crmSynchronizationService;


    //TODO: remove crm endpoints
    @GetMapping(value = "${crm.endpoint.url.findCustomerByEik}")
    public ResponseEntity<?> findCustomerByEik(
            @RequestHeader(X_AUTH_KEY) String authKeyValue,
            @RequestParam("eik") String eik
    ) {
        if (!AUTH_KEY_VALUE.equals(authKeyValue)) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid API Key");
        }

        CustomerDTO customerDTO = crmMockEndpointService.findCustomerByEik();

        return ResponseEntity.ok(customerDTO);
    }

    @GetMapping("${crm.endpoint.url.findMeteringPointByNumber}")
    public ResponseEntity<?> findMeteringPointByNumber(
            @RequestHeader(X_AUTH_KEY) String authKeyValue,
            @RequestParam("mpid") String mpid
    ) {
        if (!AUTH_KEY_VALUE.equals(authKeyValue)) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid API Key");
        }

        VeiDTO crmDataResponse = crmMockEndpointService.findMeteringPointByNumber();

        return ResponseEntity.ok(crmDataResponse);
    }

    @GetMapping("${crm.endpoint.url.getSelfInvoicingLine}")
    public ResponseEntity<?> getSelfInvoicingLine(
            @RequestHeader(X_AUTH_KEY) String authKeyValue,
            @RequestParam("mpid") String mpid
    ) {
        if (!AUTH_KEY_VALUE.equals(authKeyValue)) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid API Key");
        }

        List<SelfInvoicingLineDTO> crmDataResponse = crmMockEndpointService.getSelfInvoicingLine();

        return ResponseEntity.ok(crmDataResponse);
    }


    @PostMapping("/legal-person/synchronize")
    public ResponseEntity<?> synchronizeLegalPerson(@RequestBody LegalPersonSynchronizeRequest request) {
        return crmSynchronizationService.synchronizeLegalPerson(request.getEik());
    }


    @PostMapping("/power-plant/synchronize")
    public ResponseEntity<?> synchronizePowerPlant(@RequestBody SynchronizeRequest request) {
        return crmSynchronizationService.synchronizePowerPlant(request.getMpid());
    }


    @PostMapping("/agreement-self-invoicing/synchronize")
    public ResponseEntity<?> synchronizeAgreementSelfInvoicing(@RequestBody SynchronizeRequest request) {
        return crmSynchronizationService.synchronizeAgreementSelfInvoicing(request.getMpid());
    }


    @PostMapping("/legal-people/synchronizations")
    public ResponseEntity<List<LegalPersonSyncResult>> synchronizeAllLegalPeople() {
        return crmSynchronizationService.synchronizeAllLegalPeople();
    }


    @PostMapping("/power-plants/synchronizations")
    public ResponseEntity<List<PowerPlantSyncResult>> synchronizeAllPowerPlants() {
        return crmSynchronizationService.synchronizeAllPowerPlants();
    }


    @PostMapping("/agreements-self-invoicing/synchronizations")
    public ResponseEntity<List<AgreementSelfInvoicingSyncResult>> synchronizeAllAgreementsSelfInvoicing() {
        return crmSynchronizationService.synchronizeAllAgreementsSelfInvoicing();
    }
}
