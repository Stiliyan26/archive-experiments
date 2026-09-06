package bg.latona.santa.repositories;

import bg.latona.santa.entities.santa.common.CPriceList;
import bg.latona.santa.entities.santa.common.QCPriceList;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CCcPartner;
import bg.latona.santa.entities.santa.common.CGoods;

import java.util.List;

import org.springframework.data.rest.core.annotation.RestResource;

public interface CPriceListRepository extends CommonRepository<CPriceList, QCPriceList, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	CPriceList findFirstByParIdAndGodIdAndCompanyAndDeletedOrderByStartDateDesc(CCcPartner part, CGoods godId, ManagedCompany company, boolean deleted);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<CPriceList> findByParIdAndGodIdAndCompanyAndDeleted(CCcPartner parId, CGoods godId, ManagedCompany company, boolean deleted);
}
