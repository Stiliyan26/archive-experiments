package bg.latona.santa.repositories;

import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.CReserveQuantity;
import bg.latona.santa.entities.santa.common.CStock;
import bg.latona.santa.entities.santa.common.COffer;
import bg.latona.santa.entities.santa.common.COfferDetail;
import bg.latona.santa.entities.santa.common.QCReserveQuantity;

public interface CReserveQuantityRepository extends CommonRepository<CReserveQuantity, QCReserveQuantity, Long>{

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<CReserveQuantity> findByOfrIdAndCompanyAndDeleted(COffer ofrId, ManagedCompany company, boolean deleted);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<CReserveQuantity> findByOdlIdAndStkIdAndCompanyAndDeleted(COfferDetail odlId, CStock stkId, ManagedCompany company, boolean deleted);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<CReserveQuantity> findByOdlIdAndCompanyAndDeleted(COfferDetail odlId, ManagedCompany company, boolean deleted);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<CReserveQuantity> findByStkIdAndCompanyAndDeleted(CStock stkId, ManagedCompany company, boolean deleted);
}
