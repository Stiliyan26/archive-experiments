package bg.latona.santa.repositories;

import bg.latona.santa.entities.santa.common.COrder;
import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CGoods;

import java.util.List;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.santa.common.COrderDetail;
import bg.latona.santa.entities.santa.common.QCOrderDetail;

public interface COrderDetailRepository extends CommonRepository<COrderDetail, QCOrderDetail, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	COrderDetail findFirstByOrrIdAndGodIdAndCompanyAndDeleted(COrder orrId, CGoods godId, ManagedCompany company, boolean deleted);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<COrderDetail> findByGodIdAndCompanyAndDeleted(CGoods godId, ManagedCompany company, boolean deleted);
}
