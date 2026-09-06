package bg.latona.santa.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RestResource;

import bg.latona.santa.entities.santa.common.CCcOrganizationUnit;
import bg.latona.santa.entities.santa.common.CCcPartner;

import bg.latona.santa.entities.ManagedCompany;

import bg.latona.santa.entities.santa.common.COrder;
import bg.latona.santa.entities.santa.common.LoiOrderStatus;
import bg.latona.santa.entities.santa.common.QCOrder;

public interface COrderRepository extends CommonRepository<COrder, QCOrder, Long>{

	@Query(value = "SELECT coalesce(max(orderNum), 0) FROM COrder")
	Long getMaxAutoIncNum();

	@RestResource(exported = false) //don't expose methods that are not checking permissions
	COrder findFirstByOutCodeAndParIdAndStatusAndCompanyAndDeleted(CCcOrganizationUnit outCode, CCcPartner part, LoiOrderStatus status, ManagedCompany company, boolean deleted);
	@RestResource(exported = false) //don't expose methods that are not checking permissions
	List<COrder> findCOrderByCompanyAndDeleted(ManagedCompany company, boolean deleted);
}
