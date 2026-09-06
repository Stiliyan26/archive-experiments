package bg.latona.santa.repositories;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.nepal.*;
import org.springframework.data.rest.core.annotation.RestResource;
import java.util.List;

public interface IbexEnergyDealLegRepository extends CommonRepository<IbexEnergyDealLeg, QIbexEnergyDealLeg, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<IbexEnergyDealLeg> findByIbexEnergyDealAndDeleted(IbexEnergyDeal ibexEnergyDeal, boolean deleted);
/*
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	IbexEnergyDealLeg findFirstByTradeIdAndCompanyAndDeleted(String tradeId, ManagedCompany company, boolean deleted);*/
}
