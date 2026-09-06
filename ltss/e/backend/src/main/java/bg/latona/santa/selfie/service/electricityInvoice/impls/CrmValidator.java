package bg.latona.santa.selfie.service.electricityInvoice.impls;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.nepal.PowerPlant;
import bg.latona.santa.entities.selfie.*;
import bg.latona.santa.repositories.LoiAgreementStatusRepository;
import bg.latona.santa.repositories.PowerPlantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class CrmValidator {

    private final LoiAgreementStatusRepository loiAgreementStatusRepository;
    private final PowerPlantRepository powerPlantRepository;

    protected Set<String> validateElectricityInvoice(
            ElectricityInvoice electricityInvoice,
            Set<AgreementTypeMapping> agreementTypeMappingSet,
            ManagedCompany managedCompany
    ) {
        log.info("validateElectricityInvoice start");

        Set<String> errors = new HashSet<>();

        Map<Long, LoiAgreementStatus> statusMap = getStatusMap(managedCompany);

        Optional<PowerPlant> optionalPowerPlant = getPowerPlant(electricityInvoice.getReportingPointOwn(), managedCompany);

        if (!optionalPowerPlant.isPresent()) {
            log.info("No powerPlant found with access point: {}", electricityInvoice.getReportingPointOwn());
            errors.add("Не съществува централа с ТО " + electricityInvoice.getReportingPointOwn() + ".");
        } else {

            validatePowerPlantAgreements(
                    statusMap,
                    optionalPowerPlant.get(),
                    electricityInvoice,
                    agreementTypeMappingSet,
                    errors
            );
        }

        log.info("validateElectricityInvoiceForAgreements end");
        log.info("errors - {}", errors.size());

        return errors;
    }


    private void validatePowerPlantAgreements(
            Map<Long, LoiAgreementStatus> statusMap,
            PowerPlant powerPlant,
            ElectricityInvoice invoice,
            Set<AgreementTypeMapping> agreementTypeMappingSet,
            Set<String> errors
    ) {
        String accessPoint = powerPlant.getAccessPoint();

        boolean powerPlantHasAgreementsSelfInvoicing = powerPlant.getAgreementsSelfInvoicing().size() != 0 && powerPlant.getAgreementsSelfInvoicing()
                .stream()
                .anyMatch(agreement -> !agreement.isDeleted());

        if (!powerPlantHasAgreementsSelfInvoicing) {
            log.info("powerPlant.getAgreementsSelfInvoicing() is empty for {}", accessPoint);
            errors.add("Не е открито споразумение за ТО " + accessPoint + ".");

            return;
        }

        List<AgreementSelfInvoicing> agreements = powerPlant.getAgreementsSelfInvoicing();

        AgreementSelfInvoicing lastAgreementSelfInvoicing = agreements.get(agreements.size() - 1);

        boolean lastAgreementSelfInvoicingHasAgreementsSelfInvoicingLine = lastAgreementSelfInvoicing.getSelfInvoicingLines().size() != 0 && lastAgreementSelfInvoicing.getSelfInvoicingLines()
                .stream()
                .anyMatch(selfInvoicingLine -> !selfInvoicingLine.isDeleted());
        log.info("lastAgreementSelfInvoicingHasAgreementsSelfInvoicingLine " + lastAgreementSelfInvoicingHasAgreementsSelfInvoicingLine);

        if (!lastAgreementSelfInvoicingHasAgreementsSelfInvoicingLine) {
            errors.add("Липсват линии на самофактуриране за последното споразумение на ТО " + accessPoint + ".");
            //TODO: should we return;
            return;
        }
        validateAgreementTypeMapping(agreementTypeMappingSet, lastAgreementSelfInvoicing, invoice, errors);

        if (!errors.isEmpty()) {
            return;
        }

        validateAgreementDatesAndStatus(
                statusMap,
                lastAgreementSelfInvoicing,
                invoice,
                errors
        );

        /*3•	„Дата на данъчно събитие“ трябва да бъде по-голяма или равна на „Период до“.
				 Ако това условие не е изпълнено, системата трябва да върне грешка.
				  фактурирането не може да се извърши в бъдещ период. */
        boolean isInvoicePeriodValid = invoice.getTaxEventDate().isAfter(invoice.getPeriodTo())
                || invoice.getTaxEventDate().isEqual(invoice.getPeriodTo());

        if (!isInvoicePeriodValid) {
            errors.add("Фактурирането не може да се извърши в бъдещ период.");
        }
    }


    private void validateAgreementDatesAndStatus(
            Map<Long, LoiAgreementStatus> statusMap,
            AgreementSelfInvoicing agreementSelfInvoicing,
            ElectricityInvoice invoice,
            Set<String> errors
    ) {
        LoiAgreementStatus agreementStatus = agreementSelfInvoicing.getAgreementStatus();

        String accessPoint = invoice.getReportingPointOwn();

        if (agreementStatus == null) {
            errors.add("Липсва статус на споразумение за ТО " + accessPoint + ".");

            return;
        }


        LocalDate taxEventDate = invoice.getTaxEventDate();

        ZonedDateTime startTS = agreementSelfInvoicing.getAgreementStartDate();
        ZonedDateTime endTS = agreementSelfInvoicing.getAgreementEndDate();
        ZonedDateTime terminationTS = agreementSelfInvoicing.getTerminationDate();


        List<String> dateErrors = validateDates(
                taxEventDate, startTS, endTS, terminationTS, accessPoint, agreementStatus, statusMap
        );

        if (!dateErrors.isEmpty()) {
            errors.addAll(dateErrors);

            return;
        }

        LocalDate agreementStartDate = startTS.toLocalDate();

        LocalDate agreementEndDate = endTS != null
                ? endTS.toLocalDate()
                : null;

        LocalDate agreementTerminationDate = terminationTS != null
                ? terminationTS.toLocalDate()
                : null;

        LoiAgreementStatus agreementStatusActive = statusMap.get(LoiAgreementStatus.ACTIVE);
        LoiAgreementStatus agreementStatusTerminated = statusMap.get(LoiAgreementStatus.TERMINATED);
        LoiAgreementStatus agreementStatusExpired = statusMap.get(LoiAgreementStatus.EXPIRED);

        boolean isAgreementValid = false;

        boolean isStatusActive = agreementStatus.getListOptionItemCode()
                .equals(agreementStatusActive.getListOptionItemCode());

        boolean isStatusExpired = agreementStatus.getListOptionItemCode()
                .equals(agreementStatusExpired.getListOptionItemCode());

        boolean isStatusTerminated = agreementStatus.getListOptionItemCode()
                .equals(agreementStatusTerminated.getListOptionItemCode());

        if (isStatusActive) {
            // 3.	Със статус Активно (Active), код 914050002
            // и датата на влизане в сила (Agreement Start Date) на Споразумението е ≤ на датата на данъчното събитие
            isAgreementValid = agreementStartDate.isBefore(taxEventDate) || agreementStartDate.isEqual(taxEventDate);

        } else if (isStatusExpired) {
            // 4. Със статус изтекло (Expired), код 914050005
            // и дата на изтичане (Agreement End Date) ≥ датата на данъчното събитие
            isAgreementValid = agreementEndDate != null &&
                    (agreementEndDate.isEqual(taxEventDate) || agreementEndDate.isAfter(taxEventDate));

        } else if (isStatusTerminated) {
            // 5.	Със статус  Прекратено (Terminated), код 914050006
            // и дата на прекратяване (Termination Date) ≥ датата на данъчното събитие
            isAgreementValid = agreementTerminationDate != null &&
                    (agreementTerminationDate.isEqual(taxEventDate) || agreementTerminationDate.isAfter(taxEventDate));
        }

        if (!isAgreementValid) {
            errors.add("Невалидно споразумение за ТО " + accessPoint + ".");
        }
    }


    private void validateAgreementTypeMapping(
            Set<AgreementTypeMapping> agreementTypeMappingSet,
            AgreementSelfInvoicing agreementSelfInvoicing,
            ElectricityInvoice invoice,
            Set<String> errors
    ) {

        boolean agreementTypeMappingSetHasCode = agreementTypeMappingSet.size() != 0 && agreementTypeMappingSet
                .stream()
                .anyMatch(agreementTypeMapping -> !agreementTypeMapping.isDeleted());

        if (!agreementTypeMappingSetHasCode) {
            errors.add("Липсват данни за кодове на услуги - СРМ");

            return;
        }

        Long typeOfServiceCode = agreementSelfInvoicing.getTypeOFService() != null
                ? agreementSelfInvoicing.getTypeOFService().getListOptionItemCode()
                : null;

        if (typeOfServiceCode == null) {
            errors.add("Липсва код на услуга за ТО " + invoice.getReportingPointOwn() + ".");

            return;
        }

        // Build a list of possible AgreementTypes
        List<AgreementType> agreementTypeList = agreementTypeMappingSet.stream()
                .filter(atm -> atm.getCrmCode() != null
                        && atm.getCrmCode().compareTo(BigDecimal.valueOf(typeOfServiceCode)) == 0
                        && atm.getAgreementTypes() != null
                )
                .flatMap(atm -> atm.getAgreementTypes().stream())
                .collect(Collectors.toList());

        if (agreementTypeList.isEmpty()) {
            errors.add("ТО " + invoice.getReportingPointOwn() + " няма валиден код на услуга.");

            return;
        }
        //TODO: check is it getAgreementType mandatory
        /*if (invoice.getAgreementType() == null || invoice.getAgreementType().getCode() == null) {
            errors.add("Липсва код на споразумение във фактурата.");

            return;
        }*/

        String electricityInvoiceAgreementTypeCode = invoice.getAgreementType().getCode();

        boolean foundMatch = agreementTypeList.stream()
                .anyMatch(at -> at.getCode().equals(electricityInvoiceAgreementTypeCode));

        if (!foundMatch) {
            errors.add("ТО " + invoice.getReportingPointOwn() + " няма валиден код на услуга.");
        }
    }


    public List<String> validateDates(
            LocalDate taxEventDate,
            ZonedDateTime startTS,
            ZonedDateTime endTS,
            ZonedDateTime terminationTS,
            String accessPoint,
            LoiAgreementStatus agreementStatus,
            Map<Long, LoiAgreementStatus> statusMap
    ) {
        List<String> errors = new ArrayList<>();

        LoiAgreementStatus agreementStatusTerminated = statusMap.get(LoiAgreementStatus.TERMINATED);
        LoiAgreementStatus agreementStatusExpired = statusMap.get(LoiAgreementStatus.EXPIRED);

        if (taxEventDate == null) {
            errors.add("Липсва дата на данъчно събитие в споразумението за ТО " + accessPoint + ".");
        }

        if (startTS == null) {
            errors.add("Липсва дата на влизане в сила в споразумението за ТО " + accessPoint + ".");
        }

        boolean isStatusTerminated = agreementStatus.getListOptionItemCode()
                .equals(agreementStatusTerminated.getListOptionItemCode());

        boolean isStatusExpired = agreementStatus.getListOptionItemCode()
                .equals(agreementStatusExpired.getListOptionItemCode());

        if (isStatusTerminated) {
            if (endTS == null) {
                errors.add("Липсва дата на изтичане в споразумението за ТО " + accessPoint + ".");
            }
        } else if (isStatusExpired) {
            if (terminationTS == null) {
                errors.add("Липсва дата на терминиране в споразумението за ТО " + accessPoint + ".");
            }
        }

        return errors;
    }


    private Map<Long, LoiAgreementStatus> getStatusMap(ManagedCompany managedCompany) {

        List<Long> statusCodes = Arrays.asList(
                LoiAgreementStatus.ACTIVE,
                LoiAgreementStatus.EXPIRED,
                LoiAgreementStatus.TERMINATED
        );

        List<LoiAgreementStatus> agreementStatuses = loiAgreementStatusRepository.findByListOptionItemCodeInAndCompanyAndDeleted(
                statusCodes, managedCompany, false
        );

        return agreementStatuses.stream()
                .collect(
                        Collectors.toMap(
                                LoiAgreementStatus::getListOptionItemCode,
                                Function.identity() //use the original object as the value
                        ));
    }


    private Optional<PowerPlant> getPowerPlant(String accessPoint, ManagedCompany managedCompany) {
        return powerPlantRepository.findByAccessPointAndCompanyAndDeleted(
                accessPoint, managedCompany, false
        );
    }
}
