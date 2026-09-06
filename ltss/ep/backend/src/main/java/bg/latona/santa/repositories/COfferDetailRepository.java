package bg.latona.santa.repositories;

import java.util.List;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CGoods;
import bg.latona.santa.entities.santa.common.COfferDetail;
import bg.latona.santa.entities.santa.common.QCOfferDetail;

public interface COfferDetailRepository extends CommonRepository<COfferDetail, QCOfferDetail, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<COfferDetail> findByGodIdAndCompanyAndDeleted(CGoods godId, ManagedCompany company, boolean deleted);
}
