package bg.latona.santa.selfie.service.crm.impls;

import bg.latona.santa.entities.BankAccount;
import bg.latona.santa.entities.nepal.PowerPlant;
import bg.latona.santa.entities.person.Contact;
import bg.latona.santa.entities.person.LegalPerson;
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
public class AgreementSelfInvoicingSynchronizationService {

    private final CrmClient crmClient;
    private final CrmDtoMapping crmDtoMapping;
    private final DbPowerPlantService dbPowerPlantService;
    private final EntityPersistenceService entityPersistenceService;

    @Transactional
    public SyncSummary<AgreementSelfInvoicingSyncResult> synchronizeAll() {
        List<PowerPlant> powerPlants = dbPowerPlantService.getAllPowerPlants();

        log.info("Starting agreement self invoicing synchronization for {} power plants", powerPlants.size());

        List<AgreementSelfInvoicingSyncResult> results = new ArrayList<>();

        int successCount = 0;
        int failureCount = 0;

        for (PowerPlant pp : powerPlants) {
            String accessPoint = pp.getAccessPoint();

            log.info("Starting synchronization for agreement self invoicing for power plant with access point: {}", accessPoint);

            try {
                AgreementSelfInvoicingSyncResult syncResult = doSynchronize(accessPoint);
                results.add(syncResult);

                if (syncResult.isSuccess()) {
                    successCount++;

                    log.info("Successfully synchronized agreement self invoicing for power plant with access point: {}", accessPoint);
                } else {
                    failureCount++;

                    log.warn("Failed synchronization for agreement self invoicing for power plant with access point: {}. Reason: {}",
                            accessPoint, syncResult.getMessage());
                }
            } catch (Exception ex) {
                failureCount++;

                log.error("Exception occurred while synchronizing agreement self invoicing for power plant with access point: {}. Exception: {}",
                        accessPoint, ex.getMessage(), ex);

                results.add(new AgreementSelfInvoicingSyncResult(accessPoint, false, ex.getMessage()));
            }
        }

        log.info("Completed agreement self invoicing synchronization: {} successes, {} failures", successCount, failureCount);

        return new SyncSummary<>(successCount, failureCount, results);
    }

    private AgreementSelfInvoicingSyncResult doSynchronize(String accessPoint) {
        log.debug("Attempting synchronization for agreement self invoicing for power plant with access point: {}", accessPoint);

        try {
            ResponseEntity<String> response = synchronize(accessPoint);

            log.debug("Received response from CRM for agreement self invoicing for power plant {}: {}", accessPoint, response);

            return response.getStatusCode().is2xxSuccessful()
                    ? new AgreementSelfInvoicingSyncResult(accessPoint, true, response.getBody())
                    : new AgreementSelfInvoicingSyncResult(accessPoint, false, response.getBody());

        } catch (Exception e) {
            log.error("Error during synchronization for agreement self invoicing for power plant with access point: {}. Exception: {}",
                    accessPoint, e.getMessage(), e);

            return new AgreementSelfInvoicingSyncResult(accessPoint, false,
                    "An error occurred while synchronizing the agreement self invoicing: " + e.getMessage());
        }
    }

    public ResponseEntity<String> synchronize(String mpid) {
        log.info("Fetching self invoicing lines for power plant with mpid: {}", mpid);
        try {
            List<SelfInvoicingLineDTO> selfInvoicingLineCollection = crmClient.fetchSelfInvoicingLineByMpid(mpid);

            log.debug("Received {} self invoicing lines for power plant with mpid: {}", selfInvoicingLineCollection.size(), mpid);

            updateAgreementSelfInvoicing(selfInvoicingLineCollection, mpid);

            log.info("Agreement self invoicing updated successfully for power plant with mpid: {}", mpid);

            return ResponseEntity.ok("Agreement self invoicing Synchronized Successfully");
        } catch (ReportException e) {
            log.error("ReportException while synchronizing agreement self invoicing for power plant with mpid {}: {}",
                    mpid, e.getMessage(), e);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while synchronizing the agreement self invoicing: " + e.getMessage());
        }
    }

