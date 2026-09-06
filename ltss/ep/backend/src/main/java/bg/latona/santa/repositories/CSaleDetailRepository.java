package bg.latona.santa.repositories;

import java.util.List;

import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.ManagedCompany;
import bg.latona.santa.entities.santa.common.COfferDetail;
import bg.latona.santa.entities.santa.common.CSaleDetail;
import bg.latona.santa.entities.santa.common.CStock;
import bg.latona.santa.entities.santa.common.QCSaleDetail;

public interface CSaleDetailRepository extends CommonRepository<CSaleDetail, QCSaleDetail, Long> {

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<CSaleDetail> findByOdlIdAndStkIdAndCompanyAndDeleted(COfferDetail odlId, CStock stkId, ManagedCompany company, boolean deleted);
}
