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
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.Optional;

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
        legalPerson.setName(customerDto.getName());
        legalPerson.setEgn(customerDto.getEgn());
        legalPerson.setSapNumber(customerDto.getSapNo());

        AddressDTO addressDTO = customerDto.getAddress();

        String address = String.format("%s %s",
                addressDTO.getStreetName(),
                addressDTO.getStreetText()
        ).trim();

        legalPerson.setAddress(address);
        legalPerson.setCity(addressDTO.getCityName());
        legalPerson.setPostCode(addressDTO.getPostalCode());

        return legalPerson;
    }


    //--------------------------------------------------------------------------------
    // VeiDTO -> PowerPlant
    //--------------------------------------------------------------------------------
    public PowerPlant toPowerPlant(PowerPlant powerPlant, VeiDTO veiDTO, LegalPerson legalPerson) {
        powerPlant.setName(veiDTO.getVeiName());

        LoiTypeOfPowerPlant powerPlantType = dbLoiTypeOfPowerPlantService
                .getLoiTypeOfPowerPlantByItemCode(veiDTO.getVeiType());

        Optional.ofNullable(powerPlantType)
                .ifPresent(powerPlant::setType);

        BigDecimal veiInstalledCapacityMwh = veiDTO.getVeiInstalledCapacityMwh();

        if (veiInstalledCapacityMwh != null) {
            powerPlant.setInstalledPowerMw(veiInstalledCapacityMwh.setScale(4, RoundingMode.HALF_UP));
        }

        powerPlant.setOwner(legalPerson);

        powerPlant.setExternalNumber(veiDTO.getExternalNumber());

        return powerPlant;
    }


    //--------------------------------------------------------------------------------
    // SelfInvoicingLineDTO -> SelfInvoicingLine
    //--------------------------------------------------------------------------------
    public SelfInvoicingLine toSelfInvoicingLine(SelfInvoicingLineDTO selfInvoicingLineDTO, AgreementSelfInvoicing agreementSelfInvoicing) {
        SelfInvoicingLine newSelfInvoicingLine = new SelfInvoicingLine();

        newSelfInvoicingLine.setActivatedOn(selfInvoicingLineDTO.getActivatedOn());
        newSelfInvoicingLine.setDeActivatedOn(selfInvoicingLineDTO.getDeactivatedOn());

        newSelfInvoicingLine.setBic(selfInvoicingLineDTO.getBic());
        newSelfInvoicingLine.setIban(selfInvoicingLineDTO.getIban());

        newSelfInvoicingLine.setInvoiceEmail(selfInvoicingLineDTO.getInvoiceEmail1());
        newSelfInvoicingLine.setInvoiceEmailSec(selfInvoicingLineDTO.getInvoiceEmail2());

        //TODO: not sure
        newSelfInvoicingLine.setOwnName(selfInvoicingLineDTO.getName());

        // Handle State Code and Status Code
        Optional.ofNullable(selfInvoicingLineDTO.getStateCode())
                .map(Long::valueOf)
                .flatMap(id -> Optional.ofNullable(
                        dbLoiStatusCodeService.getLoiStatusCodeServiceByListOptionItemCode(id)
                ))
                .ifPresent(loiStatusCode -> {
                    newSelfInvoicingLine.setStatusCode(loiStatusCode);

                    // Check if the state code is active
                    newSelfInvoicingLine.setIsActiveStateCode(
                            Objects.equals(loiStatusCode.getListOptionItemCode(), LoiStatusCode.ACTIVE)
                    );
                });

         newSelfInvoicingLine.setAgreementSelfInvoicing(agreementSelfInvoicing);

        return newSelfInvoicingLine;
    }


    //--------------------------------------------------------------------------------
    // AgreementSelfInvoicingDTO -> AgreementSelfInvoicing
    //--------------------------------------------------------------------------------
    public AgreementSelfInvoicing toAgreementSelfInvoicing(AgreementSelfInvoicingDTO agreementSelfInvoicingDTO, PowerPlant powerPlant) {
        AgreementSelfInvoicing newAgreement = new AgreementSelfInvoicing();

        newAgreement.setCreatedOn(agreementSelfInvoicingDTO.getCreatedOn());
        newAgreement.setAgreementStartDate(agreementSelfInvoicingDTO.getAgreementStartDate());
        newAgreement.setAgreementEndDate(agreementSelfInvoicingDTO.getAgreementEndDate());

        //Check the statement
        String VAT_INCLUDED_KEYWORD = "VAT included";

        if (VAT_INCLUDED_KEYWORD.equals(agreementSelfInvoicingDTO.getAgreementVatTypeText())) {
            newAgreement.setIsVatIncluded(true);
            //TODO: set default value if not included
            newAgreement.setVatNumber(agreementSelfInvoicingDTO.getVatNumber());
            newAgreement.setVatRegistrationDate(agreementSelfInvoicingDTO.getVatRegistrationDate());
        } else {
            newAgreement.setIsVatIncluded(false);
        }

        String ACCEPT_KEYWORD = "Accept";

        newAgreement.setIsClientResponseAccept(
                ACCEPT_KEYWORD.equals(agreementSelfInvoicingDTO.getClientResponseText())
        );

        newAgreement.setEgn(agreementSelfInvoicingDTO.getEgn());
        newAgreement.setOwnName(agreementSelfInvoicingDTO.getName());

        // Handle Reason for Termination
        Optional.ofNullable(agreementSelfInvoicingDTO.getReasonForTerminationId())
                .map(Long::valueOf)
                .flatMap(id -> Optional.ofNullable(
                        dbLoiReasonForTerminationService.getLoiReasonForTerminationByListOptionItemCode(id)
                ))
                .ifPresent(reason -> {
                    newAgreement.setReasonForTermination(reason);
                    newAgreement.setTerminatedOn(agreementSelfInvoicingDTO.getTerminatedOn());
                    newAgreement.setTerminationDate(agreementSelfInvoicingDTO.getTerminationDate());
                });

        newAgreement.setSignedOn(agreementSelfInvoicingDTO.getSignedOn());


        // Handle Type of Service
        Optional.ofNullable(agreementSelfInvoicingDTO.getTypesOfServices())
                .map(Long::valueOf)
                .flatMap(id -> Optional.ofNullable(
                        dbLoiTypeOFServiceService.getLoiTypeOFServiceByListOptionItemCode(id)
                ))
                .ifPresent(newAgreement::setTypeOFService);

        //TODO: IN CRM ACTIVE CODE STATUS CODE IS 914050000 IN Entity it is 914050002L

        // Handle Agreement Status
        Optional.ofNullable(agreementSelfInvoicingDTO.getStatusCode())
                .map(Long::valueOf)
                .flatMap(id -> Optional.ofNullable(
                        dbLoiAgreementStatusService.getLoiAgreementStatusByListOptionItemCode(id)
                ))
                .ifPresent(agreementStatus -> {
                    newAgreement.setAgreementStatus(agreementStatus);

                    // Check for active status
                    newAgreement.setIsStateCodeStatusActive(
                            Objects.equals(agreementStatus.getListOptionItemCode(), LoiAgreementStatus.ACTIVE)
                    );
                });

        newAgreement.setPowerPlant(powerPlant);

        return newAgreement;
    }
}
