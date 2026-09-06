package bg.latona.santa.selfie.service.crm.impls;

import bg.latona.santa.selfie.dtos.Crm.*;
import bg.latona.santa.selfie.service.common.interfaces.entityRelated.*;
import bg.latona.santa.entities.selfie.AgreementSelfInvoicing;
import bg.latona.santa.entities.selfie.LoiAgreementStatus;
import bg.latona.santa.entities.selfie.LoiStatusCode;
import bg.latona.santa.entities.selfie.SelfInvoicingLine;
import bg.latona.santa.entities.nepal.LoiTypeOfPowerPlant;
import bg.latona.santa.entities.nepal.PowerPlant;
import bg.latona.santa.entities.person.LegalPerson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class CrmDtoMapping {

    private final DbLoiTypeOfPowerPlantService dbLoiTypeOfPowerPlantService;
    private final DbLoiReasonForTerminationService dbLoiReasonForTerminationService;
    private final DbLoiTypeOFServiceService dbLoiTypeOFServiceService;
    private final DbLoiAgreementStatusService dbLoiAgreementStatusService;
    private final DbLoiStatusCodeService dbLoiStatusCodeService;

    //--------------------------------------------------------------------------------
    // CustomerDTO -> LegalPerson
    //--------------------------------------------------------------------------------
    public LegalPerson toLegalPerson(LegalPerson legalPerson, CustomerDTO customerDto) {
        log.debug("Mapping CustomerDTO to LegalPerson. CustomerDTO: {}", customerDto);
        legalPerson.setName(customerDto.getName());
        legalPerson.setEgn(customerDto.getEgn());
        legalPerson.setSapNumber(customerDto.getSapNo());

        AddressDTO addressDTO = customerDto.getAddress();
        if (addressDTO == null) {
            log.debug("No address information found in CustomerDTO. Returning LegalPerson as is.");
            return legalPerson;
        }

        String address = String.format("%s %s",
                addressDTO.getStreetName(),
                addressDTO.getStreetText()
        ).trim();
        legalPerson.setAddress(address);
        legalPerson.setCity(addressDTO.getCityName());
        legalPerson.setPostCode(addressDTO.getPostalCode());

        log.debug("Completed mapping CustomerDTO to LegalPerson. Resulting LegalPerson: {}", legalPerson);
        return legalPerson;
    }

    //--------------------------------------------------------------------------------
    // VeiDTO -> PowerPlant
    //--------------------------------------------------------------------------------
    public PowerPlant toPowerPlant(PowerPlant powerPlant, VeiDTO veiDTO, LegalPerson legalPerson) {
        log.debug("Mapping VeiDTO to PowerPlant. VeiDTO: {}", veiDTO);

        powerPlant.setName(veiDTO.getVeiName());

        LoiTypeOfPowerPlant powerPlantType = dbLoiTypeOfPowerPlantService
                .getLoiTypeOfPowerPlantByItemCode(veiDTO.getVeiType());

        Optional.ofNullable(powerPlantType)
                .ifPresent(tp -> {
                    powerPlant.setType(tp);
                    log.debug("Mapped power plant type: {}", tp);
                });

        BigDecimal veiInstalledCapacityMwh = veiDTO.getVeiInstalledCapacityMwh();

        if (veiInstalledCapacityMwh != null) {
            BigDecimal capacity = veiInstalledCapacityMwh.setScale(4, RoundingMode.HALF_UP);
            powerPlant.setInstalledPowerMw(capacity);

            log.debug("Mapped installed capacity (MW): {}", capacity);
        }

        powerPlant.setOwner(legalPerson);
        powerPlant.setExternalNumber(veiDTO.getExternalNumber());

        log.debug("Completed mapping VeiDTO to PowerPlant. Resulting PowerPlant: {}", powerPlant);
        return powerPlant;
    }

    //--------------------------------------------------------------------------------
    // SelfInvoicingLineDTO -> SelfInvoicingLine
    //--------------------------------------------------------------------------------
    public SelfInvoicingLine toSelfInvoicingLine(SelfInvoicingLineDTO selfInvoicingLineDTO, AgreementSelfInvoicing agreementSelfInvoicing) {
        log.debug("Mapping SelfInvoicingLineDTO to SelfInvoicingLine. DTO: {}", selfInvoicingLineDTO);
        SelfInvoicingLine newSelfInvoicingLine = new SelfInvoicingLine();

        newSelfInvoicingLine.setActivatedOn(selfInvoicingLineDTO.getActivatedOn());
        newSelfInvoicingLine.setDeActivatedOn(selfInvoicingLineDTO.getDeactivatedOn());
        newSelfInvoicingLine.setBic(selfInvoicingLineDTO.getBic());
        newSelfInvoicingLine.setIban(selfInvoicingLineDTO.getIban());
        newSelfInvoicingLine.setInvoiceEmail(selfInvoicingLineDTO.getInvoiceEmail1());
        newSelfInvoicingLine.setInvoiceEmailSec(selfInvoicingLineDTO.getInvoiceEmail2());
        newSelfInvoicingLine.setOwnName(selfInvoicingLineDTO.getName());
        newSelfInvoicingLine.setSelfInvoicingLineIdCode(selfInvoicingLineDTO.getSelfInvoicingLineId());

        // Handle State Code and Status Code
        Optional.ofNullable(selfInvoicingLineDTO.getStateCode())
                .map(Long::valueOf)
                .flatMap(id -> {
                    log.debug("Attempting to map state code {} using DbLoiStatusCodeService", id);
                    return Optional.ofNullable(
                            dbLoiStatusCodeService.getLoiStatusCodeServiceByListOptionItemCode(id)
                    );
                })
                .ifPresent(loiStatusCode -> {
                    newSelfInvoicingLine.setStatusCode(loiStatusCode);
                    log.debug("Mapped state code to status code: {}", loiStatusCode);
                    boolean isActive = Objects.equals(loiStatusCode.getListOptionItemCode(), LoiStatusCode.ACTIVE);
                    newSelfInvoicingLine.setIsActiveStateCode(isActive);
                    log.debug("Set active state code flag to: {}", isActive);
                });

        newSelfInvoicingLine.setAgreementSelfInvoicing(agreementSelfInvoicing);
        log.debug("Completed mapping SelfInvoicingLineDTO to SelfInvoicingLine. Result: {}", newSelfInvoicingLine);
        return newSelfInvoicingLine;
    }

    //--------------------------------------------------------------------------------
    // AgreementSelfInvoicingDTO -> AgreementSelfInvoicing
    //--------------------------------------------------------------------------------
    public AgreementSelfInvoicing toAgreementSelfInvoicing(AgreementSelfInvoicingDTO agreementSelfInvoicingDTO, PowerPlant powerPlant) {
        log.debug("Mapping AgreementSelfInvoicingDTO to AgreementSelfInvoicing. DTO: {}", agreementSelfInvoicingDTO);
        AgreementSelfInvoicing newAgreement = new AgreementSelfInvoicing();

        newAgreement.setCreatedOn(agreementSelfInvoicingDTO.getCreatedOn());
        newAgreement.setAgreementSelfInvoicingIdCode(agreementSelfInvoicingDTO.getAgreementSelfInvoicingId());
        newAgreement.setAgreementStartDate(agreementSelfInvoicingDTO.getAgreementStartDate());
        newAgreement.setAgreementEndDate(agreementSelfInvoicingDTO.getAgreementEndDate());

        // Check the statement for VAT inclusion
        String VAT_INCLUDED_KEYWORD = "VAT included";
        if (VAT_INCLUDED_KEYWORD.equals(agreementSelfInvoicingDTO.getAgreementVatTypeText())) {
            newAgreement.setIsVatIncluded(true);
            newAgreement.setVatNumber(agreementSelfInvoicingDTO.getVatNumber());
            newAgreement.setVatRegistrationDate(agreementSelfInvoicingDTO.getVatRegistrationDate());
            log.debug("VAT is included. Set VAT number: {} and registration date: {}",
                    agreementSelfInvoicingDTO.getVatNumber(), agreementSelfInvoicingDTO.getVatRegistrationDate());
        } else {
            newAgreement.setIsVatIncluded(false);
            log.debug("VAT is not included.");
        }

        String ACCEPT_KEYWORD = "Accept";
        newAgreement.setIsClientResponseAccept(ACCEPT_KEYWORD.equals(agreementSelfInvoicingDTO.getClientResponseText()));
        log.debug("Set client response accept flag to: {}", newAgreement.getIsClientResponseAccept());

        newAgreement.setEgn(agreementSelfInvoicingDTO.getEgn());
        newAgreement.setOwnName(agreementSelfInvoicingDTO.getName());

        // Handle Reason for Termination
        Optional.ofNullable(agreementSelfInvoicingDTO.getReasonForTerminationId())
                .map(Long::valueOf)
                .flatMap(id -> {
                    log.debug("Attempting to map reason for termination with id: {}", id);
                    return Optional.ofNullable(
                            dbLoiReasonForTerminationService.getLoiReasonForTerminationByListOptionItemCode(id)
                    );
                })
                .ifPresent(reason -> {
                    newAgreement.setReasonForTermination(reason);
                    newAgreement.setTerminatedOn(agreementSelfInvoicingDTO.getTerminatedOn());
                    newAgreement.setTerminationDate(agreementSelfInvoicingDTO.getTerminationDate());
                    log.debug("Mapped reason for termination: {}", reason);
                });

        newAgreement.setSignedOn(agreementSelfInvoicingDTO.getSignedOn());

        // Handle Type of Service
        Optional.ofNullable(agreementSelfInvoicingDTO.getTypesOfServices())
                .map(Long::valueOf)
                .flatMap(id -> {
                    log.debug("Attempting to map type of service with id: {}", id);
                    return Optional.ofNullable(
                            dbLoiTypeOFServiceService.getLoiTypeOFServiceByListOptionItemCode(id)
                    );
                })
                .ifPresent(service -> {
                    newAgreement.setTypeOFService(service);
                    log.debug("Mapped type of service: {}", service);
                });

        // Handle Agreement Status
        Optional.ofNullable(agreementSelfInvoicingDTO.getStatusCode())
                .map(Long::valueOf)
                .flatMap(id -> {
                    log.debug("Attempting to map agreement status with id: {}", id);
                    return Optional.ofNullable(
                            dbLoiAgreementStatusService.getLoiAgreementStatusByListOptionItemCode(id)
                    );
                })
                .ifPresent(agreementStatus -> {
                    newAgreement.setAgreementStatus(agreementStatus);
                    boolean isActive = Objects.equals(agreementStatus.getListOptionItemCode(), LoiAgreementStatus.ACTIVE);
                    newAgreement.setIsStateCodeStatusActive(isActive);
                    log.debug("Mapped agreement status: {} with active flag: {}", agreementStatus, isActive);
                });

        newAgreement.setPowerPlant(powerPlant);
        log.debug("Completed mapping AgreementSelfInvoicingDTO to AgreementSelfInvoicing. Resulting Agreement: {}", newAgreement);
        return newAgreement;
    }
}