    public void updateAgreementSelfInvoicing(List<SelfInvoicingLineDTO> selfInvoicingLineCollection, String mpid) {
        log.info("Updating agreement self invoicing for power plant with mpid: {}", mpid);

        PowerPlant powerPlant = dbPowerPlantService
                .getPowerPlantByAccessPoint(mpid)
                .orElseThrow(() -> {
                    String errorMsg = "No power plant found with this access point: " + mpid;
                    log.error(errorMsg);

                    return new ReportException(errorMsg);
                });

        for (SelfInvoicingLineDTO selfInvoicingLineDTO : selfInvoicingLineCollection) {
            log.debug("Processing self invoicing line DTO for power plant with mpid {}: {}", mpid, selfInvoicingLineDTO);
            try {
                AgreementSelfInvoicing newAgreement = crmDtoMapping
                        .toAgreementSelfInvoicing(selfInvoicingLineDTO.getAgreementSelfInvoicing(), powerPlant);

                log.debug("Mapped AgreementSelfInvoicing: {}", newAgreement);

                entityPersistenceService.persistEntity(newAgreement, true, true);

				LegalPerson legalPerson = powerPlant.getOwner();
				legalPerson.setVatNumber(newAgreement.getVatNumber());

				entityPersistenceService.persistEntity(legalPerson, true, true);

				//TODO Replace validateBankAccount and validateContact with drools rules
				validateBankAccount(selfInvoicingLineDTO, legalPerson);
				validateContact(selfInvoicingLineDTO, legalPerson);

				log.info("Persisted AgreementSelfInvoicing for power plant with mpid: {}", mpid);

                SelfInvoicingLine newSelfInvoicingLine = crmDtoMapping
                        .toSelfInvoicingLine(selfInvoicingLineDTO, newAgreement);

                log.debug("Mapped SelfInvoicingLine: {}", newSelfInvoicingLine);

                entityPersistenceService.persistEntity(newSelfInvoicingLine, true, true);

                log.info("Persisted SelfInvoicingLine for power plant with mpid: {}", mpid);
            } catch (Exception ex) {
                log.error("Error updating agreement self invoicing for power plant with mpid {}: {}",
                        mpid, ex.getMessage(), ex);
                throw ex;
            }
        }
    }

	private void validateBankAccount(SelfInvoicingLineDTO selfInvoicingLineDTO, LegalPerson legalPerson) {

		boolean existingBankAccount = legalPerson.getBankAccounts().size() > 0 && legalPerson.getBankAccounts()
						.stream()
								.anyMatch(bankAccount ->
										!bankAccount.isDeleted()
										&& bankAccount.getIban() != null
										&& bankAccount.getIban().equals(selfInvoicingLineDTO.getIban()));

		if (!existingBankAccount) {
			BankAccount bankAccount = new BankAccount();
			bankAccount.setIban(selfInvoicingLineDTO.getIban());
			bankAccount.setBic(selfInvoicingLineDTO.getBic());
			bankAccount.setBankAccountOwner(legalPerson);

			entityPersistenceService.persistEntity(bankAccount, true, true);
		}
	}

	private void validateContact(SelfInvoicingLineDTO selfInvoicingLineDTO, LegalPerson legalPerson) {

		boolean existingContact = legalPerson.getContacts().size() > 0 && legalPerson.getContacts()
				.stream()
				.anyMatch(contact ->
						!contact.isDeleted()
						&& contact.getEmail() != null
						&& contact.getEmail().equals(selfInvoicingLineDTO.getInvoiceEmail1() + ", " + selfInvoicingLineDTO.getInvoiceEmail2()));

		if (!existingContact) {
			Contact contact = new Contact();
			contact.setEmail(selfInvoicingLineDTO.getInvoiceEmail1() + ", " + selfInvoicingLineDTO.getInvoiceEmail2());
			contact.setPerson(legalPerson);

			entityPersistenceService.persistEntity(contact, true, true);
		}
	}
}
