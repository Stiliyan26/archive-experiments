package bg.latona.santa.repositories;

import java.util.List;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CDeliveryDetail;
import bg.latona.santa.entities.santa.common.CGoods;
import bg.latona.santa.entities.santa.common.QCDeliveryDetail;

public interface CDeliveryDetailRepository extends CommonRepository<CDeliveryDetail, QCDeliveryDetail, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<CDeliveryDetail> findByGodIdAndCompanyAndDeleted(CGoods godId, ManagedCompany company, boolean deleted);
}
