package bg.latona.santa.selfie.service.crm.impls;

import bg.latona.santa.entities.nepal.LoiTypeOfPowerPlant;
import bg.latona.santa.entities.nepal.PowerPlant;
import bg.latona.santa.entities.person.LegalPerson;
import bg.latona.santa.entities.security.SecUser;
import bg.latona.santa.repositories.*;
import bg.latona.santa.selfie.domain.crm.AccountCrm;
import bg.latona.santa.selfie.domain.crm.CrmRequestMock;
import bg.latona.santa.selfie.service.common.interfaces.EntityPersistenceService;
import bg.latona.santa.selfie.service.common.interfaces.entityRelated.DbSecUserService;
import bg.latona.santa.selfie.service.crm.interfaces.CrmMockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class CrmMockServiceImpl implements CrmMockService {

	private final PowerPlantRepository powerPlantRepository;
	private final LoiAgreementStatusRepository loiAgreementStatusRepository;
	private final LoiTypeOFServiceRepository loiTypeOFServiceRepository;
	private final LoiReasonForTerminationRepository loiReasonForTerminationRepository;

	private final EntityPersistenceService entityPersistenceService;
	private final DbSecUserService dbSecUserService;
	private final LegalPersonRepository legalPersonRepository;
	private final LoiStatusCodeRepository loiStatusCodeRepository;
	private final LoiTypeOfPowerPlantRepository loiTypeOfPowerPlantRepository;


	public List<Map<String, Object>> createOrUpdateCRMData(CrmRequestMock crmRequestMock) {

		List<Map<String, Object>> mapList = new ArrayList<>();
		SecUser secUser = dbSecUserService.getCurrentlyAuthenticatedUser();

		PowerPlant powerPlant = powerPlantRepository.findFirstByAccessPointAndCompanyAndDeleted(crmRequestMock.getVeiBgNumber(), secUser.getCompany(), false);
		LoiTypeOfPowerPlant loiTypeOfPowerPlant = loiTypeOfPowerPlantRepository.findFirstByListOptionItemCodeAndCompanyAndDeleted(crmRequestMock.getVeiType(), secUser.getCompany(), false);

		if (powerPlant == null) {

			powerPlant = new PowerPlant();
			powerPlant.setAccessPoint(crmRequestMock.getVeiBgNumber());
			powerPlant.setName(crmRequestMock.getVeiName());
			powerPlant.setType(loiTypeOfPowerPlant);
			powerPlant.setInstalledPowerMw(crmRequestMock.getVeiInstalledCapacityMwh());
			powerPlant.setExternalNumber(crmRequestMock.getExternalNumber());

		} else {
			powerPlant.setAccessPoint(crmRequestMock.getVeiBgNumber());
			powerPlant.setName(crmRequestMock.getVeiName());
			powerPlant.setType(loiTypeOfPowerPlant);
			powerPlant.setInstalledPowerMw(crmRequestMock.getVeiInstalledCapacityMwh());
			powerPlant.setExternalNumber(crmRequestMock.getExternalNumber());
		}

		AccountCrm accountCrm = crmRequestMock.getAccount();
		LegalPerson legalPerson = legalPersonRepository.findFirstByEikAndCompanyAndDeleted(accountCrm.getAccountNumber(), secUser.getCompany(), false);

		if (legalPerson == null) {

			legalPerson = new LegalPerson();

			legalPerson.setEik(accountCrm.getAccountNumber());
			legalPerson.setName(accountCrm.getName());
			legalPerson.setEgn(accountCrm.getEgn());
			legalPerson.setSapNumber(accountCrm.getSapNo());
			legalPerson.setAddress(accountCrm.getAddress().getStreetName() + ", " + accountCrm.getAddress().getStreetText() + ", " + accountCrm.getAddress().getNumber());
			legalPerson.setCity(accountCrm.getAddress().getCityName());
			legalPerson.setPostCode(accountCrm.getAddress().getPostalCode());
			legalPerson.setCountry("България");

			entityPersistenceService.persistEntity(legalPerson, true, true);

		} else {

			legalPerson.setEik(accountCrm.getAccountNumber());
			legalPerson.setName(accountCrm.getName());
			legalPerson.setEgn(accountCrm.getEgn());
			legalPerson.setSapNumber(accountCrm.getSapNo());

			legalPerson.setAddress(accountCrm.getAddress().getStreetName() + ", " + accountCrm.getAddress().getStreetText() + ", " + accountCrm.getAddress().getNumber());
			legalPerson.setCity(accountCrm.getAddress().getCityName());
			legalPerson.setPostCode(accountCrm.getAddress().getPostalCode());

			entityPersistenceService.persistEntity(legalPerson, true, false);
		}

		log.info("powerPlant.setOwner");
		powerPlant.setOwner(legalPerson);
		entityPersistenceService.persistEntity(powerPlant, true, false);

		return mapList;
	}
}