package bg.latona.santa.repositories;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CCcPartner;
import bg.latona.santa.entities.santa.common.CDeliveryGoodMap;
import bg.latona.santa.entities.santa.common.CGoods;
import bg.latona.santa.entities.santa.common.QCDeliveryGoodMap;

public interface CDeliveryGoodMapRepository extends CommonRepository<CDeliveryGoodMap, QCDeliveryGoodMap, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	CDeliveryGoodMap findFirstByDeyCodeAndParIdAndCompanyAndDeleted(String deyCode, CCcPartner parId, ManagedCompany company, boolean deleted);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	CDeliveryGoodMap findFirstByParIdAndGodIdAndCompanyAndDeleted(CCcPartner parId, CGoods godId, ManagedCompany company, boolean deleted);
}
