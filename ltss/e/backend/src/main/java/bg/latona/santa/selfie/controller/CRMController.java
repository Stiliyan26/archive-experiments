package bg.latona.santa.selfie.controller;

import bg.latona.santa.selfie.domain.RequestBody.LegalPersonSynchronizeRequest;
import bg.latona.santa.selfie.domain.RequestBody.SynchronizeRequest;
import bg.latona.santa.selfie.domain.crm.AgreementSelfInvoicingSyncResult;
import bg.latona.santa.selfie.domain.crm.CrmRequestMock;
import bg.latona.santa.selfie.domain.crm.LegalPersonSyncResult;
import bg.latona.santa.selfie.domain.crm.PowerPlantSyncResult;
import bg.latona.santa.selfie.dtos.Crm.CustomerDTO;
import bg.latona.santa.selfie.dtos.Crm.SelfInvoicingLineDTO;
import bg.latona.santa.selfie.dtos.Crm.VeiDTO;
import bg.latona.santa.selfie.service.crm.interfaces.CrmMockEndpointService;
import bg.latona.santa.selfie.service.crm.interfaces.CrmMockService;
import bg.latona.santa.selfie.service.crm.interfaces.CrmSynchronizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@CrossOrigin
@RestController
@RequestMapping(value = "/api/reports")
public class CRMController {

    private static final String X_API_KEY = "X-Api-Key";

    @Value("${crm.x-auth-key}")
    private String AUTH_KEY_VALUE;


    private final CrmMockEndpointService crmMockEndpointService;
    private final CrmSynchronizationService crmSynchronizationService;
    private final CrmMockService crmMockService;


    //TODO: remove crm endpoints
    @GetMapping(value = "${crm.endpoint.url.findCustomerByEik}")
    public ResponseEntity<?> findCustomerByEik(
            @RequestHeader(X_API_KEY) String authKeyValue,
            @RequestParam("eik") String eik
    ) {
        log.info("Received request to find customer by EIK: {}", eik);

        if (!AUTH_KEY_VALUE.equals(authKeyValue)) {
            log.warn("Unauthorized access attempt with invalid API Key: {}", authKeyValue);

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid API Key");
        }

        try {
            CustomerDTO customerDTO = crmMockEndpointService.findCustomerByEik();

            log.info("Successfully fetched customer for EIK: {}", eik);

            return ResponseEntity.ok(customerDTO);
        } catch (Exception e) {
            log.error("Error fetching customer for EIK: {}. Exception: {}", eik, e.getMessage(), e);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching customer details");
        }
    }


    @GetMapping("${crm.endpoint.url.findMeteringPointByNumber}")
    public ResponseEntity<?> findMeteringPointByNumber(
            @RequestHeader(X_API_KEY) String authKeyValue,
            @RequestParam("mpid") String mpid
    ) {
        log.info("Received request to find Power Plant by access point: {}", mpid);

        if (!AUTH_KEY_VALUE.equals(authKeyValue)) {
            log.warn("Unauthorized access attempt with invalid API Key: {}", authKeyValue);

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid API Key");
        }

        try {
            VeiDTO crmDataResponse = crmMockEndpointService.findMeteringPointByNumber();

            log.info("Successfully fetched power plant for access point: {}", mpid);

            return ResponseEntity.ok(crmDataResponse);
        } catch (Exception e) {
            log.error("Error fetching power plant for access point: {}. Exception: {}", mpid, e.getMessage(), e);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching power plant details");
        }
    }


    @GetMapping("${crm.endpoint.url.getSelfInvoicingLine}")
    public ResponseEntity<?> getSelfInvoicingLine(
            @RequestHeader(X_API_KEY) String authKeyValue,
            @RequestParam("mpid") String mpid
    ) {
        log.info("Received request to find Power Plant by access point: {}", mpid);

        if (!AUTH_KEY_VALUE.equals(authKeyValue)) {
            log.warn("Unauthorized access attempt with invalid API Key: {}", authKeyValue);

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid API Key");
        }

        try {
            List<SelfInvoicingLineDTO> crmDataResponse = crmMockEndpointService.getSelfInvoicingLine();

            log.info("Successfully fetched agreements self invoicing for access point: {}", mpid);

            return ResponseEntity.ok(crmDataResponse);
        } catch (Exception e) {
            log.error("Error fetching power plant for access point: {}. Exception: {}", mpid, e.getMessage(), e);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching agreements self invoicing details");
        }

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

    @PostMapping(value = "/CRMData",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> mockCRMData(@RequestBody CrmRequestMock crmRequestMock) {
        return crmMockService.createOrUpdateCRMData(crmRequestMock);
    }
}
